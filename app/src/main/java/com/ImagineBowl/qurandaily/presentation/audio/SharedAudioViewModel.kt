package com.imaginebowl.qurandaily.presentation.audio

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Shared audio state across tabs. Playback wiring (ExoPlayer) lands in a later milestone.
 */
class SharedAudioViewModel : ViewModel() {
    private val _currentSurahNumber = MutableStateFlow<Int?>(null)
    val currentSurahNumber: StateFlow<Int?> = _currentSurahNumber.asStateFlow()

    private val _currentAyahInSurah = MutableStateFlow<Int?>(null)
    val currentAyahInSurah: StateFlow<Int?> = _currentAyahInSurah.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    val showMiniPlayer: Boolean
        get() = _currentSurahNumber.value != null

    fun playSurah(surahNumber: Int, fromAyah: Int) {
        _currentSurahNumber.value = surahNumber
        _currentAyahInSurah.value = fromAyah
        _isPlaying.value = true
    }

    fun togglePlayback() {
        if (_currentSurahNumber.value != null) {
            _isPlaying.update { !it }
        }
    }

    fun pause() {
        _isPlaying.value = false
    }
}
