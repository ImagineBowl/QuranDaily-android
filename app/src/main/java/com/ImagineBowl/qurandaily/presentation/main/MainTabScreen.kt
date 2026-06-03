package com.imaginebowl.qurandaily.presentation.main

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.imaginebowl.qurandaily.di.AppContainer
import com.imaginebowl.qurandaily.di.SharedAudioViewModelFactory
import com.imaginebowl.qurandaily.presentation.audio.SharedAudioViewModel
import com.imaginebowl.qurandaily.presentation.read.ReadTabNavHost
import com.imaginebowl.qurandaily.ui.components.PlaceholderTabScreen

private enum class MainTab(val label: String) {
    Read("Read"),
    Listen("Listen"),
    Bookmarks("Bookmarks"),
    Settings("Settings"),
}

@Composable
fun MainTabScreen(
    container: AppContainer,
    modifier: Modifier = Modifier,
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = MainTab.entries
    val activity = LocalContext.current as ComponentActivity
    val sharedAudioViewModel: SharedAudioViewModel = viewModel(
        viewModelStoreOwner = activity,
        factory = SharedAudioViewModelFactory(),
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
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
                    modifier = Modifier.fillMaxSize(),
                )
                else -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    PlaceholderTabScreen(title = tabs[selectedTab].label)
                }
            }
        }
    }
}
