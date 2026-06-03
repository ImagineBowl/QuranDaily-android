package com.imaginebowl.qurandaily.presentation.listen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.imaginebowl.qurandaily.core.domain.model.RecentListen
import com.imaginebowl.qurandaily.core.domain.model.Surah
import com.imaginebowl.qurandaily.presentation.audio.AudioPlayerSheet
import com.imaginebowl.qurandaily.presentation.audio.SharedAudioViewModel
import com.imaginebowl.qurandaily.ui.components.LargePrimaryButton
import com.imaginebowl.qurandaily.ui.layout.TabContentWindowInsets
import com.imaginebowl.qurandaily.ui.theme.Accent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListenScreen(
    viewModel: ListenViewModel,
    sharedAudioViewModel: SharedAudioViewModel,
    onReadAndListen: (surahNumber: Int, ayahNumber: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAudioSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    if (showAudioSheet) {
        AudioPlayerSheet(
            sharedAudioViewModel = sharedAudioViewModel,
            surahs = uiState.surahs,
            onDismiss = { showAudioSheet = false },
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = TabContentWindowInsets,
        topBar = {
            TopAppBar(
                title = { Text("Listen") },
                actions = {
                    IconButton(onClick = { showAudioSheet = true }) {
                        Icon(Icons.Default.Headphones, contentDescription = "Open audio player")
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
                        text = "Loading surahs...",
                        modifier = Modifier.padding(top = 12.dp),
                        fontSize = 18.sp,
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
                AyahReferencePicker(
                    surahs = uiState.surahs,
                    recentListens = uiState.recentListens,
                    onReadAndListen = onReadAndListen,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState()),
                )
            }
        }
    }
}

@Composable
private fun AyahReferencePicker(
    surahs: List<Surah>,
    recentListens: List<RecentListen>,
    onReadAndListen: (surahNumber: Int, ayahNumber: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedSurahNumber by remember(surahs) {
        mutableIntStateOf(surahs.firstOrNull()?.number ?: 1)
    }
    var selectedAyahNumber by remember { mutableIntStateOf(1) }
    var showSurahBrowser by remember { mutableStateOf(false) }
    var showAyahPicker by remember { mutableStateOf(false) }

    val selectedSurah = surahs.firstOrNull { it.number == selectedSurahNumber }

    LaunchedEffect(surahs) {
        if (surahs.isNotEmpty() && surahs.none { it.number == selectedSurahNumber }) {
            selectedSurahNumber = surahs.first().number
        }
        selectedSurah?.let { surah ->
            selectedAyahNumber = selectedAyahNumber.coerceIn(1, surah.numberOfAyahs)
        }
    }

    LaunchedEffect(selectedSurahNumber) {
        selectedAyahNumber = 1
    }

    if (showSurahBrowser) {
        AyahSurahBrowserSheet(
            surahs = surahs,
            selectedSurahNumber = selectedSurahNumber,
            onSelect = { selectedSurahNumber = it },
            onDismiss = { showSurahBrowser = false },
        )
    }

    if (showAyahPicker && selectedSurah != null) {
        AyahNumberPickerSheet(
            numberOfAyahs = selectedSurah.numberOfAyahs,
            selectedAyahNumber = selectedAyahNumber,
            onSelect = { selectedAyahNumber = it },
            onDismiss = { showAyahPicker = false },
        )
    }

    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        if (selectedSurah != null) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    PickerSelectionRow(
                        title = "Surah",
                        value = "${selectedSurah.number} · ${selectedSurah.englishName}",
                        onClick = { showSurahBrowser = true },
                    )
                    PickerSelectionRow(
                        title = "Ayah",
                        value = selectedAyahNumber.toString(),
                        onClick = { showAyahPicker = true },
                    )
                    LargePrimaryButton(
                        text = "Read & Listen",
                        onClick = {
                            onReadAndListen(selectedSurahNumber, selectedAyahNumber)
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    )
                }
            }
            Text(
                text = "${selectedSurah.englishNameTranslation} · ${selectedSurah.numberOfAyahs} ayahs",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 20.dp),
            )
        }

        if (recentListens.isNotEmpty()) {
            Text(
                text = "Recent",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            recentListens.forEach { item ->
                RecentListenRow(
                    item = item,
                    onClick = { onReadAndListen(item.surahNumber, item.ayahNumber) },
                )
            }
        }
    }
}

@Composable
private fun PickerSelectionRow(
    title: String,
    value: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, fontSize = 17.sp)
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 17.sp,
            maxLines = 1,
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun RecentListenRow(
    item: RecentListen,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.surahName, fontSize = 17.sp)
            Text(
                text = "Ayah ${item.ayahNumber}",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            imageVector = Icons.Default.PlayCircle,
            contentDescription = null,
            tint = Accent,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}
