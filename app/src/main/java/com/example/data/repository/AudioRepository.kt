package com.example.data.repository

import com.example.data.model.Reciter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AudioRepository {

    val availableReciters = listOf(
        Reciter(
            id = "mishary",
            nameArabic = "الشيخ مشاري بن راشد العفاسي",
            nameEnglish = "Sheikh Mishary Rashid Al-Afasy",
            country = "Kuwait 🇰🇼",
            isDownloaded = true,
            downloadedSizeMb = 142.5,
            totalSizeMb = 142.5
        ),
        Reciter(
            id = "basit",
            nameArabic = "الشيخ عبد الباسط عبد الصمد",
            nameEnglish = "Sheikh Abdul Basit Abdul Samad",
            country = "Egypt 🇪🇬",
            isDownloaded = false,
            downloadedSizeMb = 0.0,
            totalSizeMb = 185.0
        ),
        Reciter(
            id = "sudais",
            nameArabic = "الشيخ عبد الرحمن السد يس",
            nameEnglish = "Sheikh Abdul Rahman Al-Sudais",
            country = "Saudi Arabia 🇸🇦",
            isDownloaded = true,
            downloadedSizeMb = 130.0,
            totalSizeMb = 130.0
        ),
        Reciter(
            id = "ghamdi",
            nameArabic = "الشيخ سعد الغامدي",
            nameEnglish = "Sheikh Saad Al-Ghamdi",
            country = "Saudi Arabia 🇸🇦",
            isDownloaded = false,
            downloadedSizeMb = 0.0,
            totalSizeMb = 150.0
        ),
        Reciter(
            id = "muaiqly",
            nameArabic = "الشيخ ماهر المعيقلي",
            nameEnglish = "Sheikh Maher Al-Muaiqly",
            country = "Saudi Arabia 🇸🇦",
            isDownloaded = false,
            downloadedSizeMb = 0.0,
            totalSizeMb = 160.0
        )
    )

    private val _recitersState = MutableStateFlow(availableReciters)
    val recitersState: StateFlow<List<Reciter>> = _recitersState.asStateFlow()

    fun toggleDownloadReciter(reciterId: String) {
        val currentList = _recitersState.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == reciterId }
        if (index != -1) {
            val reciter = currentList[index]
            val updated = reciter.copy(
                isDownloaded = !reciter.isDownloaded,
                downloadedSizeMb = if (!reciter.isDownloaded) reciter.totalSizeMb else 0.0
            )
            currentList[index] = updated
            _recitersState.value = currentList
        }
    }

    fun getEveryAyahFolder(reciterId: String): String {
        return when (reciterId) {
            "mishary" -> "Alafasy_128kbps"
            "basit" -> "Abdul_Basit_Murattal_192kbps"
            "sudais" -> "Abdurrahmaan_As-Sudais_192kbps"
            "ghamdi" -> "Ghamadi_40kbps"
            "muaiqly" -> "MaherAlMuaiqly128kbps"
            "shuraym" -> "Saood_ash-Shuraym_128kbps"
            else -> "Alafasy_128kbps"
        }
    }

    fun getAyahAudioUrl(reciterId: String, surahId: Int, ayahNumber: Int): String {
        val folder = getEveryAyahFolder(reciterId)
        val fileName = String.format(java.util.Locale.US, "%03d%03d.mp3", surahId, ayahNumber)
        return "https://everyayah.com/data/$folder/$fileName"
    }
}
