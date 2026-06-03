package com.imaginebowl.qurandaily.presentation.surah

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.imaginebowl.qurandaily.presentation.audio.SharedAudioViewModel
import com.imaginebowl.qurandaily.ui.layout.TabContentWindowInsets
import com.imaginebowl.qurandaily.ui.theme.Accent
import com.imaginebowl.qurandaily.ui.theme.AppDimensions
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahReadListenScreen(
    surahNumber: Int,
    ayahNumber: Int,
    autoPlay: Boolean,
    sharedAudioViewModel: SharedAudioViewModel,
    detailViewModel: SurahDetailViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    tracksReadingPosition: Boolean = true,
    tracksRecentListens: Boolean = false,
    onRecordRecentListen: ((surahNumber: Int, surahName: String, ayahNumber: Int) -> Unit)? = null,
) {
    val uiState by detailViewModel.uiState.collectAsStateWithLifecycle()
    val currentSurah by sharedAudioViewModel.currentSurahNumber.collectAsStateWithLifecycle()
    val currentAyah by sharedAudioViewModel.currentAyahInSurah.collectAsStateWithLifecycle()
    val isPlaying by sharedAudioViewModel.isPlaying.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var visibleAyahs by remember { mutableStateOf(setOf<Int>()) }
    var didScrollToInitial by remember { mutableStateOf(false) }
    var trackedAyah by remember { mutableIntStateOf(ayahNumber) }
    fun recordPlayback(fromAyah: Int) {
        if (!tracksRecentListens) return
        val surah = uiState.surah
        onRecordRecentListen?.invoke(surah.number, surah.englishName, fromAyah)
    }

    fun playAtAyah(fromAyah: Int) {
        val surah = uiState.surah
        sharedAudioViewModel.playSurah(
            surahNumber = surah.number,
            fromAyah = fromAyah,
            totalAyahs = surah.numberOfAyahs,
            stopsAtSurahEnd = true,
        )
        recordPlayback(fromAyah)
    }

    val highlightedAyah =
        if (currentSurah == surahNumber) currentAyah else null

    val showJumpToPlaying = isPlaying &&
        highlightedAyah != null &&
        !visibleAyahs.contains(highlightedAyah)

    LaunchedEffect(Unit) {
        detailViewModel.load()
        if (autoPlay) {
            playAtAyah(ayahNumber)
        }
    }

    LaunchedEffect(uiState.ayahs.size, uiState.initialAyah) {
        if (uiState.ayahs.isNotEmpty() && !didScrollToInitial) {
            val index = uiState.ayahs.indexOfFirst { it.numberInSurah == uiState.initialAyah }
            if (index >= 0) {
                repeat(8) { attempt ->
                    listState.scrollToItem(index)
                    if (listState.layoutInfo.visibleItemsInfo.any { it.index == index }) {
                        didScrollToInitial = true
                        return@LaunchedEffect
                    }
                    delay(50)
                }
                didScrollToInitial = true
            }
        }
    }

    LaunchedEffect(highlightedAyah, isPlaying) {
        val target = highlightedAyah ?: return@LaunchedEffect
        if (tracksReadingPosition) {
            trackedAyah = if (isPlaying && highlightedAyah != null) target else trackedAyah
            detailViewModel.saveReadingPosition(target)
        }
        if (visibleAyahs.contains(target)) {
            val index = uiState.ayahs.indexOfFirst { it.numberInSurah == target }
            if (index >= 0) {
                listState.animateScrollToItem(index)
            }
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo.mapNotNull { info ->
                uiState.ayahs.getOrNull(info.index)?.numberInSurah
            }.toSet()
        }
            .distinctUntilChanged()
            .collect { visibleAyahs = it }
    }

    LaunchedEffect(visibleAyahs, isPlaying, highlightedAyah) {
        if (!tracksReadingPosition || isPlaying) return@LaunchedEffect
        val top = visibleAyahs.minOrNull() ?: return@LaunchedEffect
        trackedAyah = top
        detailViewModel.saveReadingPosition(top)
    }

    DisposableEffect(Unit) {
        onDispose {
            if (tracksReadingPosition) {
                detailViewModel.saveReadingPosition(trackedAyah)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = TabContentWindowInsets,
        topBar = {
            TopAppBar(
                title = { Text(uiState.surah.englishName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (currentSurah == surahNumber) {
                                sharedAudioViewModel.togglePlayback()
                            } else {
                                playAtAyah(ayahNumber)
                            }
                        },
                    ) {
                        Icon(
                            imageVector = if (isPlaying && currentSurah == surahNumber) {
                                Icons.Default.Pause
                            } else {
                                Icons.Default.PlayArrow
                            },
                            contentDescription = "Play or pause",
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            if (showJumpToPlaying && highlightedAyah != null) {
                FloatingActionButton(
                    onClick = {
                        scope.launch {
                            val index = uiState.ayahs.indexOfFirst { it.numberInSurah == highlightedAyah }
                            if (index >= 0) listState.animateScrollToItem(index)
                        }
                    },
                    containerColor = Accent,
                    shape = RoundedCornerShape(24.dp),
                ) {
                    Text("Now Playing", modifier = Modifier.padding(horizontal = 12.dp))
                }
            }
        },
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Accent)
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
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(AppDimensions.sectionSpacing),
                ) {
                    items(uiState.ayahs, key = { it.numberInSurah }) { ayah ->
                        AyahCard(
                            ayah = ayah,
                            fontSize = uiState.settings.fontSize,
                            arabicFont = uiState.settings.arabicFont,
                            urduFont = uiState.settings.urduFont,
                            isBookmarked = detailViewModel.isBookmarked(ayah),
                            isHighlighted = highlightedAyah == ayah.numberInSurah,
                            onBookmark = { detailViewModel.toggleBookmark(ayah) },
                            onAyahTap = { playAtAyah(ayah.numberInSurah) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}
