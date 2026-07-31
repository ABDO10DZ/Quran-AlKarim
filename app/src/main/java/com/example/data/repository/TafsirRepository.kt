package com.example.data.repository

import com.example.data.model.Ayah
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

data class TafsirSource(
    val id: String,
    val nameArabic: String,
    val nameEnglish: String,
    val scholar: String,
    val description: String,
    val apiEdition: String
)

data class AyahTafsir(
    val surahId: Int,
    val ayahNumber: Int,
    val ayahTextArabic: String,
    val tafsirText: String,
    val sourceId: String
)

object TafsirRepository {

    private val tafsirCache = ConcurrentHashMap<String, String>()
    private val surahFetchCompletedMap = ConcurrentHashMap<String, Boolean>()

    val availableTafsirSources = listOf(
        TafsirSource(
            id = "muyassar",
            nameArabic = "التفسير الميسر",
            nameEnglish = "Tafsir Al-Muyassar",
            scholar = "مجمع الملك فهد لطباعة المصحف الشريف",
            description = "تفسير ميسر ومبسط باللغة المعاصرة وفق عقيدة السلف الصالح",
            apiEdition = "ar.muyassar"
        ),
        TafsirSource(
            id = "jalalayn",
            nameArabic = "تفسير الجلالين",
            nameEnglish = "Tafsir Al-Jalalayn",
            scholar = "الجلال المحلي والجلال السيوطي",
            description = "تفسير موجز ودقيق يعتني بالمعنى اللغوي والإعراب واستنباط الأحكام",
            apiEdition = "ar.jalalayn"
        ),
        TafsirSource(
            id = "qurtubi",
            nameArabic = "تفسير القرطبي",
            nameEnglish = "Tafsir Al-Qurtubi",
            scholar = "الإمام أبي عبد الله القرطبي",
            description = "الجامع لأحكام القرآن - أجمع كتب التفسير لأحكام الفقه والمعاني",
            apiEdition = "ar.qurtubi"
        ),
        TafsirSource(
            id = "waseet",
            nameArabic = "التفسير الوسيط",
            nameEnglish = "Tafsir Al-Waseet",
            scholar = "الإمام محمد سيد طنطاوي",
            description = "تفسير وسيط معاصر يجمع بين الأصالة وسهولة العبارة",
            apiEdition = "ar.waseet"
        ),
        TafsirSource(
            id = "baghawi",
            nameArabic = "تفسير البغوي",
            nameEnglish = "Tafsir Al-Baghawi",
            scholar = "الإمام الحسين بن مسعود البغوي",
            description = "معالم التنزيل - من أصفى تفاسير أهل السنة والجماعة وأسلمها",
            apiEdition = "ar.baghawi"
        ),
        TafsirSource(
            id = "miqbas",
            nameArabic = "تنوير المقباس",
            nameEnglish = "Tanwir Al-Miqbas",
            scholar = "منسوب لعبد الله بن عباس رضي الله عنهما",
            description = "تفسير مأثور يرادي معاني الآيات وإعرابها وأسباب نزولها",
            apiEdition = "ar.miqbas"
        )
    )

    suspend fun getTafsirForAyah(
        surahId: Int,
        ayahNumber: Int,
        ayahTextArabic: String,
        sourceId: String
    ): AyahTafsir = withContext(Dispatchers.IO) {
        val cacheKey = "$surahId:$ayahNumber:$sourceId"

        // 1. Check in-memory cache
        if (tafsirCache.containsKey(cacheKey)) {
            return@withContext AyahTafsir(
                surahId = surahId,
                ayahNumber = ayahNumber,
                ayahTextArabic = ayahTextArabic,
                tafsirText = tafsirCache[cacheKey]!!,
                sourceId = sourceId
            )
        }

        val source = availableTafsirSources.find { it.id == sourceId } ?: availableTafsirSources[0]
        val surahCompletedKey = "$surahId:${source.apiEdition}"

        // 2. Fetch entire Surah tafsir if not yet fetched
        if (surahFetchCompletedMap[surahCompletedKey] != true) {
            fetchAndCacheSurahTafsir(surahId, source)
        }

        // 3. Return cached text or fallback single Ayah fetch
        val tafsirText = tafsirCache[cacheKey] ?: fetchSingleAyahTafsirFromApi(surahId, ayahNumber, source.apiEdition)

        val finalTafsir = tafsirText ?: "تفسير الآية ($surahId:$ayahNumber) غير متوفر حالياً في الشبكة. يرجى التأكد من الاتصال بالإنترنت."

        // Store in cache
        tafsirCache[cacheKey] = finalTafsir

        AyahTafsir(
            surahId = surahId,
            ayahNumber = ayahNumber,
            ayahTextArabic = ayahTextArabic,
            tafsirText = finalTafsir,
            sourceId = sourceId
        )
    }

    private fun fetchAndCacheSurahTafsir(surahId: Int, source: TafsirSource) {
        try {
            val url = URL("https://api.alquran.cloud/v1/surah/$surahId/${source.apiEdition}")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 8000
            conn.readTimeout = 8000

            if (conn.responseCode == 200) {
                val jsonString = conn.inputStream.bufferedReader().use { it.readText() }
                conn.disconnect()

                val rootObj = JSONObject(jsonString)
                if (rootObj.optInt("code") == 200) {
                    val dataObj = rootObj.getJSONObject("data")
                    val ayahsArray = dataObj.getJSONArray("ayahs")

                    for (i in 0 until ayahsArray.length()) {
                        val item = ayahsArray.getJSONObject(i)
                        val numInSurah = item.getInt("numberInSurah")
                        val text = item.getString("text").trim()
                        val itemKey = "$surahId:$numInSurah:${source.id}"
                        tafsirCache[itemKey] = text
                    }
                    surahFetchCompletedMap["$surahId:${source.apiEdition}"] = true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun fetchSingleAyahTafsirFromApi(surahId: Int, ayahNumber: Int, edition: String): String? {
        try {
            val url = URL("https://api.alquran.cloud/v1/ayah/$surahId:$ayahNumber/$edition")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 5000
            conn.readTimeout = 5000

            if (conn.responseCode == 200) {
                val jsonString = conn.inputStream.bufferedReader().use { it.readText() }
                conn.disconnect()

                val rootObj = JSONObject(jsonString)
                if (rootObj.optInt("code") == 200) {
                    val dataObj = rootObj.getJSONObject("data")
                    return dataObj.getString("text").trim()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}
