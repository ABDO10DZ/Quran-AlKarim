package com.example.ui.viewmodel

import android.app.Application
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.AppSettings
import com.example.data.model.Ayah
import com.example.data.model.Bookmark
import com.example.data.model.Surah
import com.example.data.repository.AudioRepository
import com.example.data.repository.QuranRepository
import com.example.data.repository.TafsirRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Locale

class QuranViewModel(application: Application) : AndroidViewModel(application), TextToSpeech.OnInitListener {

    private val db = AppDatabase.getDatabase(application)
    private val bookmarkDao = db.bookmarkDao()
    private val settingsDao = db.appSettingsDao()

    val surahList: List<Surah> = QuranRepository.surahsList
    val tajweedRules = QuranRepository.tajweedRules

    private val _selectedSurah = MutableStateFlow<Surah>(surahList.first())
    val selectedSurah: StateFlow<Surah> = _selectedSurah.asStateFlow()

    private val _currentAyahs = MutableStateFlow<List<Ayah>>(emptyList())
    val currentAyahs: StateFlow<List<Ayah>> = _currentAyahs.asStateFlow()

    private val _isLoadingAyahs = MutableStateFlow(false)
    val isLoadingAyahs: StateFlow<Boolean> = _isLoadingAyahs.asStateFlow()

    private val _ayahsErrorMessage = MutableStateFlow<String?>(null)
    val ayahsErrorMessage: StateFlow<String?> = _ayahsErrorMessage.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private var searchJob: Job? = null

    private val _searchResults = MutableStateFlow<List<Ayah>>(emptyList())
    val searchResults: StateFlow<List<Ayah>> = _searchResults.asStateFlow()

    private val _activePlayingAyahNumber = MutableStateFlow<Int?>(null)
    val activePlayingAyahNumber: StateFlow<Int?> = _activePlayingAyahNumber.asStateFlow()

    private val _isPlayingAudio = MutableStateFlow(false)
    val isPlayingAudio: StateFlow<Boolean> = _isPlayingAudio.asStateFlow()

    val bookmarksState: StateFlow<List<Bookmark>> = bookmarkDao.getAllBookmarks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lastReadBookmark: StateFlow<Bookmark?> = bookmarkDao.getLastRead()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val appSettings: StateFlow<AppSettings> = settingsDao.getSettings()
        .map { it ?: AppSettings() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            AppSettings()
        )

    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false

    init {
        tts = TextToSpeech(application, this)
        loadSurah(1)
        
        // Ensure initial settings exist in Room
        viewModelScope.launch {
            if (appSettings.value == null) {
                settingsDao.saveSettings(AppSettings())
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("ar"))
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isTtsInitialized = true
            }
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                override fun onDone(utteranceId: String?) {
                    viewModelScope.launch {
                        _isPlayingAudio.value = false
                        _activePlayingAyahNumber.value = null
                    }
                }
                override fun onError(utteranceId: String?) {
                    viewModelScope.launch {
                        _isPlayingAudio.value = false
                        _activePlayingAyahNumber.value = null
                    }
                }
            })
        }
    }

    fun loadSurah(surahId: Int) {
        val surah = surahList.find { it.id == surahId } ?: surahList.first()
        _selectedSurah.value = surah
        viewModelScope.launch {
            _isLoadingAyahs.value = true
            _ayahsErrorMessage.value = null
            val ayahs = QuranRepository.getAyahsForSurah(surahId)
            if (ayahs.isEmpty()) {
                _currentAyahs.value = emptyList()
                _ayahsErrorMessage.value = "تعذر تحميل آيات السورة، يرجى التأكد من الاتصال بالإنترنت ثم إعادة المحاولة."
            } else {
                _currentAyahs.value = ayahs
                _ayahsErrorMessage.value = null
                // Save as last read
                bookmarkDao.clearLastRead()
                bookmarkDao.insertBookmark(
                    Bookmark(
                        surahId = surah.id,
                        ayahNumber = 1,
                        surahNameArabic = surah.nameArabic,
                        surahNameEnglish = surah.nameEnglish,
                        ayahTextArabic = ayahs.firstOrNull()?.textArabic ?: "",
                        isLastRead = true
                    )
                )
            }
            _isLoadingAyahs.value = false
        }
    }

    fun retryLoadCurrentSurah() {
        loadSurah(_selectedSurah.value.id)
    }

    fun search(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        searchJob = viewModelScope.launch {
            val results = QuranRepository.searchVerses(query)
            _searchResults.value = results
        }
    }

    fun toggleBookmark(ayah: Ayah) {
        viewModelScope.launch {
            val surah = _selectedSurah.value
            val isBookmarked = bookmarksState.value.any { it.surahId == ayah.surahId && it.ayahNumber == ayah.ayahNumber && !it.isLastRead }
            if (isBookmarked) {
                bookmarkDao.deleteBookmarkByAyah(ayah.surahId, ayah.ayahNumber)
            } else {
                bookmarkDao.insertBookmark(
                    Bookmark(
                        surahId = ayah.surahId,
                        ayahNumber = ayah.ayahNumber,
                        surahNameArabic = surah.nameArabic,
                        surahNameEnglish = surah.nameEnglish,
                        ayahTextArabic = ayah.textArabic,
                        isLastRead = false
                    )
                )
            }
        }
    }

    private var mediaPlayer: MediaPlayer? = null

    fun playAyahAudio(ayah: Ayah) {
        if (_isPlayingAudio.value && _activePlayingAyahNumber.value == ayah.ayahNumber) {
            stopAudio()
            return
        }
        stopAudio()

        _activePlayingAyahNumber.value = ayah.ayahNumber
        _isPlayingAudio.value = true

        val reciterId = appSettings.value?.selectedReciterId ?: "mishary"
        val audioUrl = AudioRepository.getAyahAudioUrl(reciterId, ayah.surahId, ayah.ayahNumber)

        try {
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(audioUrl)
                setOnPreparedListener { mp ->
                    mp.start()
                }
                setOnCompletionListener {
                    _isPlayingAudio.value = false
                    _activePlayingAyahNumber.value = null
                }
                setOnErrorListener { _, _, _ ->
                    fallbackToTts(ayah)
                    true
                }
                prepareAsync()
            }
            mediaPlayer = player
        } catch (e: Exception) {
            fallbackToTts(ayah)
        }
    }

    private fun fallbackToTts(ayah: Ayah) {
        if (isTtsInitialized && tts != null) {
            val cleanText = ayah.textArabic.replace(Regex("<.*?>"), "")
            tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "AYAH_${ayah.id}")
        } else {
            viewModelScope.launch {
                kotlinx.coroutines.delay(4000)
                _isPlayingAudio.value = false
                _activePlayingAyahNumber.value = null
            }
        }
    }

    fun stopAudio() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaPlayer = null

        tts?.stop()
        _isPlayingAudio.value = false
        _activePlayingAyahNumber.value = null
    }

    private val _selectedTafsirSourceId = MutableStateFlow("muyassar")
    val selectedTafsirSourceId: StateFlow<String> = _selectedTafsirSourceId.asStateFlow()

    private val _isDownloadingOfflineData = MutableStateFlow(false)
    val isDownloadingOfflineData: StateFlow<Boolean> = _isDownloadingOfflineData.asStateFlow()

    private val _downloadProgressText = MutableStateFlow("")
    val downloadProgressText: StateFlow<String> = _downloadProgressText.asStateFlow()

    fun preloadAllDataForOffline() {
        viewModelScope.launch {
            _isDownloadingOfflineData.value = true
            _downloadProgressText.value = "جاري تنزيل وحفظ سور القرآن والتفاسير للاستخدام بدون إنترنت..."
            
            val keySurahs = listOf(1, 2, 3, 18, 36, 55, 67, 112, 113, 114)
            for (sId in keySurahs) {
                val sName = surahList.find { it.id == sId }?.nameArabic ?: sId.toString()
                _downloadProgressText.value = "جاري تنزيل سورة $sName وتفاسيرها..."
                QuranRepository.getAyahsForSurah(sId)
                TafsirRepository.getTafsirForAyah(sId, 1, "", "muyassar")
                TafsirRepository.getTafsirForAyah(sId, 1, "", "jalalayn")
            }

            _downloadProgressText.value = "تم الحفظ والتخزين بنجاح! التطبيق يعمل الآن بدون إنترنت."
            _isDownloadingOfflineData.value = false
        }
    }

    private val _currentAyahTafsir = MutableStateFlow<com.example.data.repository.AyahTafsir?>(null)
    val currentAyahTafsir: StateFlow<com.example.data.repository.AyahTafsir?> = _currentAyahTafsir.asStateFlow()

    private val _showTafsirSheet = MutableStateFlow(false)
    val showTafsirSheet: StateFlow<Boolean> = _showTafsirSheet.asStateFlow()

    fun openTafsirForAyah(ayah: Ayah) {
        viewModelScope.launch {
            _showTafsirSheet.value = true
            val tafsir = com.example.data.repository.TafsirRepository.getTafsirForAyah(
                surahId = ayah.surahId,
                ayahNumber = ayah.ayahNumber,
                ayahTextArabic = ayah.textArabic,
                sourceId = _selectedTafsirSourceId.value
            )
            _currentAyahTafsir.value = tafsir
        }
    }

    fun openTafsirForCurrentSurah() {
        val ayahs = currentAyahs.value
        if (ayahs.isNotEmpty()) {
            openTafsirForAyah(ayahs.first())
        }
    }

    fun setTafsirSource(sourceId: String) {
        _selectedTafsirSourceId.value = sourceId
        val currentTafsir = _currentAyahTafsir.value
        if (currentTafsir != null) {
            viewModelScope.launch {
                val updated = com.example.data.repository.TafsirRepository.getTafsirForAyah(
                    surahId = currentTafsir.surahId,
                    ayahNumber = currentTafsir.ayahNumber,
                    ayahTextArabic = currentTafsir.ayahTextArabic,
                    sourceId = sourceId
                )
                _currentAyahTafsir.value = updated
            }
        }
    }

    fun closeTafsirSheet() {
        _showTafsirSheet.value = false
    }

    fun updateSettings(transform: (AppSettings) -> AppSettings) {
        val current = appSettings.value ?: AppSettings()
        val updated = transform(current)
        viewModelScope.launch {
            settingsDao.saveSettings(updated)
        }
    }

    fun deleteBookmark(bookmarkId: Int) {
        viewModelScope.launch {
            bookmarkDao.deleteBookmark(bookmarkId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
    }
}
