package com.imaginebowl.qurandaily.core.domain.usecase

import com.imaginebowl.qurandaily.core.data.repository.DefaultQuranRepository
import com.imaginebowl.qurandaily.core.domain.model.DownloadProgress
import com.imaginebowl.qurandaily.core.domain.model.QuranError
import com.imaginebowl.qurandaily.core.fake.FakeApiClient
import com.imaginebowl.qurandaily.core.fake.FakeStorageService
import com.imaginebowl.qurandaily.core.fake.TestFixtures
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DownloadQuranUseCaseTest {
    private lateinit var storage: FakeStorageService
    private lateinit var apiClient: FakeApiClient
    private lateinit var repository: DefaultQuranRepository
    private lateinit var useCase: DownloadQuranUseCase

    @Before
    fun setUp() {
        storage = FakeStorageService()
        apiClient = FakeApiClient()
        apiClient.arabicResponse = TestFixtures.makeArabicResponse()
        apiClient.urduResponse = TestFixtures.makeUrduResponse()
        apiClient.metaResponse = TestFixtures.makeMetaResponse()
        repository = DefaultQuranRepository(storage)
        useCase = DownloadQuranUseCase(apiClient, repository)
    }

    @Test
    fun execute_downloadsAndStoresMergedQuran() = runTest {
        var lastProgress: DownloadProgress = DownloadProgress.Idle

        useCase.execute { progress -> lastProgress = progress }

        assertTrue(lastProgress is DownloadProgress.Completed)
        val surahs = repository.fetchSurahs()
        assertEquals(1, surahs.size)
        val ayahs = repository.fetchAyahs(1)
        assertEquals(TestFixtures.ayah1.urduText, ayahs.first().urduText)
    }

    @Test
    fun execute_skipsDownload_whenAlreadyStored() = runTest {
        val bundle = TestFixtures.makeBundle()
        repository.saveQuranData(bundle.surahs, bundle.ayahsBySurah, bundle.juzs)

        val progressValues = mutableListOf<DownloadProgress>()
        useCase.execute { progress -> progressValues.add(progress) }

        assertEquals(1, progressValues.size)
        assertTrue(progressValues.first() is DownloadProgress.Completed)
    }

    @Test
    fun execute_throws_whenApiFails() = runTest {
        apiClient.shouldThrow = true
        try {
            useCase.execute { }
            error("Expected InvalidResponse")
        } catch (error: QuranError.InvalidResponse) {
            assertTrue(true)
        }
    }
}
