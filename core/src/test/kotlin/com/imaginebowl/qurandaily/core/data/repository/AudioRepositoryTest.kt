package com.imaginebowl.qurandaily.core.data.repository

import com.imaginebowl.qurandaily.core.fake.FakeApiClient
import com.imaginebowl.qurandaily.core.fake.FakeDownloadService
import com.imaginebowl.qurandaily.core.fake.FakeStorageService
import com.imaginebowl.qurandaily.core.fake.TestFixtures
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.io.path.exists

class AudioRepositoryTest {
    private lateinit var storage: FakeStorageService
    private lateinit var downloadService: FakeDownloadService
    private lateinit var apiClient: FakeApiClient
    private lateinit var repository: DefaultAudioRepository

    @Before
    fun setUp() {
        storage = FakeStorageService()
        downloadService = FakeDownloadService()
        apiClient = FakeApiClient()
        val quranRepository = DefaultQuranRepository(storage)
        repository = DefaultAudioRepository(storage, downloadService, apiClient, quranRepository)
    }

    @Test
    fun downloadSurahAudio_storesLocalFile() = runTest {
        val path = repository.downloadSurahAudio(1)
        assertTrue(path.exists())
        assertTrue(path.fileName.toString().contains("001.mp3"))
        assertTrue(repository.isSurahDownloaded(1))
    }

    @Test
    fun downloadSurahAudio_usesCacheWhenExists() = runTest {
        repository.downloadSurahAudio(1)
        downloadService.shouldThrow = true
        val path = repository.downloadSurahAudio(1)
        assertTrue(path.exists())
    }

    @Test
    fun playbackUri_usesLocalFileWhenDownloaded() = runTest {
        val localPath = repository.downloadSurahAudio(1)
        val playbackUri = repository.playbackUri(1)
        assertEquals(localPath.toAbsolutePath().toUri(), playbackUri)
    }

    @Test
    fun playbackUri_usesStreamingWhenNotDownloaded() = runTest {
        val playbackUri = repository.playbackUri(36)
        assertEquals(apiClient.surahAudioUri(36), playbackUri)
    }

    @Test
    fun ayahStreamingUri_usesAbsoluteAyahNumber() = runTest {
        val quranRepository = DefaultQuranRepository(storage)
        val bundle = TestFixtures.makeBundle()
        quranRepository.saveQuranData(bundle.surahs, bundle.ayahsBySurah, bundle.juzs)

        val uri = repository.ayahStreamingUri(1, 2)
        assertEquals(apiClient.ayahAudioUri(2), uri)
    }
}
