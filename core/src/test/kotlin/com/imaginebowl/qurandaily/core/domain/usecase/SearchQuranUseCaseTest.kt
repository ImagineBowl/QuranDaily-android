package com.imaginebowl.qurandaily.core.domain.usecase

import com.imaginebowl.qurandaily.core.data.repository.DefaultQuranRepository
import com.imaginebowl.qurandaily.core.domain.model.SearchMode
import com.imaginebowl.qurandaily.core.domain.model.SearchResult
import com.imaginebowl.qurandaily.core.fake.FakeStorageService
import com.imaginebowl.qurandaily.core.fake.TestFixtures
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SearchQuranUseCaseTest {
    private lateinit var repository: DefaultQuranRepository
    private lateinit var useCase: SearchQuranUseCase

    @Before
    fun setUp() = runBlocking {
        val storage = FakeStorageService()
        repository = DefaultQuranRepository(storage)
        useCase = SearchQuranUseCase(repository)
        val bundle = TestFixtures.makeBundle()
        repository.saveQuranData(bundle.surahs, bundle.ayahsBySurah, bundle.juzs)
    }

    @Test
    fun execute_returnsSurahNumberMatch() = runTest {
        val results = useCase.execute("1")
        assertTrue(results.any { it.matchType == SearchResult.MatchType.SURAH_NUMBER })
    }

    @Test
    fun execute_returnsSurahNameMatch() = runTest {
        val results = useCase.execute("faatiha")
        assertTrue(results.any { it.matchType == SearchResult.MatchType.SURAH_NAME })
    }

    @Test
    fun execute_returnsJuzMatch() = runTest {
        val results = useCase.execute("1")
        assertTrue(results.any { it.matchType == SearchResult.MatchType.JUZ })
    }

    @Test
    fun execute_returnsTextMatch() = runTest {
        val results = useCase.execute("تعریف", SearchMode.TEXT)
        assertTrue(results.any { it.matchType == SearchResult.MatchType.TEXT })
    }

    @Test
    fun execute_returnsAyahReference_forColonSyntax() = runTest {
        val results = useCase.execute("1:1", SearchMode.AYAH)
        assertEquals(1, results.size)
        assertEquals(SearchResult.MatchType.AYAH_REFERENCE, results.first().matchType)
        assertEquals(1, results.first().surahNumber)
        assertEquals(1, results.first().ayahNumber)
    }

    @Test
    fun execute_returnsAyahReference_forAbsoluteNumber() = runTest {
        val results = useCase.execute("2", SearchMode.AYAH)
        assertEquals(1, results.size)
        assertEquals(SearchResult.MatchType.AYAH_REFERENCE, results.first().matchType)
        assertEquals(1, results.first().surahNumber)
        assertEquals(2, results.first().ayahNumber)
        assertEquals(2, results.first().absoluteAyahNumber)
    }

    @Test
    fun execute_returnsEmpty_forInvalidAyahReference() = runTest {
        val results = useCase.execute("999:999", SearchMode.AYAH)
        assertTrue(results.isEmpty())
    }

    @Test
    fun execute_returnsEmpty_forBlankQuery() = runTest {
        val results = useCase.execute("   ")
        assertTrue(results.isEmpty())
    }
}
