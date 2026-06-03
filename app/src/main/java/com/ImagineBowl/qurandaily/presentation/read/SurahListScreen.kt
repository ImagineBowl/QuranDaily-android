package com.imaginebowl.qurandaily.presentation.read

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.imaginebowl.qurandaily.core.domain.model.Juz
import com.imaginebowl.qurandaily.core.domain.model.Surah
import com.imaginebowl.qurandaily.presentation.audio.SharedAudioViewModel
import com.imaginebowl.qurandaily.ui.layout.TabContentWindowInsets
import com.imaginebowl.qurandaily.ui.theme.Accent
import com.imaginebowl.qurandaily.ui.theme.AppDimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahListScreen(
    viewModel: QuranViewModel,
    sharedAudioViewModel: SharedAudioViewModel,
    onOpenSurah: (surahNumber: Int, ayahNumber: Int, autoPlay: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentSurah by sharedAudioViewModel.currentSurahNumber.collectAsStateWithLifecycle()
    val currentAyah by sharedAudioViewModel.currentAyahInSurah.collectAsStateWithLifecycle()
    var showJuzPicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    LaunchedEffect(currentSurah, currentAyah) {
        val surah = currentSurah ?: return@LaunchedEffect
        val ayah = currentAyah ?: return@LaunchedEffect
        val saved = uiState.readingPosition
        if (saved.surahNumber == surah && saved.hasSavedPosition) {
            viewModel.updateReadingPosition(surah, ayah)
        }
    }

    if (showJuzPicker) {
        JuzPickerSheet(
            juzs = uiState.juzs,
            surahs = uiState.surahs,
            onDismiss = { showJuzPicker = false },
            onSelect = { juz ->
                showJuzPicker = false
                onOpenSurah(juz.startSurah, juz.startAyah, false)
            },
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = TabContentWindowInsets,
        topBar = {
            TopAppBar(
                title = { Text("QuranDaily") },
                actions = {
                    IconButton(onClick = { showJuzPicker = true }) {
                        Icon(Icons.Default.List, contentDescription = "Jump to Juz")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        when {
            uiState.isLoading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator(color = Accent)
                    Text(
                        text = "Loading Quran...",
                        modifier = Modifier.padding(top = 12.dp),
                        fontSize = 20.sp,
                    )
                }
            }
            uiState.errorMessage != null -> {
                Text(
                    text = uiState.errorMessage ?: "",
                    modifier = Modifier.padding(padding).padding(24.dp),
                    color = MaterialTheme.colorScheme.error,
                )
            }
            else -> {
                val resumeSurah = uiState.surahs.firstOrNull {
                    it.number == uiState.readingPosition.surahNumber
                }.takeIf { uiState.readingPosition.hasSavedPosition }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (resumeSurah != null) {
                        item(key = "continue") {
                            ContinueReadingCard(
                                surah = resumeSurah,
                                ayahNumber = uiState.readingPosition.ayahNumber,
                                onResume = {
                                    onOpenSurah(
                                        resumeSurah.number,
                                        uiState.readingPosition.ayahNumber,
                                        false,
                                    )
                                },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            )
                        }
                    }
                    item {
                        Text(
                            text = "All Surahs",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                    items(uiState.surahs, key = { it.number }) { surah ->
                        SurahRow(
                            surah = surah,
                            onClick = { onOpenSurah(surah.number, 1, false) },
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContinueReadingCard(
    surah: Surah,
    ayahNumber: Int,
    onResume: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppDimensions.cardCornerRadius))
            .clickable(onClick = onResume),
        color = Accent.copy(alpha = 0.15f),
        shape = RoundedCornerShape(AppDimensions.cardCornerRadius),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = surah.name,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = surah.englishName,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "Ayah $ayahNumber of ${surah.numberOfAyahs}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LinearProgressIndicator(
                progress = { (ayahNumber.toFloat() / surah.numberOfAyahs).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                color = Accent,
            )
            Surface(
                color = Accent,
                shape = RoundedCornerShape(20.dp),
            ) {
                Text(
                    text = "Resume",
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun SurahRow(
    surah: Surah,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = Accent.copy(alpha = 0.15f),
        ) {
            Text(
                text = surah.number.toString(),
                modifier = Modifier.padding(10.dp),
                color = Accent,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
        ) {
            Text(text = surah.englishName, fontWeight = FontWeight.SemiBold)
            Text(
                text = "${surah.englishNameTranslation} · ${surah.numberOfAyahs} ayahs",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = surah.name,
            fontSize = 18.sp,
            textAlign = TextAlign.End,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JuzPickerSheet(
    juzs: List<Juz>,
    surahs: List<Surah>,
    onDismiss: () -> Unit,
    onSelect: (Juz) -> Unit,
) {
    androidx.compose.material3.ModalBottomSheet(onDismissRequest = onDismiss) {
        LazyColumn(modifier = Modifier.padding(bottom = 24.dp)) {
            items(juzs, key = { it.number }) { juz ->
                val surahName = surahs.firstOrNull { it.number == juz.startSurah }?.englishName
                    ?: "Surah ${juz.startSurah}"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(juz) }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Juz ${juz.number}", fontWeight = FontWeight.SemiBold)
                    Text(
                        text = "$surahName ${juz.startSurah}:${juz.startAyah}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
