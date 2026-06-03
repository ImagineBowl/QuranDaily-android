package com.imaginebowl.qurandaily.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imaginebowl.qurandaily.core.domain.model.AppSettings
import com.imaginebowl.qurandaily.core.domain.model.AppThemeMode
import com.imaginebowl.qurandaily.core.domain.model.ArabicFontChoice
import com.imaginebowl.qurandaily.core.domain.model.StorageInfo
import com.imaginebowl.qurandaily.core.domain.model.UrduFontChoice
import com.imaginebowl.qurandaily.core.domain.repository.SettingsRepository
import com.imaginebowl.qurandaily.core.domain.usecase.ClearCacheUseCase
import com.imaginebowl.qurandaily.core.domain.usecase.StorageInfoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TipOption(
    val id: String,
    val displayName: String,
    val displayPrice: String,
)

data class SettingsUiState(
    val settings: AppSettings = AppSettings.DEFAULT,
    val storageInfo: StorageInfo = StorageInfo(0, 0),
    val tipOptions: List<TipOption> = emptyList(),
    val isLoading: Boolean = false,
    val isPurchasing: Boolean = false,
    val statusMessage: String? = null,
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val storageInfoUseCase: StorageInfoUseCase,
    private val clearCacheUseCase: ClearCacheUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    var onThemeChanged: ((AppThemeMode) -> Unit)? = null

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val settings = settingsRepository.fetchSettings()
            val storage = storageInfoUseCase.execute()
            _uiState.update {
                it.copy(
                    settings = settings,
                    storageInfo = storage,
                    isLoading = false,
                )
            }
            onThemeChanged?.invoke(settings.theme)
        }
    }

    fun loadTips() {
        _uiState.update {
            it.copy(
                tipOptions = listOf(
                    TipOption("com.imaginebowl.qurandaily.tip.small", "Small Tip", "$0.99"),
                    TipOption("com.imaginebowl.qurandaily.tip.medium", "Medium Tip", "$2.99"),
                    TipOption("com.imaginebowl.qurandaily.tip.large", "Generous Tip", "$4.99"),
                ),
            )
        }
    }

    fun updateFontSize(size: Double) {
        viewModelScope.launch {
            val updated = _uiState.value.settings.copy(fontSize = size)
            persistSettings(updated)
        }
    }

    fun updateArabicFont(font: ArabicFontChoice) {
        viewModelScope.launch {
            persistSettings(_uiState.value.settings.copy(arabicFont = font))
        }
    }

    fun updateUrduFont(font: UrduFontChoice) {
        viewModelScope.launch {
            persistSettings(_uiState.value.settings.copy(urduFont = font))
        }
    }

    fun updateTheme(theme: AppThemeMode) {
        viewModelScope.launch {
            persistSettings(_uiState.value.settings.copy(theme = theme))
            onThemeChanged?.invoke(theme)
        }
    }

    fun tip(option: TipOption) {
        _uiState.update {
            it.copy(
                statusMessage = "In-app tips require Google Play setup (M8). Thank you for your support!",
            )
        }
    }

    fun clearQuranCache() = clearCache(clearQuran = true, clearAudio = false, success = "Quran cache cleared.")

    fun clearAudioCache() = clearCache(clearQuran = false, clearAudio = true, success = "Audio cache cleared.")

    fun clearAllCache() = clearCache(clearQuran = true, clearAudio = true, success = "All cache cleared.")

    private fun clearCache(clearQuran: Boolean, clearAudio: Boolean, success: String) {
        viewModelScope.launch {
            runCatching {
                clearCacheUseCase.execute(clearQuran, clearAudio)
                val storage = storageInfoUseCase.execute()
                _uiState.update { it.copy(storageInfo = storage, statusMessage = success) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(statusMessage = error.message ?: "Could not clear cache")
                }
            }
        }
    }

    private suspend fun persistSettings(settings: AppSettings) {
        settingsRepository.saveSettings(settings)
        _uiState.update { it.copy(settings = settings) }
    }
}
