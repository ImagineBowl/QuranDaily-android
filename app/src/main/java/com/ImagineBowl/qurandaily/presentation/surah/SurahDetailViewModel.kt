package com.imaginebowl.qurandaily.presentation.surah

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imaginebowl.qurandaily.core.domain.model.AppSettings
import com.imaginebowl.qurandaily.core.domain.model.Ayah
import com.imaginebowl.qurandaily.core.domain.model.Bookmark
import com.imaginebowl.qurandaily.core.domain.model.Surah
import com.imaginebowl.qurandaily.core.domain.repository.BookmarkRepository
import com.imaginebowl.qurandaily.core.domain.repository.ReadingPositionRepository
import com.imaginebowl.qurandaily.core.domain.repository.SettingsRepository
import com.imaginebowl.qurandaily.core.domain.usecase.FetchQuranUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SurahDetailUiState(
    val surah: Surah,
    val ayahs: List<Ayah> = emptyList(),
    val bookmarkKeys: Set<String> = emptySet(),
    val settings: AppSettings = AppSettings.DEFAULT,
    val initialAyah: Int = 1,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

class SurahDetailViewModel(
    val surah: Surah,
    private val fetchQuranUseCase: FetchQuranUseCase,
    private val bookmarkRepository: BookmarkRepository,
    private val readingPositionRepository: ReadingPositionRepository,
    private val settingsRepository: SettingsRepository,
    initialAyah: Int,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        SurahDetailUiState(surah = surah, initialAyah = initialAyah),
    )
    val uiState: StateFlow<SurahDetailUiState> = _uiState.asStateFlow()

    fun load() {
        if (_uiState.value.ayahs.isNotEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val ayahs = fetchQuranUseCase.executeAyahs(surah.number)
                val settings = settingsRepository.fetchSettings()
                val keys = loadBookmarkKeys()
                _uiState.update {
                    it.copy(ayahs = ayahs, settings = settings, bookmarkKeys = keys, isLoading = false)
                }
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = error.message)
                }
            }
        }
    }

    fun toggleBookmark(ayah: Ayah) {
        viewModelScope.launch {
            val key = bookmarkKey(ayah.surahNumber, ayah.numberInSurah)
            try {
                if (_uiState.value.bookmarkKeys.contains(key)) {
                    val all = bookmarkRepository.fetchBookmarks()
                    val existing = all.firstOrNull {
                        it.surahNumber == ayah.surahNumber && it.ayahNumber == ayah.numberInSurah
                    }
                    if (existing != null) {
                        bookmarkRepository.removeBookmark(existing.id)
                    }
                } else {
                    bookmarkRepository.addBookmark(
                        Bookmark(
                            surahNumber = ayah.surahNumber,
                            ayahNumber = ayah.numberInSurah,
                            surahName = surah.englishName,
                            arabicPreview = ayah.arabicText.take(80),
                        ),
                    )
                }
                val keys = loadBookmarkKeys()
                _uiState.update { it.copy(bookmarkKeys = keys) }
            } catch (error: Exception) {
                _uiState.update { it.copy(errorMessage = error.message) }
            }
        }
    }

    fun saveReadingPosition(ayahNumber: Int) {
        viewModelScope.launch {
            readingPositionRepository.savePosition(
                com.imaginebowl.qurandaily.core.domain.model.ReadingPosition(
                    surahNumber = surah.number,
                    ayahNumber = ayahNumber,
                    scrollAnchor = "ayah-$ayahNumber",
                ),
            )
        }
    }

    fun isBookmarked(ayah: Ayah): Boolean =
        _uiState.value.bookmarkKeys.contains(bookmarkKey(ayah.surahNumber, ayah.numberInSurah))

    private suspend fun loadBookmarkKeys(): Set<String> =
        bookmarkRepository.fetchBookmarks()
            .map { bookmarkKey(it.surahNumber, it.ayahNumber) }
            .toSet()

    private fun bookmarkKey(surahNumber: Int, ayahNumber: Int): String = "$surahNumber-$ayahNumber"
}
