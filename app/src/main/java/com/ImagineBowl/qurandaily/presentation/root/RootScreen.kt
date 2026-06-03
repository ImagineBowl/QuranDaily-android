package com.imaginebowl.qurandaily.presentation.root

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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
import com.imaginebowl.qurandaily.ui.theme.Accent

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

    LaunchedEffect(Unit) {
        downloadViewModel.checkDownloadStatus()
    }

    when {
        !uiState.hasCheckedDownload -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Accent)
            }
        }
        uiState.isDownloaded -> {
            MainTabScreen(
                container = container,
                onThemeChanged = onThemeChanged,
                modifier = modifier,
            )
        }
        else -> {
            DownloadScreen(viewModel = downloadViewModel, modifier = modifier)
        }
    }
}
