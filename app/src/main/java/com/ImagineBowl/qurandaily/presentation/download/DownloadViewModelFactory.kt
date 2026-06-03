package com.imaginebowl.qurandaily.presentation.download

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.imaginebowl.qurandaily.di.AppContainer

class DownloadViewModelFactory(
    private val container: AppContainer,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DownloadViewModel::class.java)) {
            return DownloadViewModel(
                downloadQuranUseCase = container.downloadQuranUseCase,
                fetchQuranUseCase = container.fetchQuranUseCase,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
