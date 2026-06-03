package com.imaginebowl.qurandaily.core.data.repository

import com.imaginebowl.qurandaily.core.domain.model.Bookmark
import com.imaginebowl.qurandaily.core.fake.FakeStorageService
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BookmarkRepositoryTest {
    private lateinit var repository: DefaultBookmarkRepository

    @Before
    fun setUp() {
        repository = DefaultBookmarkRepository(FakeStorageService())
    }

    @Test
    fun addAndFetchBookmark_persistsEntry() = runTest {
        val bookmark = Bookmark(
            surahNumber = 1,
            ayahNumber = 1,
            surahName = "Al-Faatiha",
            arabicPreview = "بِسْمِ",
        )
        repository.addBookmark(bookmark)
        val bookmarks = repository.fetchBookmarks()
        assertEquals(1, bookmarks.size)
        assertEquals(1, bookmarks.first().surahNumber)
    }

    @Test
    fun isBookmarked_returnsTrue_afterAdd() = runTest {
        repository.addBookmark(
            Bookmark(
                surahNumber = 2,
                ayahNumber = 5,
                surahName = "Al-Baqara",
                arabicPreview = "preview",
            ),
        )
        assertTrue(repository.isBookmarked(2, 5))
    }

    @Test
    fun removeBookmark_deletesEntry() = runTest {
        val bookmark = Bookmark(
            surahNumber = 1,
            ayahNumber = 2,
            surahName = "Al-Faatiha",
            arabicPreview = "preview",
        )
        repository.addBookmark(bookmark)
        repository.removeBookmark(bookmark.id)
        assertTrue(repository.fetchBookmarks().isEmpty())
    }
}
