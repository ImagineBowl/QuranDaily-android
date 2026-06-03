package com.imaginebowl.qurandaily.presentation.audio

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imaginebowl.qurandaily.core.domain.repository.AudioRepository
import com.imaginebowl.qurandaily.core.domain.usecase.FetchQuranUseCase
import com.imaginebowl.qurandaily.data.audio.QuranAudioPlayer
import com.imaginebowl.qurandaily.data.audio.QuranPlaybackService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SharedAudioViewModel(
    private val appContext: Context,
    private val audioPlayer: QuranAudioPlayer,
    private val audioRepository: AudioRepository,
    private val fetchQuranUseCase: FetchQuranUseCase,
) : ViewModel() {
    private val _currentSurahNumber = MutableStateFlow<Int?>(null)
    val currentSurahNumber: StateFlow<Int?> = _currentSurahNumber.asStateFlow()

    private val _currentAyahInSurah = MutableStateFlow<Int?>(null)
    val currentAyahInSurah: StateFlow<Int?> = _currentAyahInSurah.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isLoadingAudio = MutableStateFlow(false)
    val isLoadingAudio: StateFlow<Boolean> = _isLoadingAudio.asStateFlow()

    private val _currentTime = MutableStateFlow(0.0)
    val currentTime: StateFlow<Double> = _currentTime.asStateFlow()

    /** Smooth UI position — interpolated between ExoPlayer sync ticks (~60fps). */
    private val _displayTime = MutableStateFlow(0.0)
    val displayTime: StateFlow<Double> = _displayTime.asStateFlow()

    private val _duration = MutableStateFlow(0.0)
    val duration: StateFlow<Double> = _duration.asStateFlow()

    private val _audioErrorMessage = MutableStateFlow<String?>(null)
    val audioErrorMessage: StateFlow<String?> = _audioErrorMessage.asStateFlow()

    private val _currentAyahArabicPreview = MutableStateFlow<String?>(null)
    val currentAyahArabicPreview: StateFlow<String?> = _currentAyahArabicPreview.asStateFlow()

    private val _selectedSurahNumber = MutableStateFlow(1)
    val selectedSurahNumber: StateFlow<Int> = _selectedSurahNumber.asStateFlow()

    private val _downloadedSurahs = MutableStateFlow<Set<Int>>(emptySet())
    val downloadedSurahs: StateFlow<Set<Int>> = _downloadedSurahs.asStateFlow()

    private val _isDownloadingAudio = MutableStateFlow(false)
    val isDownloadingAudio: StateFlow<Boolean> = _isDownloadingAudio.asStateFlow()

    private var previewJob: Job? = null
    private var progressJob: Job? = null
    private var syncJob: Job? = null
    private var lastPreviewKey: String? = null
    private var progressAnchor = ProgressAnchor()

    private data class ProgressAnchor(
        val positionSec: Double = 0.0,
        val durationSec: Double = 0.0,
        val wallClockMs: Long = 0L,
    )

    val showMiniPlayer: Boolean
        get() = _currentSurahNumber.value != null || _isLoadingAudio.value

    val playbackTrackId: String
        get() {
            val surah = _currentSurahNumber.value ?: return "idle"
            val ayah = _currentAyahInSurah.value ?: return "surah-$surah"
            return "$surah-$ayah"
        }

    init {
        viewModelScope.launch {
            _downloadedSurahs.value = audioRepository.downloadedSurahNumbers().toSet()
        }
        viewModelScope.launch {
            audioPlayer.snapshot.collect { applySnapshotFromPlayer() }
        }
    }

    fun playSurah(
        surahNumber: Int,
        fromAyah: Int,
        totalAyahs: Int? = null,
        stopsAtSurahEnd: Boolean = true,
    ) {
        viewModelScope.launch {
            startPlaybackService()
            _selectedSurahNumber.value = surahNumber
            val total = totalAyahs ?: fetchQuranUseCase.executeSurahs()
                .firstOrNull { it.number == surahNumber }
                ?.numberOfAyahs ?: 286
            audioPlayer.playSurahAyah(
                surahNumber = surahNumber,
                fromAyah = fromAyah,
                totalAyahs = total,
                stopsAtSurahEnd = stopsAtSurahEnd,
            )
        }
    }

    fun togglePlayback() {
        if (audioPlayer.snapshot.value.isPlaying) {
            audioPlayer.pause()
        } else if (_currentSurahNumber.value != null) {
            startPlaybackService()
            audioPlayer.resume()
        }
        syncFromPlayer()
    }

    fun pause() {
        audioPlayer.pause()
        syncFromPlayer()
    }

    fun seek(progress: Double) {
        val duration = _duration.value
        if (duration > 0) {
            audioPlayer.seekTo(progress * duration)
            syncFromPlayer()
        }
    }

    fun playNext() {
        viewModelScope.launch {
            audioPlayer.playNext(totalAyahsForCurrentSurah = null)
        }
    }

    fun playPrevious() {
        audioPlayer.playPrevious()
    }

    fun downloadSelectedSurah(surahNumber: Int = _selectedSurahNumber.value) {
        viewModelScope.launch {
            _isDownloadingAudio.value = true
            _audioErrorMessage.value = null
            runCatching { audioRepository.downloadSurahAudio(surahNumber) }
                .onSuccess {
                    _downloadedSurahs.update { it + surahNumber }
                }
                .onFailure { error ->
                    _audioErrorMessage.value = error.message
                }
            _isDownloadingAudio.value = false
        }
    }

    fun isSurahDownloaded(surahNumber: Int): Boolean =
        _downloadedSurahs.value.contains(surahNumber)

    fun setSelectedSurah(surahNumber: Int) {
        _selectedSurahNumber.value = surahNumber
    }

    private fun syncFromPlayer() {
        audioPlayer.refreshSnapshot()
        applySnapshotFromPlayer()
    }

    private fun applySnapshotFromPlayer() {
        val snap = audioPlayer.snapshot.value
        _currentSurahNumber.value = snap.currentSurahNumber
        _currentAyahInSurah.value = snap.currentAyahInSurah
        _isPlaying.value = snap.isPlaying
        _isLoadingAudio.value = snap.isLoading
        _currentTime.value = snap.currentTimeSec
        _duration.value = snap.durationSec
        _audioErrorMessage.value = snap.errorMessage
        progressAnchor = ProgressAnchor(
            positionSec = snap.currentTimeSec,
            durationSec = snap.durationSec,
            wallClockMs = System.currentTimeMillis(),
        )
        _displayTime.value = snap.currentTimeSec

        snap.currentSurahNumber?.let { _selectedSurahNumber.value = it }
        refreshAyahPreview()
        updateProgressPolling()
    }

    private fun refreshAyahPreview() {
        val surah = _currentSurahNumber.value
        val ayah = _currentAyahInSurah.value
        if (surah == null || ayah == null) {
            _currentAyahArabicPreview.value = null
            lastPreviewKey = null
            previewJob?.cancel()
            return
        }
        val key = "$surah-$ayah"
        if (key == lastPreviewKey) return
        lastPreviewKey = key
        previewJob?.cancel()
        previewJob = viewModelScope.launch {
            val text = fetchQuranUseCase.executeAyah(surah, ayah)?.arabicText
            _currentAyahArabicPreview.value = text?.let { truncatedPreview(it) }
        }
    }

    private fun updateProgressPolling() {
        val hasActivePlayback = _currentSurahNumber.value != null
        if (hasActivePlayback && _isPlaying.value) {
            if (progressJob?.isActive != true) {
                progressJob = viewModelScope.launch {
                    while (isActive && _currentSurahNumber.value != null && _isPlaying.value) {
                        tickDisplayTime()
                        delay(200)
                    }
                }
            }
            if (syncJob?.isActive != true) {
                syncJob = viewModelScope.launch {
                    while (isActive && _currentSurahNumber.value != null && _isPlaying.value) {
                        syncFromPlayer()
                        delay(500)
                    }
                }
            }
        } else {
            progressJob?.cancel()
            progressJob = null
            syncJob?.cancel()
            syncJob = null
            _displayTime.value = _currentTime.value
        }
    }

    private fun tickDisplayTime() {
        val anchor = progressAnchor
        if (!_isPlaying.value || anchor.durationSec <= 0) {
            _displayTime.value = _currentTime.value
            return
        }
        val elapsedSec = (System.currentTimeMillis() - anchor.wallClockMs) / 1000.0
        val extrapolated = (anchor.positionSec + elapsedSec)
            .coerceIn(0.0, anchor.durationSec)
        _displayTime.value = extrapolated
    }

    private fun startPlaybackService() {
        try {
            val intent = Intent(appContext, QuranPlaybackService::class.java)
            ContextCompat.startForegroundService(appContext, intent)
        } catch (error: Exception) {
            Log.e(TAG, "Could not start playback foreground service", error)
        }
    }

    override fun onCleared() {
        previewJob?.cancel()
        progressJob?.cancel()
        syncJob?.cancel()
        super.onCleared()
    }

    companion object {
        private const val TAG = "SharedAudioViewModel"

        private fun truncatedPreview(text: String, maxLength: Int = 72): String {
            val trimmed = text.trim()
            return if (trimmed.length <= maxLength) trimmed else trimmed.take(maxLength) + "…"
        }
    }
}
