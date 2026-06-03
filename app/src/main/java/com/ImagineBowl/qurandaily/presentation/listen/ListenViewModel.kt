package com.imaginebowl.qurandaily.presentation.listen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imaginebowl.qurandaily.core.domain.model.AppSettings
import com.imaginebowl.qurandaily.core.domain.model.RecentListen
import com.imaginebowl.qurandaily.core.domain.model.Surah
import com.imaginebowl.qurandaily.core.domain.repository.RecentListenRepository
import com.imaginebowl.qurandaily.core.domain.repository.SettingsRepository
import com.imaginebowl.qurandaily.core.domain.usecase.FetchQuranUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ListenUiState(
    val surahs: List<Surah> = emptyList(),
    val recentListens: List<RecentListen> = emptyList(),
    val settings: AppSettings = AppSettings.DEFAULT,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

class ListenViewModel(
    private val fetchQuranUseCase: FetchQuranUseCase,
    private val recentListenRepository: RecentListenRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ListenUiState())
    val uiState: StateFlow<ListenUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val surahs = fetchQuranUseCase.executeSurahs()
                val recent = recentListenRepository.fetchRecent()
                val settings = settingsRepository.fetchSettings()
                _uiState.update {
                    it.copy(
                        surahs = surahs,
                        recentListens = recent,
                        settings = settings,
                        isLoading = false,
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to load",
                    )
                }
            }
        }
    }

    fun recordRecentListen(surahNumber: Int, surahName: String, ayahNumber: Int) {
        viewModelScope.launch {
            val updated = recentListenRepository.record(surahNumber, surahName, ayahNumber)
            _uiState.update { it.copy(recentListens = updated) }
        }
    }
}
