package com.imaginebowl.qurandaily.presentation.download

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imaginebowl.qurandaily.core.domain.model.DownloadProgress
import com.imaginebowl.qurandaily.core.domain.usecase.DownloadQuranUseCase
import com.imaginebowl.qurandaily.core.domain.usecase.FetchQuranUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DownloadUiState(
    val progress: DownloadProgress = DownloadProgress.Idle,
    val isDownloaded: Boolean = false,
) {
    val isDownloading: Boolean
        get() = progress is DownloadProgress.Downloading
}

class DownloadViewModel(
    private val downloadQuranUseCase: DownloadQuranUseCase,
    private val fetchQuranUseCase: FetchQuranUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DownloadUiState())
    val uiState: StateFlow<DownloadUiState> = _uiState.asStateFlow()

    fun checkDownloadStatus() {
        viewModelScope.launch {
            val downloaded = fetchQuranUseCase.isDownloaded()
            _uiState.update { it.copy(isDownloaded = downloaded) }
        }
    }

    fun startDownload() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(progress = DownloadProgress.Downloading("Preparing download...", 0.05))
            }
            try {
                downloadQuranUseCase.execute { update ->
                    _uiState.update { state ->
                        state.copy(
                            progress = update,
                            isDownloaded = state.isDownloaded || update is DownloadProgress.Completed,
                        )
                    }
                }
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(progress = DownloadProgress.Failed(error.message ?: "Download failed"))
                }
            }
        }
    }
}
