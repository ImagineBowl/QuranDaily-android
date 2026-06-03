package com.imaginebowl.qurandaily.presentation.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.imaginebowl.qurandaily.di.AppContainerOwner
import com.imaginebowl.qurandaily.presentation.download.DownloadScreen
import com.imaginebowl.qurandaily.presentation.download.DownloadViewModel
import com.imaginebowl.qurandaily.di.DownloadViewModelFactory
import com.imaginebowl.qurandaily.core.domain.model.AppThemeMode
import com.imaginebowl.qurandaily.presentation.main.MainTabScreen

@Composable
fun RootScreen(
    onThemeChanged: (AppThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val container = (LocalContext.current.applicationContext as AppContainerOwner).appContainer
    val downloadViewModel: DownloadViewModel = viewModel(
        factory = DownloadViewModelFactory(container),
    )
    val uiState by downloadViewModel.uiState.collectAsStateWithLifecycle()
    var isReady by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        downloadViewModel.checkDownloadStatus()
    }

    LaunchedEffect(uiState.isDownloaded) {
        if (uiState.isDownloaded) {
            isReady = true
        }
    }

    if (isReady || uiState.isDownloaded) {
        MainTabScreen(
            container = container,
            onThemeChanged = onThemeChanged,
            modifier = modifier,
        )
    } else {
        DownloadScreen(viewModel = downloadViewModel, modifier = modifier)
    }
}
