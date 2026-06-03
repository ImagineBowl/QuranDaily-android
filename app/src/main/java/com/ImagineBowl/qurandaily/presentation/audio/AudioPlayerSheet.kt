package com.imaginebowl.qurandaily.presentation.audio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.imaginebowl.qurandaily.core.domain.model.Surah
import com.imaginebowl.qurandaily.ui.theme.Accent
import com.imaginebowl.qurandaily.ui.theme.AppDimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioPlayerSheet(
    sharedAudioViewModel: SharedAudioViewModel,
    surahs: List<Surah>,
    onDismiss: () -> Unit,
) {
    val currentSurahNumber by sharedAudioViewModel.currentSurahNumber.collectAsStateWithLifecycle()
    val currentAyah by sharedAudioViewModel.currentAyahInSurah.collectAsStateWithLifecycle()
    val isPlaying by sharedAudioViewModel.isPlaying.collectAsStateWithLifecycle()
    val isLoading by sharedAudioViewModel.isLoadingAudio.collectAsStateWithLifecycle()
    val currentTime by sharedAudioViewModel.currentTime.collectAsStateWithLifecycle()
    val duration by sharedAudioViewModel.duration.collectAsStateWithLifecycle()
    val arabicPreview by sharedAudioViewModel.currentAyahArabicPreview.collectAsStateWithLifecycle()
    val selectedSurah by sharedAudioViewModel.selectedSurahNumber.collectAsStateWithLifecycle()
    val downloadedSurahs by sharedAudioViewModel.downloadedSurahs.collectAsStateWithLifecycle()
    val isDownloading by sharedAudioViewModel.isDownloadingAudio.collectAsStateWithLifecycle()
    val errorMessage by sharedAudioViewModel.audioErrorMessage.collectAsStateWithLifecycle()

    val displaySurahNumber = currentSurahNumber ?: selectedSurah
    val surah = surahs.firstOrNull { it.number == displaySurahNumber }
    val isDownloaded = downloadedSurahs.contains(displaySurahNumber)

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Close player")
                }
                Text(
                    text = "NOW PLAYING",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Box(modifier = Modifier.sizeIn(minWidth = AppDimensions.minimumTapSize))
            }

            Box(
                modifier = Modifier
                    .padding(horizontal = 28.dp, vertical = 16.dp)
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Accent, Accent.copy(alpha = 0.7f)),
                        ),
                        shape = RoundedCornerShape(28.dp),
                    )
                    .padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = surah?.name ?: "—",
                        fontSize = 36.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = surah?.englishName ?: "Select a surah to play",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.9f),
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp),
            ) {
                Text(text = surah?.englishName ?: "Not playing", style = MaterialTheme.typography.headlineSmall)
                surah?.let {
                    Text(text = it.englishNameTranslation, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                arabicPreview?.let { preview ->
                    Text(
                        text = preview,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp),
                        maxLines = 3,
                    )
                }
                currentAyah?.let { ayah ->
                    Text(
                        text = "Ayah $ayah of ${surah?.numberOfAyahs ?: "?"}",
                        color = Accent,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }

            if (duration > 0) {
                val progress = (currentTime / duration).toFloat().coerceIn(0f, 1f)
                Slider(
                    value = progress,
                    onValueChange = { sharedAudioViewModel.seek(it.toDouble()) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp),
                ) {
                    Text(formatPlaybackTime(currentTime), style = MaterialTheme.typography.bodySmall)
                    Box(modifier = Modifier.weight(1f))
                    Text(formatPlaybackTime(duration), style = MaterialTheme.typography.bodySmall)
                }
            } else {
                Box(modifier = Modifier.height(44.dp))
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { sharedAudioViewModel.playPrevious() }) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = Accent)
                }
                IconButton(
                    onClick = { sharedAudioViewModel.togglePlayback() },
                    enabled = currentSurahNumber != null || !isLoading,
                    modifier = Modifier.sizeIn(minWidth = 76.dp, minHeight = 76.dp),
                ) {
                    Surface(
                        shape = RoundedCornerShape(38.dp),
                        color = Accent,
                        modifier = Modifier.sizeIn(minWidth = 76.dp, minHeight = 76.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.padding(16.dp),
                                )
                            } else {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = Color.White,
                                )
                            }
                        }
                    }
                }
                IconButton(onClick = { sharedAudioViewModel.playNext() }) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Accent)
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp),
                shape = RoundedCornerShape(AppDimensions.cardCornerRadius),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (isDownloaded) "Available offline" else "Streaming online",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    TextButton(
                        onClick = { sharedAudioViewModel.downloadSelectedSurah(displaySurahNumber) },
                        enabled = !isDownloaded && !isDownloading,
                    ) {
                        if (isDownloading) {
                            CircularProgressIndicator(modifier = Modifier.padding(4.dp))
                        } else {
                            Text(if (isDownloaded) "Downloaded" else "Download")
                        }
                    }
                }
            }

            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp, start = 28.dp, end = 28.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

private fun formatPlaybackTime(seconds: Double): String {
    val total = seconds.toInt().coerceAtLeast(0)
    val minutes = total / 60
    val secs = total % 60
    return "%d:%02d".format(minutes, secs)
}
