package com.imaginebowl.qurandaily.core.domain.usecase

import com.imaginebowl.qurandaily.core.data.api.MetaResponse
import com.imaginebowl.qurandaily.core.data.api.QuranEditionResponse
import com.imaginebowl.qurandaily.core.domain.model.Ayah
import com.imaginebowl.qurandaily.core.domain.model.DownloadProgress
import com.imaginebowl.qurandaily.core.domain.model.Juz
import com.imaginebowl.qurandaily.core.domain.model.SearchMode
import com.imaginebowl.qurandaily.core.domain.model.SearchResult
import com.imaginebowl.qurandaily.core.domain.model.StorageInfo
import com.imaginebowl.qurandaily.core.domain.model.StoragePaths
import com.imaginebowl.qurandaily.core.domain.model.StoredQuranBundle
import com.imaginebowl.qurandaily.core.domain.model.Surah
import com.imaginebowl.qurandaily.core.domain.parser.AyahReference
import com.imaginebowl.qurandaily.core.domain.parser.AyahReferenceParser
import com.imaginebowl.qurandaily.core.domain.repository.ApiClient
import com.imaginebowl.qurandaily.core.domain.repository.AudioRepository
import com.imaginebowl.qurandaily.core.domain.repository.QuranRepository
import com.imaginebowl.qurandaily.core.domain.repository.StorageService

class FetchQuranUseCase(
    private val quranRepository: QuranRepository,
) {
    suspend fun executeSurahs(): List<Surah> = quranRepository.fetchSurahs()

    suspend fun executeAyahs(forSurah: Int): List<Ayah> = quranRepository.fetchAyahs(forSurah)

    suspend fun executeAyah(surahNumber: Int, ayahInSurah: Int): Ayah? =
        quranRepository.fetchAyah(surahNumber, ayahInSurah)

    suspend fun executeJuzList(): List<Juz> = quranRepository.fetchJuzList()

    suspend fun isDownloaded(): Boolean = quranRepository.isQuranDownloaded()
}

class DownloadQuranUseCase(
    private val apiClient: ApiClient,
    private val quranRepository: QuranRepository,
) {
    suspend fun execute(progressHandler: (DownloadProgress) -> Unit) {
        if (quranRepository.isQuranDownloaded()) {
            progressHandler(DownloadProgress.Completed)
            return
        }

        progressHandler(DownloadProgress.Downloading("Fetching Arabic Quran...", 0.1))
        val arabic = apiClient.fetchArabicQuran()

        progressHandler(DownloadProgress.Downloading("Fetching Urdu translation...", 0.4))
        val urdu = apiClient.fetchUrduTranslation()

        progressHandler(DownloadProgress.Downloading("Fetching metadata...", 0.7))
        val meta = apiClient.fetchMeta()

        val merged = mergeQuranData(arabic, urdu, meta)

        progressHandler(DownloadProgress.Downloading("Saving locally...", 0.9))
        quranRepository.saveQuranData(
            surahs = merged.surahs,
            ayahsBySurah = merged.ayahsBySurah,
            juzs = merged.juzs,
        )

        progressHandler(DownloadProgress.Completed)
    }

    internal fun mergeQuranData(
        arabic: QuranEditionResponse,
        urdu: QuranEditionResponse,
        meta: MetaResponse,
    ): StoredQuranBundle {
        val urduAyahsBySurah = urdu.data.surahs.associate { surah ->
            surah.number to surah.ayahs.associate { it.numberInSurah to it.text }
        }

        val ayahsBySurah = mutableMapOf<Int, List<Ayah>>()
        val surahs = mutableListOf<Surah>()

        for (apiSurah in arabic.data.surahs) {
            val metaSurah = meta.data.surahs.references.firstOrNull { it.number == apiSurah.number }
            val surah = Surah(
                number = apiSurah.number,
                name = apiSurah.name,
                englishName = apiSurah.englishName,
                englishNameTranslation = apiSurah.englishNameTranslation,
                revelationType = apiSurah.revelationType,
                numberOfAyahs = metaSurah?.numberOfAyahs ?: apiSurah.ayahs.size,
            )
            surahs.add(surah)

            val urduMap = urduAyahsBySurah[apiSurah.number].orEmpty()
            val ayahs = apiSurah.ayahs.map { apiAyah ->
                Ayah(
                    number = apiAyah.number,
                    numberInSurah = apiAyah.numberInSurah,
                    surahNumber = apiSurah.number,
                    arabicText = apiAyah.text.trim(),
                    urduText = urduMap[apiAyah.numberInSurah].orEmpty(),
                    juz = apiAyah.juz,
                    page = apiAyah.page,
                )
            }
            ayahsBySurah[apiSurah.number] = ayahs
        }

        val juzs = meta.data.juzs.references.mapIndexed { index, reference ->
            Juz(number = index + 1, startSurah = reference.surah, startAyah = reference.ayah)
        }

        return StoredQuranBundle(
            surahs = surahs.sortedBy { it.number },
            ayahsBySurah = ayahsBySurah,
            juzs = juzs,
        )
    }
}

class SearchQuranUseCase(
    private val quranRepository: QuranRepository,
) {
    suspend fun execute(query: String, mode: SearchMode = SearchMode.SURAH): List<SearchResult> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return emptyList()

        return when (mode) {
            SearchMode.SURAH -> searchSurahs(trimmed)
            SearchMode.AYAH -> searchAyahReference(trimmed)
            SearchMode.TEXT -> searchText(trimmed)
        }
    }

    private suspend fun searchSurahs(query: String): List<SearchResult> {
        val surahs = quranRepository.fetchSurahs()
        val results = mutableListOf<SearchResult>()

        query.toIntOrNull()?.let { surahNumber ->
            surahs.firstOrNull { it.number == surahNumber }?.let { surah ->
                results.add(
                    SearchResult(
                        id = "surah-number-${surah.number}",
                        surahNumber = surah.number,
                        ayahNumber = 1,
                        surahName = surah.englishName,
                        arabicText = surah.name,
                        urduText = surah.englishNameTranslation,
                        matchType = SearchResult.MatchType.SURAH_NUMBER,
                    ),
                )
            }
        }

        query.toIntOrNull()?.let { juzNumber ->
            if (juzNumber in 1..30) {
                val juzs = quranRepository.fetchJuzList()
                juzs.firstOrNull { it.number == juzNumber }?.let { juz ->
                    val surah = surahs.firstOrNull { it.number == juz.startSurah }
                    results.add(
                        SearchResult(
                            id = "juz-${juz.number}",
                            surahNumber = juz.startSurah,
                            ayahNumber = juz.startAyah,
                            surahName = surah?.englishName ?: "Surah ${juz.startSurah}",
                            arabicText = "Juz ${juz.number}",
                            urduText = "Starts at ${juz.startSurah}:${juz.startAyah}",
                            matchType = SearchResult.MatchType.JUZ,
                        ),
                    )
                }
            }
        }

        val normalizedQuery = query.lowercase()
        for (surah in surahs) {
            val matchesName =
                surah.englishName.lowercase().contains(normalizedQuery) ||
                    surah.englishNameTranslation.lowercase().contains(normalizedQuery) ||
                    surah.name.contains(query)

            if (matchesName) {
                results.add(
                    SearchResult(
                        id = "surah-name-${surah.number}",
                        surahNumber = surah.number,
                        ayahNumber = 1,
                        surahName = surah.englishName,
                        arabicText = surah.name,
                        urduText = surah.englishNameTranslation,
                        matchType = SearchResult.MatchType.SURAH_NAME,
                    ),
                )
            }
        }

        return deduplicated(results)
    }

    private suspend fun searchAyahReference(query: String): List<SearchResult> {
        val surahs = quranRepository.fetchSurahs()
        val reference = AyahReferenceParser.parse(query, surahs) ?: return emptyList()

        val ayah = when (reference) {
            is AyahReference.SurahAyah ->
                quranRepository.fetchAyah(reference.surah, reference.ayah)
            is AyahReference.AbsoluteNumber ->
                quranRepository.fetchAyah(byAbsoluteNumber = reference.number)
        } ?: return emptyList()

        val surah = surahs.firstOrNull { it.number == ayah.surahNumber }

        return listOf(
            SearchResult(
                id = "ayah-ref-${ayah.number}",
                surahNumber = ayah.surahNumber,
                ayahNumber = ayah.numberInSurah,
                surahName = surah?.englishName ?: "Surah ${ayah.surahNumber}",
                arabicText = ayah.arabicText,
                urduText = ayah.urduText,
                matchType = SearchResult.MatchType.AYAH_REFERENCE,
                absoluteAyahNumber = ayah.number,
            ),
        )
    }

    private suspend fun searchText(query: String): List<SearchResult> {
        val surahs = quranRepository.fetchSurahs()
        val normalizedQuery = query.lowercase()
        val results = mutableListOf<SearchResult>()

        for (surah in surahs) {
            val ayahs = quranRepository.fetchAyahs(surah.number)
            for (ayah in ayahs) {
                if (matchesText(ayah, normalizedQuery)) {
                    results.add(
                        SearchResult(
                            id = "text-${ayah.number}",
                            surahNumber = ayah.surahNumber,
                            ayahNumber = ayah.numberInSurah,
                            surahName = surah.englishName,
                            arabicText = ayah.arabicText,
                            urduText = ayah.urduText,
                            matchType = SearchResult.MatchType.TEXT,
                            absoluteAyahNumber = ayah.number,
                        ),
                    )
                }
            }
        }

        return deduplicated(results)
    }

    private fun matchesText(ayah: Ayah, query: String): Boolean =
        ayah.arabicText.contains(query) || ayah.urduText.lowercase().contains(query)

    private fun deduplicated(results: List<SearchResult>): List<SearchResult> {
        val seen = mutableSetOf<String>()
        return results.filter { result ->
            val key = "${result.surahNumber}-${result.ayahNumber}-${result.matchType}"
            if (key in seen) {
                false
            } else {
                seen.add(key)
                true
            }
        }
    }
}

class StorageInfoUseCase(
    private val storage: StorageService,
) {
    suspend fun execute(): StorageInfo {
        val quranBytes = storage.directorySize(StoragePaths.QURAN_BUNDLE)
        val audioBytes = storage.directorySize(StoragePaths.AUDIO_DIRECTORY)
        return StorageInfo(quranDataBytes = quranBytes, audioBytes = audioBytes)
    }
}

class ClearCacheUseCase(
    private val quranRepository: QuranRepository,
    private val audioRepository: AudioRepository,
) {
    suspend fun execute(clearQuran: Boolean, clearAudio: Boolean) {
        if (clearQuran) quranRepository.clearQuranCache()
        if (clearAudio) audioRepository.clearAudioCache()
    }
}
