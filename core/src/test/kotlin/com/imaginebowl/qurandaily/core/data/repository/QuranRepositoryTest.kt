package com.imaginebowl.qurandaily.core.data.repository

import com.imaginebowl.qurandaily.core.domain.model.QuranError
import com.imaginebowl.qurandaily.core.fake.FakeStorageService
import com.imaginebowl.qurandaily.core.fake.TestFixtures
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class QuranRepositoryTest {
    private lateinit var storage: FakeStorageService
    private lateinit var repository: DefaultQuranRepository

    @Before
    fun setUp() {
        storage = FakeStorageService()
        repository = DefaultQuranRepository(storage)
    }

    @Test
    fun isQuranDownloaded_returnsFalse_whenMissing() = runTest {
        assertFalse(repository.isQuranDownloaded())
    }

    @Test
    fun saveAndFetchSurahs_persistsBundle() = runTest {
        val bundle = TestFixtures.makeBundle()
        repository.saveQuranData(bundle.surahs, bundle.ayahsBySurah, bundle.juzs)

        assertTrue(repository.isQuranDownloaded())
        val surahs = repository.fetchSurahs()
        assertEquals(1, surahs.size)
        assertEquals("Al-Faatiha", surahs.first().englishName)
    }

    @Test
    fun fetchAyahsForSurah_returnsAyahs() = runTest {
        val bundle = TestFixtures.makeBundle()
        repository.saveQuranData(bundle.surahs, bundle.ayahsBySurah, bundle.juzs)

        val ayahs = repository.fetchAyahs(1)
        assertEquals(2, ayahs.size)
        assertEquals(TestFixtures.ayah1.arabicText, ayahs.first().arabicText)
    }

    @Test
    fun fetchAyahsForSurah_throws_whenSurahMissing() = runTest {
        val bundle = TestFixtures.makeBundle()
        repository.saveQuranData(bundle.surahs, bundle.ayahsBySurah, bundle.juzs)

        try {
            repository.fetchAyahs(99)
            error("Expected SurahNotFound")
        } catch (error: QuranError.SurahNotFound) {
            assertEquals(99, error.number)
        }
    }

    @Test
    fun clearQuranCache_removesBundle() = runTest {
        val bundle = TestFixtures.makeBundle()
        repository.saveQuranData(bundle.surahs, bundle.ayahsBySurah, bundle.juzs)
        repository.clearQuranCache()
        assertFalse(repository.isQuranDownloaded())
    }

    @Test
    fun repeatedFetches_reuseInMemoryBundle() = runTest {
        val bundle = TestFixtures.makeBundle()
        repository.saveQuranData(bundle.surahs, bundle.ayahsBySurah, bundle.juzs)
        storage.quranBundleLoadCount = 0

        repeat(10) {
            repository.fetchAyah(1, 1)
        }

        assertEquals(0, storage.quranBundleLoadCount)
    }

    @Test
    fun clearQuranCache_invalidatesInMemoryBundle() = runTest {
        val bundle = TestFixtures.makeBundle()
        repository.saveQuranData(bundle.surahs, bundle.ayahsBySurah, bundle.juzs)
        repository.fetchSurahs()
        repository.clearQuranCache()

        try {
            repository.fetchSurahs()
            error("Expected NotDownloaded")
        } catch (_: QuranError.NotDownloaded) {
        }
    }
}
