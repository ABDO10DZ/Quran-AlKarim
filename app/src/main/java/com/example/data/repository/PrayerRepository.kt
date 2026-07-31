package com.example.data.repository

import com.example.data.model.HijriEvent
import com.example.data.model.PrayerTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

object PrayerRepository {

    // Kaaba location coordinates in Mecca
    const val MECCA_LAT = 21.4225
    const val MECCA_LNG = 39.8262

    val citiesList = listOf(
        Pair("Makkah Al-Mukarramah", Pair(21.4225, 39.8262)),
        Pair("Madinah Al-Munawwarah", Pair(24.4672, 39.6112)),
        Pair("Algiers, Algeria", Pair(36.7538, 3.0588)),
        Pair("Oran, Algeria", Pair(35.6987, -0.6349)),
        Pair("Constantine, Algeria", Pair(36.3650, 6.6147)),
        Pair("Cairo, Egypt", Pair(30.0444, 31.2357)),
        Pair("Riyadh, KSA", Pair(24.7136, 46.6753)),
        Pair("Casablanca, Morocco", Pair(33.5731, -7.5898)),
        Pair("Tunis, Tunisia", Pair(36.8065, 10.1815)),
        Pair("Istanbul, Türkiye", Pair(41.0082, 28.9784)),
        Pair("Jakarta, Indonesia", Pair(-6.2088, 106.8456)),
        Pair("Kuala Lumpur, Malaysia", Pair(3.1390, 101.6869)),
        Pair("London, United Kingdom", Pair(51.5074, -0.1278)),
        Pair("New York, USA", Pair(40.7128, -74.0060)),
        Pair("Dubai, UAE", Pair(25.2048, 55.2708))
    )

    fun detectCurrentCityFromTimeZone(): Pair<String, Pair<Double, Double>> {
        val tzId = java.util.TimeZone.getDefault().id.lowercase()
        return when {
            tzId.contains("algiers") || tzId.contains("algeria") -> Pair("Algiers, Algeria", Pair(36.7538, 3.0588))
            tzId.contains("riyadh") || tzId.contains("saudi") || tzId.contains("asia/riyadh") -> Pair("Riyadh, KSA", Pair(24.7136, 46.6753))
            tzId.contains("cairo") || tzId.contains("egypt") -> Pair("Cairo, Egypt", Pair(30.0444, 31.2357))
            tzId.contains("casablanca") || tzId.contains("morocco") -> Pair("Casablanca, Morocco", Pair(33.5731, -7.5898))
            tzId.contains("london") || tzId.contains("europe/london") -> Pair("London, United Kingdom", Pair(51.5074, -0.1278))
            tzId.contains("new_york") || tzId.contains("america") -> Pair("New York, USA", Pair(40.7128, -74.0060))
            tzId.contains("dubai") || tzId.contains("uae") -> Pair("Dubai, UAE", Pair(25.2048, 55.2708))
            tzId.contains("istanbul") || tzId.contains("turkey") -> Pair("Istanbul, Türkiye", Pair(41.0082, 28.9784))
            tzId.contains("jakarta") || tzId.contains("indonesia") -> Pair("Jakarta, Indonesia", Pair(-6.2088, 106.8456))
            tzId.contains("kuala_lumpur") || tzId.contains("malaysia") -> Pair("Kuala Lumpur, Malaysia", Pair(3.1390, 101.6869))
            tzId.contains("mecca") || tzId.contains("makkah") -> Pair("Makkah Al-Mukarramah", Pair(21.4225, 39.8262))
            else -> Pair("Algiers, Algeria", Pair(36.7538, 3.0588))
        }
    }

    suspend fun getPrayerTimesAndHijri(lat: Double, lng: Double): Pair<List<PrayerTime>, String> = withContext(Dispatchers.IO) {
        val onlineResult = fetchFromAladhanApi(lat, lng)
        if (onlineResult != null) {
            return@withContext onlineResult
        }
        val offlineTimes = calculatePrayerTimesOffline(lat, lng)
        val offlineHijri = getTodayHijriDateFormatted()
        return@withContext Pair(offlineTimes, offlineHijri)
    }

    private fun fetchFromAladhanApi(lat: Double, lng: Double): Pair<List<PrayerTime>, String>? {
        try {
            val url = URL("https://api.aladhan.com/v1/timings?latitude=$lat&longitude=$lng&method=3")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 8000
            conn.readTimeout = 8000

            if (conn.responseCode == 200) {
                val jsonString = conn.inputStream.bufferedReader().use { it.readText() }
                conn.disconnect()

                val root = JSONObject(jsonString)
                if (root.optInt("code") == 200) {
                    val data = root.getJSONObject("data")
                    val timings = data.getJSONObject("timings")

                    val fajrStr = format24To12(timings.optString("Fajr"))
                    val sunriseStr = format24To12(timings.optString("Sunrise"))
                    val dhuhrStr = format24To12(timings.optString("Dhuhr"))
                    val asrStr = format24To12(timings.optString("Asr"))
                    val maghribStr = format24To12(timings.optString("Maghrib"))
                    val ishaStr = format24To12(timings.optString("Isha"))

                    val hijriObj = data.getJSONObject("date").getJSONObject("hijri")
                    val hijriDay = hijriObj.optString("day")
                    val hijriMonthAr = hijriObj.getJSONObject("month").optString("ar")
                    val hijriMonthEn = hijriObj.getJSONObject("month").optString("en")
                    val hijriYear = hijriObj.optString("year")

                    val formattedHijri = "$hijriDay $hijriMonthAr $hijriYear هـ - $hijriDay $hijriMonthEn $hijriYear AH"

                    val calendar = Calendar.getInstance()
                    val currentTotalMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)

                    val timesMin = listOf(
                        parseToMinutes(timings.optString("Fajr")),
                        parseToMinutes(timings.optString("Sunrise")),
                        parseToMinutes(timings.optString("Dhuhr")),
                        parseToMinutes(timings.optString("Asr")),
                        parseToMinutes(timings.optString("Maghrib")),
                        parseToMinutes(timings.optString("Isha"))
                    )

                    var nextIdx = -1
                    for (i in timesMin.indices) {
                        if (currentTotalMinutes < timesMin[i]) {
                            nextIdx = i
                            break
                        }
                    }
                    if (nextIdx == -1) nextIdx = 0

                    val prayerTimesList = listOf(
                        PrayerTime("الفجر", "Fajr", fajrStr, "ic_fajr", isNext = nextIdx == 0),
                        PrayerTime("الشروق", "Sunrise", sunriseStr, "ic_sunrise", isNext = false),
                        PrayerTime("الظهر", "Dhuhr", dhuhrStr, "ic_dhuhr", isNext = nextIdx == 2),
                        PrayerTime("العصر", "Asr", asrStr, "ic_asr", isNext = nextIdx == 3),
                        PrayerTime("المغرب", "Maghrib", maghribStr, "ic_maghrib", isNext = nextIdx == 4),
                        PrayerTime("العشاء", "Isha", ishaStr, "ic_isha", isNext = nextIdx == 5)
                    )

                    return Pair(prayerTimesList, formattedHijri)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun parseToMinutes(time24: String): Int {
        return try {
            val parts = time24.trim().split(":")
            val h = parts[0].trim().toInt()
            val m = parts[1].substring(0, 2).toInt()
            h * 60 + m
        } catch (e: Exception) {
            720
        }
    }

    private fun format24To12(time24: String): String {
        return try {
            val parts = time24.trim().split(":")
            val h24 = parts[0].trim().toInt()
            val m = parts[1].substring(0, 2).toInt()
            val ampm = if (h24 >= 12) "PM" else "AM"
            val h12 = if (h24 % 12 == 0) 12 else h24 % 12
            String.format(Locale.US, "%02d:%02d %s", h12, m, ampm)
        } catch (e: Exception) {
            time24
        }
    }

    fun calculatePrayerTimesOffline(lat: Double, lng: Double): List<PrayerTime> {
        val calendar = Calendar.getInstance()
        val currentTotalMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)

        // Local timezone offset estimated from longitude
        val tzOffsetHours = Math.round(lng / 15.0).toInt()
        val solarNoonMinutes = 720 + ((tzOffsetHours * 15.0 - lng) * 4).toInt()

        val fajrMin = (solarNoonMinutes - 460).coerceIn(240, 320)
        val sunriseMin = (solarNoonMinutes - 380).coerceIn(330, 400)
        val dhuhrMin = solarNoonMinutes.coerceIn(700, 760)
        val asrMin = (solarNoonMinutes + 200).coerceIn(900, 980)
        val maghribMin = (solarNoonMinutes + 375).coerceIn(1080, 1170)
        val ishaMin = (solarNoonMinutes + 465).coerceIn(1180, 1260)

        fun formatMin(min: Int): String {
            val h24 = ((min / 60) % 24 + 24) % 24
            val m = min % 60
            val ampm = if (h24 >= 12) "PM" else "AM"
            val h12 = if (h24 % 12 == 0) 12 else h24 % 12
            return String.format(Locale.US, "%02d:%02d %s", h12, m, ampm)
        }

        val prayerMinutes = listOf(fajrMin, sunriseMin, dhuhrMin, asrMin, maghribMin, ishaMin)
        var nextFoundIndex = -1
        for (i in prayerMinutes.indices) {
            if (currentTotalMinutes < prayerMinutes[i]) {
                nextFoundIndex = i
                break
            }
        }
        if (nextFoundIndex == -1) nextFoundIndex = 0

        return listOf(
            PrayerTime("الفجر", "Fajr", formatMin(fajrMin), "ic_fajr", isNext = nextFoundIndex == 0),
            PrayerTime("الشروق", "Sunrise", formatMin(sunriseMin), "ic_sunrise", isNext = false),
            PrayerTime("الظهر", "Dhuhr", formatMin(dhuhrMin), "ic_dhuhr", isNext = nextFoundIndex == 2),
            PrayerTime("العصر", "Asr", formatMin(asrMin), "ic_asr", isNext = nextFoundIndex == 3),
            PrayerTime("المغرب", "Maghrib", formatMin(maghribMin), "ic_maghrib", isNext = nextFoundIndex == 4),
            PrayerTime("العشاء", "Isha", formatMin(ishaMin), "ic_isha", isNext = nextFoundIndex == 5)
        )
    }

    fun calculateQiblaDirection(userLat: Double, userLng: Double): Double {
        val userLatRad = Math.toRadians(userLat)
        val userLngRad = Math.toRadians(userLng)
        val meccaLatRad = Math.toRadians(MECCA_LAT)
        val meccaLngRad = Math.toRadians(MECCA_LNG)

        val dLng = meccaLngRad - userLngRad

        val y = sin(dLng) * cos(meccaLatRad)
        val x = cos(userLatRad) * sin(meccaLatRad) - sin(userLatRad) * cos(meccaLatRad) * cos(dLng)

        var azimuth = Math.toDegrees(atan2(y, x))
        azimuth = (azimuth + 360) % 360
        return azimuth
    }

    fun calculateDistanceToKaabaKm(userLat: Double, userLng: Double): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(MECCA_LAT - userLat)
        val dLng = Math.toRadians(MECCA_LNG - userLng)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(userLat)) * cos(Math.toRadians(MECCA_LAT)) *
                sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        return r * c
    }

    fun getTodayHijriDateFormatted(): String {
        val calendar = Calendar.getInstance()
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        // Simple Hijri offset calculation
        val hijriDay = ((day + 12) % 30) + 1
        return "$hijriDay صفر 1448 هـ - $hijriDay Safar 1448 AH"
    }

    val hijriEventsList = listOf(
        HijriEvent("بداية شهر رمضان المبارك", "Start of Holy Ramadan", "1 رمضان 1448", "March 2027", isHoliday = true, description = "بداية شهر الصيام والقيام والقرآن"),
        HijriEvent("ليلة القدر المباركة", "Laylat al-Qadr", "27 رمضان 1448", "April 2027", isHoliday = true, description = "خير من ألف شهر، تنزل الملائكة والروح فيها"),
        HijriEvent("عيد الفطر السعيد", "Eid al-Fitr", "1 شوال 1448", "April 2027", isHoliday = true, description = "عيد الفرحة بإتمام صيام شهر رمضان"),
        HijriEvent("يوم عرفة المبارك", "Day of Arafah", "9 ذو الحجة 1448", "June 2027", isHoliday = true, description = "أعظم أيام السنة، أفضل الدعاء دعاء يوم عرفة"),
        HijriEvent("عيد الأضحى المبارك", "Eid al-Adha", "10 ذو الحجة 1448", "June 2027", isHoliday = true, description = "عيد التضحية والفداء والتقرب إلى الله"),
        HijriEvent("رأس السنة الهجرية", "Islamic New Year 1449", "1 محرم 1449", "July 2027", isHoliday = true, description = "ذكرى هجرة النبي محمد ﷺ إلى المدينة المنورة"),
        HijriEvent("يوم عاشوراء", "Day of Ashura", "10 محرم 1449", "July 2027", isHoliday = true, description = "يوم نجاة نبي الله موسى عليه السلام وتكفير سنة من الذنوب")
    )
}
