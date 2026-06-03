package com.imaginebowl.qurandaily.core.data.repository

import com.imaginebowl.qurandaily.core.data.load
import com.imaginebowl.qurandaily.core.data.save
import com.imaginebowl.qurandaily.core.domain.model.AppSettings
import com.imaginebowl.qurandaily.core.domain.model.Bookmark
import com.imaginebowl.qurandaily.core.domain.model.ReadingPosition
import com.imaginebowl.qurandaily.core.domain.model.RecentListen
import com.imaginebowl.qurandaily.core.domain.model.StoragePaths
import com.imaginebowl.qurandaily.core.domain.repository.BookmarkRepository
import com.imaginebowl.qurandaily.core.domain.repository.ReadingPositionRepository
import com.imaginebowl.qurandaily.core.domain.repository.RecentListenRepository
import com.imaginebowl.qurandaily.core.domain.repository.SettingsRepository
import com.imaginebowl.qurandaily.core.domain.repository.StorageService
import java.time.Instant
import java.util.UUID

class DefaultBookmarkRepository(
    private val storage: StorageService,
) : BookmarkRepository {
    override suspend fun fetchBookmarks(): List<Bookmark> {
        val bookmarks: List<Bookmark>? = storage.load(StoragePaths.BOOKMARKS)
        return bookmarks?.sortedByDescending { it.createdAt } ?: emptyList()
    }

    override suspend fun addBookmark(bookmark: Bookmark) {
        val bookmarks = fetchBookmarks()
            .filterNot {
                it.surahNumber == bookmark.surahNumber && it.ayahNumber == bookmark.ayahNumber
            }
            .toMutableList()
        bookmarks.add(0, bookmark)
        storage.save(bookmarks, StoragePaths.BOOKMARKS)
    }

    override suspend fun removeBookmark(id: UUID) {
        val bookmarks = fetchBookmarks().filterNot { it.id == id }
        storage.save(bookmarks, StoragePaths.BOOKMARKS)
    }

    override suspend fun isBookmarked(surahNumber: Int, ayahNumber: Int): Boolean =
        fetchBookmarks().any { it.surahNumber == surahNumber && it.ayahNumber == ayahNumber }
}

class DefaultReadingPositionRepository(
    private val storage: StorageService,
) : ReadingPositionRepository {
    override suspend fun fetchPosition(): ReadingPosition =
        storage.load<ReadingPosition>(StoragePaths.READING_POSITION) ?: ReadingPosition.DEFAULT

    override suspend fun savePosition(position: ReadingPosition) {
        storage.save(position, StoragePaths.READING_POSITION)
    }
}

class DefaultSettingsRepository(
    private val storage: StorageService,
) : SettingsRepository {
    override suspend fun fetchSettings(): AppSettings =
        storage.load<AppSettings>(StoragePaths.SETTINGS) ?: AppSettings.DEFAULT

    override suspend fun saveSettings(settings: AppSettings) {
        storage.save(settings, StoragePaths.SETTINGS)
    }
}

class DefaultRecentListenRepository(
    private val storage: StorageService,
) : RecentListenRepository {
    private val maxItems = 10

    override suspend fun fetchRecent(): List<RecentListen> {
        val items: List<RecentListen>? = storage.load(StoragePaths.RECENT_LISTENS)
        return items?.sortedByDescending { it.listenedAt } ?: emptyList()
    }

    override suspend fun record(
        surahNumber: Int,
        surahName: String,
        ayahNumber: Int,
    ): List<RecentListen> {
        val items = fetchRecent()
            .filterNot { it.surahNumber == surahNumber }
            .toMutableList()
        items.add(
            0,
            RecentListen(
                surahNumber = surahNumber,
                surahName = surahName,
                ayahNumber = ayahNumber,
                listenedAt = Instant.now(),
            ),
        )
        val trimmed = items.take(maxItems)
        storage.save(trimmed, StoragePaths.RECENT_LISTENS)
        return trimmed
    }
}
