package com.imaginebowl.qurandaily.presentation.main

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.imaginebowl.qurandaily.di.AppContainer
import com.imaginebowl.qurandaily.core.domain.model.AppThemeMode
import com.imaginebowl.qurandaily.di.QuranViewModelFactory
import com.imaginebowl.qurandaily.di.SettingsViewModelFactory
import com.imaginebowl.qurandaily.di.SharedAudioViewModelFactory
import com.imaginebowl.qurandaily.presentation.audio.AudioMiniPlayerBar
import com.imaginebowl.qurandaily.presentation.audio.AudioPlayerSheet
import com.imaginebowl.qurandaily.presentation.audio.SharedAudioViewModel
import com.imaginebowl.qurandaily.presentation.bookmarks.BookmarksTabNavHost
import com.imaginebowl.qurandaily.presentation.read.QuranViewModel
import com.imaginebowl.qurandaily.presentation.read.ReadTabNavHost
import com.imaginebowl.qurandaily.presentation.settings.SettingsScreen
import com.imaginebowl.qurandaily.presentation.settings.SettingsViewModel
import com.imaginebowl.qurandaily.presentation.listen.ListenTabNavHost
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private enum class MainTab(val label: String) {
    Read("Read"),
    Listen("Listen"),
    Bookmarks("Bookmarks"),
    Settings("Settings"),
}

@Composable
fun MainTabScreen(
    container: AppContainer,
    onThemeChanged: (AppThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = MainTab.entries
    val activity = LocalContext.current as ComponentActivity
    val sharedAudioViewModel: SharedAudioViewModel = viewModel(
        viewModelStoreOwner = activity,
        factory = SharedAudioViewModelFactory(container),
    )
    val settingsViewModel: SettingsViewModel = viewModel(
        viewModelStoreOwner = activity,
        factory = SettingsViewModelFactory(container),
    )

    val tabChromeViewModel: TabChromeViewModel = viewModel(viewModelStoreOwner = activity)
    val hideBottomBar by tabChromeViewModel.hideBottomBar.collectAsStateWithLifecycle()
    val quranViewModel: QuranViewModel = viewModel(
        viewModelStoreOwner = activity,
        factory = QuranViewModelFactory(container),
    )
    val quranState by quranViewModel.uiState.collectAsStateWithLifecycle()
    val currentSurah by sharedAudioViewModel.currentSurahNumber.collectAsStateWithLifecycle()
    val isLoadingAudio by sharedAudioViewModel.isLoadingAudio.collectAsStateWithLifecycle()
    var showAudioSheet by remember { mutableStateOf(false) }

    val showMiniPlayer =
        (currentSurah != null || isLoadingAudio) && quranState.surahs.isNotEmpty()

    LaunchedEffect(onThemeChanged) {
        settingsViewModel.onThemeChanged = onThemeChanged
    }

    LaunchedEffect(Unit) {
        if (quranState.surahs.isEmpty()) {
            quranViewModel.load()
        }
    }

    LaunchedEffect(selectedTab) {
        tabChromeViewModel.setHideBottomBar(false)
    }

    if (showAudioSheet && quranState.surahs.isNotEmpty()) {
        AudioPlayerSheet(
            sharedAudioViewModel = sharedAudioViewModel,
            surahs = quranState.surahs,
            onDismiss = { showAudioSheet = false },
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(WindowInsets.navigationBars),
        bottomBar = {
            Column(modifier = Modifier.navigationBarsPadding()) {
                if (showMiniPlayer) {
                    AudioMiniPlayerBar(
                        sharedAudioViewModel = sharedAudioViewModel,
                        surahs = quranState.surahs,
                        onOpenFullPlayer = { showAudioSheet = true },
                    )
                }
                if (!hideBottomBar) {
                    NavigationBar(
                        windowInsets = WindowInsets(0),
                    ) {
                        tabs.forEachIndexed { index, tab ->
                            NavigationBarItem(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                icon = {
                                    Icon(
                                        imageVector = when (tab) {
                                            MainTab.Read -> Icons.Default.Book
                                            MainTab.Listen -> Icons.Default.Headphones
                                            MainTab.Bookmarks -> Icons.Default.Bookmark
                                            MainTab.Settings -> Icons.Default.Settings
                                        },
                                        contentDescription = tab.label,
                                    )
                                },
                                label = { Text(tab.label) },
                            )
                        }
                    }
                }
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (tabs[selectedTab]) {
                MainTab.Read -> ReadTabNavHost(
                    container = container,
                    sharedAudioViewModel = sharedAudioViewModel,
                    tabChromeViewModel = tabChromeViewModel,
                    modifier = Modifier.fillMaxSize(),
                )
                MainTab.Listen -> ListenTabNavHost(
                    container = container,
                    sharedAudioViewModel = sharedAudioViewModel,
                    tabChromeViewModel = tabChromeViewModel,
                    modifier = Modifier.fillMaxSize(),
                )
                MainTab.Bookmarks -> BookmarksTabNavHost(
                    container = container,
                    sharedAudioViewModel = sharedAudioViewModel,
                    tabChromeViewModel = tabChromeViewModel,
                    modifier = Modifier.fillMaxSize(),
                )
                MainTab.Settings -> SettingsScreen(
                    viewModel = settingsViewModel,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
