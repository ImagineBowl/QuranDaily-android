package com.imaginebowl.qurandaily.presentation.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imaginebowl.qurandaily.core.domain.model.Bookmark
import com.imaginebowl.qurandaily.core.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class BookmarksUiState(
    val bookmarks: List<Bookmark> = emptyList(),
    val isLoading: Boolean = false,
)

class BookmarksViewModel(
    private val bookmarkRepository: BookmarkRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(BookmarksUiState())
    val uiState: StateFlow<BookmarksUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val bookmarks = runCatching { bookmarkRepository.fetchBookmarks() }
                .getOrDefault(emptyList())
            _uiState.update { it.copy(bookmarks = bookmarks, isLoading = false) }
        }
    }

    fun removeBookmark(id: UUID) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(bookmarks = state.bookmarks.filterNot { it.id == id })
            }
            bookmarkRepository.removeBookmark(id)
        }
    }
}
