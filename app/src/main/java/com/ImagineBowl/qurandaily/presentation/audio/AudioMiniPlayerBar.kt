package com.imaginebowl.qurandaily.presentation.audio

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.imaginebowl.qurandaily.core.domain.model.Surah
import com.imaginebowl.qurandaily.ui.theme.Accent
import com.imaginebowl.qurandaily.ui.theme.AppDimensions

@Composable
fun AudioMiniPlayerBar(
    sharedAudioViewModel: SharedAudioViewModel,
    surahs: List<Surah>,
    onOpenFullPlayer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentSurah by sharedAudioViewModel.currentSurahNumber.collectAsStateWithLifecycle()
    val currentAyah by sharedAudioViewModel.currentAyahInSurah.collectAsStateWithLifecycle()
    val isPlaying by sharedAudioViewModel.isPlaying.collectAsStateWithLifecycle()
    val isLoading by sharedAudioViewModel.isLoadingAudio.collectAsStateWithLifecycle()
    val displayTime by sharedAudioViewModel.displayTime.collectAsStateWithLifecycle()
    val duration by sharedAudioViewModel.duration.collectAsStateWithLifecycle()

    val surah = surahs.firstOrNull { it.number == currentSurah } ?: return

    val progress = if (duration > 0) {
        (displayTime / duration).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpenFullPlayer),
        ) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = Accent,
                trackColor = Accent.copy(alpha = 0.2f),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                    Text(text = surah.englishName, fontWeight = FontWeight.SemiBold)
                    val ayah = currentAyah
                    Text(
                        text = when {
                            isLoading -> "Loading audio..."
                            ayah != null -> "Ayah $ayah"
                            else -> surah.englishNameTranslation
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Accent,
                    )
                }
                IconButton(
                    onClick = { sharedAudioViewModel.playPrevious() },
                    modifier = Modifier.sizeIn(
                        minWidth = AppDimensions.minimumTapSize,
                        minHeight = AppDimensions.minimumTapSize,
                    ),
                ) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = Accent)
                }
                IconButton(
                    onClick = { sharedAudioViewModel.togglePlayback() },
                    modifier = Modifier.sizeIn(
                        minWidth = AppDimensions.minimumTapSize,
                        minHeight = AppDimensions.minimumTapSize,
                    ),
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Accent,
                    )
                }
                IconButton(
                    onClick = { sharedAudioViewModel.playNext() },
                    modifier = Modifier.sizeIn(
                        minWidth = AppDimensions.minimumTapSize,
                        minHeight = AppDimensions.minimumTapSize,
                    ),
                ) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Accent)
                }
            }
        }
    }
}
