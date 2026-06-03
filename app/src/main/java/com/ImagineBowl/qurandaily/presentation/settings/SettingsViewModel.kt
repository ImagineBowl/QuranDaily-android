package com.imaginebowl.qurandaily.presentation.settings

import android.app.Activity
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
import com.imaginebowl.qurandaily.data.billing.TipJarService
import com.imaginebowl.qurandaily.data.billing.TipLoadResult
import com.imaginebowl.qurandaily.data.billing.TipProduct
import com.imaginebowl.qurandaily.data.billing.TipPurchaseResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

data class SettingsUiState(
    val settings: AppSettings = AppSettings.DEFAULT,
    val storageInfo: StorageInfo = StorageInfo(0, 0),
    val tipOptions: List<TipProduct> = emptyList(),
    val tipsUnavailableReason: String? = null,
    val isLoading: Boolean = false,
    val isPurchasing: Boolean = false,
    val statusMessage: String? = null,
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val storageInfoUseCase: StorageInfoUseCase,
    private val clearCacheUseCase: ClearCacheUseCase,
    private val tipJarService: TipJarService,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    var onThemeChanged: ((AppThemeMode) -> Unit)? = null

    private var purchaseHostRef: WeakReference<Activity>? = null

    fun attachPurchaseHost(activity: Activity) {
        purchaseHostRef = WeakReference(activity)
    }

    fun detachPurchaseHost() {
        purchaseHostRef = null
    }

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
        viewModelScope.launch {
            when (val result = tipJarService.loadProducts()) {
                is TipLoadResult.Ready -> {
                    _uiState.update {
                        it.copy(
                            tipOptions = result.products,
                            tipsUnavailableReason = null,
                        )
                    }
                }
                is TipLoadResult.Unavailable -> {
                    _uiState.update {
                        it.copy(
                            tipOptions = result.fallbackProducts,
                            tipsUnavailableReason = result.reason,
                        )
                    }
                }
            }
        }
    }

    fun updateFontSize(size: Double) {
        viewModelScope.launch {
            persistSettings(_uiState.value.settings.copy(fontSize = size))
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

    fun tip(product: TipProduct) {
        val activity = purchaseHostRef?.get()
        if (activity == null) {
            _uiState.update {
                it.copy(statusMessage = "Unable to start purchase. Reopen Settings and try again.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isPurchasing = true, statusMessage = null) }
            when (val result = tipJarService.purchase(activity, product.id)) {
                TipPurchaseResult.Success -> {
                    _uiState.update {
                        it.copy(statusMessage = "Thank you for supporting QuranDaily!")
                    }
                }
                TipPurchaseResult.Cancelled -> Unit
                is TipPurchaseResult.Failed -> {
                    _uiState.update { it.copy(statusMessage = result.message) }
                }
            }
            _uiState.update { it.copy(isPurchasing = false) }
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
