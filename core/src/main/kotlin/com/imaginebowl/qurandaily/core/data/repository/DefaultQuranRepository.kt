package com.imaginebowl.qurandaily.core.data.repository

import com.imaginebowl.qurandaily.core.data.load
import com.imaginebowl.qurandaily.core.data.save
import com.imaginebowl.qurandaily.core.domain.model.Ayah
import com.imaginebowl.qurandaily.core.domain.model.Juz
import com.imaginebowl.qurandaily.core.domain.model.QuranError
import com.imaginebowl.qurandaily.core.domain.model.StoragePaths
import com.imaginebowl.qurandaily.core.domain.model.StoredQuranBundle
import com.imaginebowl.qurandaily.core.domain.model.Surah
import com.imaginebowl.qurandaily.core.domain.repository.QuranRepository
import com.imaginebowl.qurandaily.core.domain.repository.StorageService
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class DefaultQuranRepository(
    private val storage: StorageService,
) : QuranRepository {
    private val cacheMutex = Mutex()
    private var cachedBundle: StoredQuranBundle? = null

    override suspend fun isQuranDownloaded(): Boolean =
        storage.fileExists(StoragePaths.QURAN_BUNDLE)

    override suspend fun fetchSurahs(): List<Surah> =
        loadBundle().surahs.sortedBy { it.number }

    override suspend fun fetchAyahs(forSurah: Int): List<Ayah> {
        val ayahs = loadBundle().ayahsBySurah[forSurah]
            ?: throw QuranError.SurahNotFound(forSurah)
        return ayahs.sortedBy { it.numberInSurah }
    }

    override suspend fun fetchAyah(surahNumber: Int, ayahInSurah: Int): Ayah? =
        loadBundle().ayahsBySurah[surahNumber]
            ?.firstOrNull { it.numberInSurah == ayahInSurah }

    override suspend fun fetchAyah(byAbsoluteNumber: Int): Ayah? {
        val bundle = loadBundle()
        for (ayahs in bundle.ayahsBySurah.values) {
            val match = ayahs.firstOrNull { it.number == byAbsoluteNumber }
            if (match != null) return match
        }
        return null
    }

    override suspend fun fetchJuzList(): List<Juz> =
        loadBundle().juzs.sortedBy { it.number }

    override suspend fun saveQuranData(
        surahs: List<Surah>,
        ayahsBySurah: Map<Int, List<Ayah>>,
        juzs: List<Juz>,
    ) {
        val bundle = StoredQuranBundle(surahs = surahs, ayahsBySurah = ayahsBySurah, juzs = juzs)
        storage.save(bundle, StoragePaths.QURAN_BUNDLE)
        cacheMutex.withLock { cachedBundle = bundle }
    }

    override suspend fun clearQuranCache() {
        cacheMutex.withLock { cachedBundle = null }
        storage.deleteFile(StoragePaths.QURAN_BUNDLE)
    }

    private suspend fun loadBundle(): StoredQuranBundle = cacheMutex.withLock {
        cachedBundle?.let { return@withLock it }
        val loaded = storage.load<StoredQuranBundle>(StoragePaths.QURAN_BUNDLE)
            ?: throw QuranError.NotDownloaded
        cachedBundle = loaded
        loaded
    }
}
