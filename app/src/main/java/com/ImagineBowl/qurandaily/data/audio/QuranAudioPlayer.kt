package com.imaginebowl.qurandaily.data.audio

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.imaginebowl.qurandaily.core.domain.repository.AudioRepository
import com.imaginebowl.qurandaily.core.domain.usecase.FetchQuranUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class QuranPlaybackSnapshot(
    val currentSurahNumber: Int? = null,
    val currentAyahInSurah: Int? = null,
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val currentTimeSec: Double = 0.0,
    val durationSec: Double = 0.0,
    val errorMessage: String? = null,
)

/**
 * Single shared ExoPlayer instance — mirrors iOS [AudioPlayerService].
 */
class QuranAudioPlayer(
    context: Context,
    private val audioRepository: AudioRepository,
    private val fetchQuranUseCase: FetchQuranUseCase,
) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    val player: ExoPlayer = ExoPlayer.Builder(appContext).build()

    private var ayahSequenceEnd: Int? = null
    private var stopsAtSurahEnd: Boolean = false

    private val _snapshot = MutableStateFlow(QuranPlaybackSnapshot())
    val snapshot: StateFlow<QuranPlaybackSnapshot> = _snapshot.asStateFlow()

    var onPlaybackUpdate: (() -> Unit)? = null

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_ENDED) {
                scope.launch { handleItemEnded() }
            }
            syncFromPlayer()
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            syncFromPlayer()
        }
    }

    init {
        player.addListener(playerListener)
    }

    fun pause() {
        player.pause()
        syncFromPlayer()
    }

    fun resume() {
        player.play()
        syncFromPlayer()
    }

    fun seekTo(timeSec: Double) {
        val ms = (timeSec * 1000).toLong().coerceAtLeast(0L)
        player.seekTo(ms)
        syncFromPlayer()
    }

    fun stop() {
        player.stop()
        player.clearMediaItems()
        ayahSequenceEnd = null
        stopsAtSurahEnd = false
        _snapshot.value = QuranPlaybackSnapshot()
        notifyUpdate()
    }

    fun playSurahAyah(
        surahNumber: Int,
        fromAyah: Int,
        totalAyahs: Int,
        stopsAtSurahEnd: Boolean = true,
    ) {
        scope.launch {
            _snapshot.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                this@QuranAudioPlayer.stopsAtSurahEnd = stopsAtSurahEnd
                val uri = withContext(Dispatchers.IO) {
                    audioRepository.ayahStreamingUri(surahNumber, fromAyah)
                }
                startPlayback(
                    uri = uri.toString(),
                    surahNumber = surahNumber,
                    ayahInSurah = fromAyah,
                    sequenceEnd = totalAyahs,
                )
            } catch (e: Exception) {
                _snapshot.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "Playback failed")
                }
                notifyUpdate()
            }
        }
    }

    fun playNext(totalAyahsForCurrentSurah: Int?) {
        scope.launch {
            val state = _snapshot.value
            val surah = state.currentSurahNumber ?: return@launch
            val ayah = state.currentAyahInSurah ?: return@launch
            val end = ayahSequenceEnd
            if (end != null && ayah < end) {
                playSurahAyah(surah, ayah + 1, end, stopsAtSurahEnd)
                return@launch
            }
            val nextSurah = (surah + 1).coerceAtMost(114)
            playFullSurah(nextSurah, stopsAtSurahEnd = false)
        }
    }

    fun playPrevious() {
        scope.launch {
            val state = _snapshot.value
            val surah = state.currentSurahNumber ?: return@launch
            val ayah = state.currentAyahInSurah ?: return@launch
            val end = ayahSequenceEnd
            if (end != null && ayah > 1) {
                playSurahAyah(surah, ayah - 1, end, stopsAtSurahEnd)
                return@launch
            }
            val previousSurah = (surah - 1).coerceAtLeast(1)
            playFullSurah(previousSurah, stopsAtSurahEnd = false)
        }
    }

    fun playFullSurah(surahNumber: Int, stopsAtSurahEnd: Boolean = false) {
        scope.launch {
            _snapshot.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                this@QuranAudioPlayer.stopsAtSurahEnd = stopsAtSurahEnd
                val uri = withContext(Dispatchers.IO) {
                    audioRepository.playbackUri(surahNumber)
                }
                startPlayback(
                    uri = uri.toString(),
                    surahNumber = surahNumber,
                    ayahInSurah = null,
                    sequenceEnd = null,
                )
            } catch (e: Exception) {
                _snapshot.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "Playback failed")
                }
                notifyUpdate()
            }
        }
    }

    fun release() {
        player.removeListener(playerListener)
        player.release()
        scope.cancel()
    }

    private suspend fun handleItemEnded() {
        val state = _snapshot.value
        val end = ayahSequenceEnd
        val surah = state.currentSurahNumber
        val ayah = state.currentAyahInSurah
        if (end != null && surah != null && ayah != null && ayah < end) {
            playSurahAyah(surah, ayah + 1, end, stopsAtSurahEnd)
            return
        }
        if (stopsAtSurahEnd) {
            player.pause()
            player.clearMediaItems()
            syncFromPlayer()
            return
        }
        playNext(null)
    }

    private fun startPlayback(
        uri: String,
        surahNumber: Int,
        ayahInSurah: Int?,
        sequenceEnd: Int?,
    ) {
        player.stop()
        player.setMediaItem(MediaItem.fromUri(uri))
        player.prepare()
        ayahSequenceEnd = sequenceEnd
        _snapshot.update {
            it.copy(
                currentSurahNumber = surahNumber,
                currentAyahInSurah = ayahInSurah,
                isLoading = false,
                errorMessage = null,
            )
        }
        player.play()
        syncFromPlayer()
    }

    private fun syncFromPlayer() {
        val durationMs = player.duration.takeIf { it > 0 } ?: 0L
        val positionMs = player.currentPosition.coerceAtLeast(0L)
        _snapshot.update { current ->
            current.copy(
                isPlaying = player.isPlaying,
                isLoading = player.playbackState == Player.STATE_BUFFERING,
                currentTimeSec = positionMs / 1000.0,
                durationSec = durationMs / 1000.0,
            )
        }
        notifyUpdate()
    }

    private fun notifyUpdate() {
        onPlaybackUpdate?.invoke()
    }
}
