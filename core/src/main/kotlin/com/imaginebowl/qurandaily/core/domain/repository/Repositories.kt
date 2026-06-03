package com.imaginebowl.qurandaily.core.domain.repository

import com.imaginebowl.qurandaily.core.data.api.MetaResponse
import com.imaginebowl.qurandaily.core.data.api.QuranEditionResponse
import com.imaginebowl.qurandaily.core.domain.model.AppSettings
import com.imaginebowl.qurandaily.core.domain.model.Ayah
import com.imaginebowl.qurandaily.core.domain.model.Bookmark
import com.imaginebowl.qurandaily.core.domain.model.Juz
import com.imaginebowl.qurandaily.core.domain.model.ReadingPosition
import com.imaginebowl.qurandaily.core.domain.model.RecentListen
import com.imaginebowl.qurandaily.core.domain.model.Surah
import java.net.URI
import java.nio.file.Path
import java.util.UUID

interface QuranRepository {
    suspend fun isQuranDownloaded(): Boolean

    suspend fun fetchSurahs(): List<Surah>

    suspend fun fetchAyahs(forSurah: Int): List<Ayah>

    suspend fun fetchAyah(surahNumber: Int, ayahInSurah: Int): Ayah?

    suspend fun fetchAyah(byAbsoluteNumber: Int): Ayah?

    suspend fun fetchJuzList(): List<Juz>

    suspend fun saveQuranData(
        surahs: List<Surah>,
        ayahsBySurah: Map<Int, List<Ayah>>,
        juzs: List<Juz>,
    )

    suspend fun clearQuranCache()
}

interface AudioRepository {
    suspend fun isSurahDownloaded(surahNumber: Int): Boolean

    suspend fun localAudioPath(forSurah: Int): Path?

    fun streamingAudioUri(forSurah: Int): URI

    suspend fun ayahStreamingUri(surahNumber: Int, ayahInSurah: Int): URI

    suspend fun playbackUri(forSurah: Int): URI

    suspend fun downloadSurahAudio(surahNumber: Int): Path

    suspend fun downloadedSurahNumbers(): List<Int>

    suspend fun clearAudioCache()
}

interface DownloadService {
    suspend fun download(from: URI, to: Path)
}

interface BookmarkRepository {
    suspend fun fetchBookmarks(): List<Bookmark>

    suspend fun addBookmark(bookmark: Bookmark)

    suspend fun removeBookmark(id: UUID)

    suspend fun isBookmarked(surahNumber: Int, ayahNumber: Int): Boolean
}

interface ReadingPositionRepository {
    suspend fun fetchPosition(): ReadingPosition

    suspend fun savePosition(position: ReadingPosition)
}

interface SettingsRepository {
    suspend fun fetchSettings(): AppSettings

    suspend fun saveSettings(settings: AppSettings)
}

interface RecentListenRepository {
    suspend fun fetchRecent(): List<RecentListen>

    suspend fun record(surahNumber: Int, surahName: String, ayahNumber: Int): List<RecentListen>
}

interface ApiClient {
    suspend fun fetchArabicQuran(): QuranEditionResponse

    suspend fun fetchUrduTranslation(): QuranEditionResponse

    suspend fun fetchMeta(): MetaResponse

    fun surahAudioUri(surahNumber: Int): URI

    fun ayahAudioUri(absoluteNumber: Int): URI
}
