package com.imaginebowl.qurandaily.presentation.listen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imaginebowl.qurandaily.core.domain.model.Surah
import com.imaginebowl.qurandaily.ui.theme.Accent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AyahSurahBrowserSheet(
    surahs: List<Surah>,
    selectedSurahNumber: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var filter by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Select Surah", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = onDismiss) { Text("Close") }
            }
            OutlinedTextField(
                value = filter,
                onValueChange = { filter = it },
                placeholder = { Text("Filter surahs") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
            )
            val trimmed = filter.trim()
            val normalized = trimmed.lowercase()
            val filtered = if (trimmed.isEmpty()) {
                surahs
            } else {
                surahs.filter { surah ->
                    surah.number.toString() == trimmed ||
                        surah.englishName.lowercase().contains(normalized) ||
                        surah.englishNameTranslation.lowercase().contains(normalized) ||
                        surah.name.contains(trimmed)
                }
            }
            LazyColumn {
                items(filtered, key = { it.number }) { surah ->
                    SurahPickerSelectionRow(
                        surah = surah,
                        isSelected = surah.number == selectedSurahNumber,
                        onClick = {
                            onSelect(surah.number)
                            onDismiss()
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AyahNumberPickerSheet(
    numberOfAyahs: Int,
    selectedAyahNumber: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var filter by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Select Ayah", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = onDismiss) { Text("Close") }
            }
            OutlinedTextField(
                value = filter,
                onValueChange = { filter = it },
                placeholder = { Text("Find ayah number") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
            )
            val trimmed = filter.trim()
            val ayahs = if (trimmed.isEmpty()) {
                (1..numberOfAyahs).toList()
            } else {
                (1..numberOfAyahs).filter { it.toString().startsWith(trimmed) }
            }
            LazyColumn {
                items(ayahs, key = { it }) { ayah ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelect(ayah)
                                onDismiss()
                            }
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Ayah $ayah", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.weight(1f))
                        if (ayah == selectedAyahNumber) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Accent,
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SurahPickerSelectionRow(
    surah: Surah,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(36.dp),
            shape = CircleShape,
            color = if (isSelected) Accent.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Text(
                text = surah.number.toString(),
                modifier = Modifier.padding(8.dp),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp),
        ) {
            Text(text = surah.englishName, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
            Text(
                text = surah.englishNameTranslation,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = surah.name,
            fontSize = 20.sp,
            textAlign = TextAlign.End,
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Accent,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}
