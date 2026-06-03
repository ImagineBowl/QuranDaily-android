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

class DefaultQuranRepository(
    private val storage: StorageService,
) : QuranRepository {
    override suspend fun isQuranDownloaded(): Boolean =
        storage.fileExists(StoragePaths.QURAN_BUNDLE)

    override suspend fun fetchSurahs(): List<Surah> =
        loadBundle().surahs.sortedBy { it.number }

    override suspend fun fetchAyahs(forSurah: Int): List<Ayah> {
        val bundle = loadBundle()
        val ayahs = bundle.ayahsBySurah[forSurah]
            ?: throw QuranError.SurahNotFound(forSurah)
        return ayahs.sortedBy { it.numberInSurah }
    }

    override suspend fun fetchAyah(surahNumber: Int, ayahInSurah: Int): Ayah? =
        fetchAyahs(surahNumber).firstOrNull { it.numberInSurah == ayahInSurah }

    override suspend fun fetchAyah(byAbsoluteNumber: Int): Ayah? {
        val bundle = loadBundle()
        return bundle.ayahsBySurah.values
            .flatten()
            .firstOrNull { it.number == byAbsoluteNumber }
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
    }

    override suspend fun clearQuranCache() {
        storage.deleteFile(StoragePaths.QURAN_BUNDLE)
    }

    private suspend fun loadBundle(): StoredQuranBundle =
        storage.load<StoredQuranBundle>(StoragePaths.QURAN_BUNDLE)
            ?: throw QuranError.NotDownloaded
}
