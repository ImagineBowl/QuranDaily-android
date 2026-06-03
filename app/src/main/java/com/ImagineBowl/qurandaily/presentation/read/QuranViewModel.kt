package com.imaginebowl.qurandaily.presentation.read

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imaginebowl.qurandaily.core.domain.model.Juz
import com.imaginebowl.qurandaily.core.domain.model.ReadingPosition
import com.imaginebowl.qurandaily.core.domain.model.Surah
import com.imaginebowl.qurandaily.core.domain.repository.ReadingPositionRepository
import com.imaginebowl.qurandaily.core.domain.usecase.FetchQuranUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QuranUiState(
    val surahs: List<Surah> = emptyList(),
    val juzs: List<Juz> = emptyList(),
    val readingPosition: ReadingPosition = ReadingPosition.DEFAULT,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

class QuranViewModel(
    private val fetchQuranUseCase: FetchQuranUseCase,
    private val readingPositionRepository: ReadingPositionRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(QuranUiState())
    val uiState: StateFlow<QuranUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val surahs = fetchQuranUseCase.executeSurahs()
                val juzs = fetchQuranUseCase.executeJuzList()
                val position = readingPositionRepository.fetchPosition()
                _uiState.update {
                    it.copy(
                        surahs = surahs,
                        juzs = juzs,
                        readingPosition = position,
                        isLoading = false,
                    )
                }
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = error.message)
                }
            }
        }
    }

    fun refreshReadingPosition() {
        viewModelScope.launch {
            val position = readingPositionRepository.fetchPosition()
            _uiState.update { it.copy(readingPosition = position) }
        }
    }

    fun updateReadingPosition(surahNumber: Int, ayahNumber: Int) {
        viewModelScope.launch {
            val position = ReadingPosition(
                surahNumber = surahNumber,
                ayahNumber = ayahNumber,
                scrollAnchor = "ayah-$ayahNumber",
            )
            readingPositionRepository.savePosition(position)
            _uiState.update { it.copy(readingPosition = position) }
        }
    }
}
