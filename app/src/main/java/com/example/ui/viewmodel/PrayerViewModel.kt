package com.example.ui.viewmodel

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.HijriEvent
import com.example.data.model.PrayerTime
import com.example.data.repository.PrayerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PrayerViewModel(application: Application) : AndroidViewModel(application) {

    private val _selectedCity = MutableStateFlow(PrayerRepository.citiesList.first().first)
    val selectedCity: StateFlow<String> = _selectedCity.asStateFlow()

    private val _userLat = MutableStateFlow(PrayerRepository.MECCA_LAT)
    val userLat: StateFlow<Double> = _userLat.asStateFlow()

    private val _userLng = MutableStateFlow(PrayerRepository.MECCA_LNG)
    val userLng: StateFlow<Double> = _userLng.asStateFlow()

    private val _prayerTimes = MutableStateFlow<List<PrayerTime>>(emptyList())
    val prayerTimes: StateFlow<List<PrayerTime>> = _prayerTimes.asStateFlow()

    private val _todayHijriDate = MutableStateFlow(PrayerRepository.getTodayHijriDateFormatted())
    val todayHijriDate: StateFlow<String> = _todayHijriDate.asStateFlow()

    private val _isLoadingPrayerTimes = MutableStateFlow(false)
    val isLoadingPrayerTimes: StateFlow<Boolean> = _isLoadingPrayerTimes.asStateFlow()

    private val _qiblaAngle = MutableStateFlow(0.0)
    val qiblaAngle: StateFlow<Double> = _qiblaAngle.asStateFlow()

    private val _distanceToKaabaKm = MutableStateFlow(0.0)
    val distanceToKaabaKm: StateFlow<Double> = _distanceToKaabaKm.asStateFlow()

    val hijriEvents: List<HijriEvent> = PrayerRepository.hijriEventsList

    private val _simulatedNotification = MutableStateFlow<String?>(null)
    val simulatedNotification: StateFlow<String?> = _simulatedNotification.asStateFlow()

    init {
        detectAndSetLocationFromTimeZone()
    }

    fun detectAndSetLocationFromTimeZone() {
        val detected = PrayerRepository.detectCurrentCityFromTimeZone()
        _selectedCity.value = detected.first
        _userLat.value = detected.second.first
        _userLng.value = detected.second.second
        recalculate()
    }

    fun updateCity(cityName: String) {
        val cityPair = PrayerRepository.citiesList.find { it.first == cityName }
            ?: PrayerRepository.citiesList.first()
        
        _selectedCity.value = cityPair.first
        _userLat.value = cityPair.second.first
        _userLng.value = cityPair.second.second

        recalculate()
    }

    fun updateGpsLocation(lat: Double, lng: Double, label: String = "الموقع الحالي (GPS)") {
        _selectedCity.value = label
        _userLat.value = lat
        _userLng.value = lng
        recalculate()
    }

    private fun recalculate() {
        viewModelScope.launch {
            _isLoadingPrayerTimes.value = true
            _qiblaAngle.value = PrayerRepository.calculateQiblaDirection(_userLat.value, _userLng.value)
            _distanceToKaabaKm.value = PrayerRepository.calculateDistanceToKaabaKm(_userLat.value, _userLng.value)

            val result = PrayerRepository.getPrayerTimesAndHijri(_userLat.value, _userLng.value)
            _prayerTimes.value = result.first
            _todayHijriDate.value = result.second
            _isLoadingPrayerTimes.value = false
        }
    }

    fun togglePrayerAlert(prayerName: String) {
        val currentList = _prayerTimes.value.toMutableList()
        val index = currentList.indexOfFirst { it.nameEnglish == prayerName || it.nameArabic == prayerName }
        if (index != -1) {
            val item = currentList[index]
            val updated = item.copy(alertEnabled = !item.alertEnabled)
            currentList[index] = updated
            _prayerTimes.value = currentList
            
            if (updated.alertEnabled) {
                val message = "🔔 تم تفعيل أذان ${updated.nameArabic} (${updated.nameEnglish}) الساعة ${updated.time}"
                _simulatedNotification.value = message
                showSystemNotification("تنبيه أذان ${updated.nameArabic}", message)
            }
        }
    }

    fun triggerHolidayReminderNotification(event: HijriEvent) {
        val msg = "🌙 Islamic Holiday Alert: ${event.titleArabic} (${event.titleEnglish}) - ${event.dateHijri}"
        _simulatedNotification.value = msg
        showSystemNotification(event.titleArabic, msg)
    }

    private fun showSystemNotification(title: String, message: String) {
        try {
            val context = getApplication<Application>().applicationContext
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId = "prayer_alerts_channel"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Prayer & Islamic Alerts",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notifications for prayer times and religious events"
                }
                notificationManager.createNotificationChannel(channel)
            }

            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)

            notificationManager.notify((System.currentTimeMillis() % 10000).toInt(), builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clearNotificationBanner() {
        _simulatedNotification.value = null
    }
}
