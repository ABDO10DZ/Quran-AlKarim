package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class RevelationType {
    MECCAN, MEDINAN
}

data class Surah(
    val id: Int,
    val nameArabic: String,
    val nameEnglish: String,
    val nameTranslation: String,
    val totalAyahs: Int,
    val revelationType: RevelationType,
    val startPage: Int,
    val startJuz: Int
)

data class Ayah(
    val id: Int,
    val surahId: Int,
    val ayahNumber: Int,
    val textArabic: String,
    val textEnglish: String,
    val juz: Int,
    val page: Int,
    val tajweedAnnotated: String = "",
    val sajdah: Boolean = false
)

@Entity(tableName = "bookmarks")
data class Bookmark(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val surahId: Int,
    val ayahNumber: Int,
    val surahNameArabic: String,
    val surahNameEnglish: String,
    val ayahTextArabic: String,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isLastRead: Boolean = false
)

data class TajweedRule(
    val id: String,
    val nameArabic: String,
    val nameEnglish: String,
    val descriptionArabic: String,
    val descriptionEnglish: String,
    val colorHex: String,
    val exampleArabic: String,
    val exampleSurah: String,
    val audioSampleRes: String = ""
)

data class Reciter(
    val id: String,
    val nameArabic: String,
    val nameEnglish: String,
    val country: String,
    val isDownloaded: Boolean = false,
    val downloadedSizeMb: Double = 0.0,
    val totalSizeMb: Double = 120.0,
    val sampleAudioUrl: String = ""
)

data class PrayerTime(
    val nameArabic: String,
    val nameEnglish: String,
    val time: String,
    val iconName: String,
    val isNext: Boolean = false,
    val alertEnabled: Boolean = true
)

data class HijriEvent(
    val titleArabic: String,
    val titleEnglish: String,
    val dateHijri: String,
    val dayGregorian: String,
    val isHoliday: Boolean = true,
    val description: String = ""
)

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey val id: Int = 1,
    val isArabicDefault: Boolean = true,
    val isEnglishSecondary: Boolean = true,
    val themeMode: String = "EMERALD_DARK", // EMERALD_DARK, CLASSIC_LIGHT, HIGH_CONTRAST, SEPIA
    val fontSizeSp: Float = 26f,
    val translationFontSizeSp: Float = 16f,
    val showTajweedColor: Boolean = true,
    val showEnglishTranslation: Boolean = true,
    val isBookMode: Boolean = false,
    val selectedReciterId: String = "mishary",
    val calculationMethod: String = "MWL", // Muslim World League
    val isPrayerAlertsEnabled: Boolean = true,
    val isHolidayRemindersEnabled: Boolean = true,
    val userCity: String = "Makkah",
    val userLat: Double = 21.4225,
    val userLng: Double = 39.8262
)
