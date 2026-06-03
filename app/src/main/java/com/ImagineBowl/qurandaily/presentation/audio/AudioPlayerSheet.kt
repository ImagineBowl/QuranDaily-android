package com.imaginebowl.qurandaily.presentation.audio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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

/**
 * Full audio player UI shell. ExoPlayer, scrubber, and download controls land in M7.
 */
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

    val surah = surahs.firstOrNull { it.number == currentSurahNumber }

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
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp),
            ) {
                Text(
                    text = surah?.englishName ?: "Not playing",
                    style = MaterialTheme.typography.headlineSmall,
                )
                surah?.let {
                    Text(
                        text = it.englishNameTranslation,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                currentAyah?.let { ayah ->
                    Text(
                        text = "Ayah $ayah of ${surah?.numberOfAyahs ?: "?"}",
                        color = Accent,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth(),
            ) {
                IconButton(
                    onClick = { sharedAudioViewModel.togglePlayback() },
                    enabled = currentSurahNumber != null,
                    modifier = Modifier.sizeIn(
                        minWidth = 76.dp,
                        minHeight = 76.dp,
                    ),
                ) {
                    Surface(
                        shape = RoundedCornerShape(38.dp),
                        color = Accent,
                        modifier = Modifier.sizeIn(minWidth = 76.dp, minHeight = 76.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color.White,
                            )
                        }
                    }
                }
            }

            Text(
                text = "Streaming and offline download controls — M7",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
            )
        }
    }
}
