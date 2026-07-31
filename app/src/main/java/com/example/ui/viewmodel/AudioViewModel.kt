package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.data.model.Reciter
import com.example.data.repository.AudioRepository
import kotlinx.coroutines.flow.StateFlow

class AudioViewModel(application: Application) : AndroidViewModel(application) {

    val reciters: StateFlow<List<Reciter>> = AudioRepository.recitersState

    fun toggleDownload(reciterId: String) {
        AudioRepository.toggleDownloadReciter(reciterId)
    }
}
