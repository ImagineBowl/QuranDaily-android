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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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

    val surah = surahs.firstOrNull { it.number == currentSurah } ?: return

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpenFullPlayer)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = surah.englishName, fontWeight = FontWeight.SemiBold)
                val ayah = currentAyah
                if (ayah != null) {
                    Text(
                        text = "Ayah $ayah",
                        style = MaterialTheme.typography.bodySmall,
                        color = Accent,
                    )
                }
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
        }
    }
}
