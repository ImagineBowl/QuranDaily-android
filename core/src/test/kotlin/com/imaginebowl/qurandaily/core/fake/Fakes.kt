package com.imaginebowl.qurandaily.core.fake

import com.imaginebowl.qurandaily.core.data.api.ApiAyah
import com.imaginebowl.qurandaily.core.data.api.ApiSurah
import com.imaginebowl.qurandaily.core.data.api.MetaData
import com.imaginebowl.qurandaily.core.data.api.MetaJuzReference
import com.imaginebowl.qurandaily.core.data.api.MetaJuzs
import com.imaginebowl.qurandaily.core.data.api.MetaResponse
import com.imaginebowl.qurandaily.core.data.api.MetaSurahReference
import com.imaginebowl.qurandaily.core.data.api.MetaSurahs
import com.imaginebowl.qurandaily.core.data.api.QuranEditionData
import com.imaginebowl.qurandaily.core.data.api.QuranEditionResponse
import com.imaginebowl.qurandaily.core.data.save
import com.imaginebowl.qurandaily.core.domain.model.Ayah
import com.imaginebowl.qurandaily.core.domain.model.Juz
import com.imaginebowl.qurandaily.core.domain.model.QuranError
import com.imaginebowl.qurandaily.core.domain.model.StoredQuranBundle
import com.imaginebowl.qurandaily.core.domain.model.Surah
import com.imaginebowl.qurandaily.core.domain.repository.ApiClient
import com.imaginebowl.qurandaily.core.domain.repository.DownloadService
import com.imaginebowl.qurandaily.core.domain.repository.StorageService
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.writeBytes

class FakeStorageService(
    private val rootDirectory: Path = Files.createTempDirectory("qurandaily-test"),
) : StorageService {
    val files = mutableMapOf<String, ByteArray>()
    val directories = mutableSetOf<String>()

    override suspend fun saveBytes(filename: String, data: ByteArray) {
        files[filename] = data
    }

    override suspend fun loadBytes(filename: String): ByteArray? = files[filename]

    override suspend fun fileExists(filename: String): Boolean = files.containsKey(filename)

    override suspend fun deleteFile(filename: String) {
        files.remove(filename)
    }

    override suspend fun directorySize(relativePath: String): Long {
        files[relativePath]?.let { return it.size.toLong() }
        return files.filterKeys { it.startsWith(relativePath) }
            .values
            .sumOf { it.size.toLong() }
    }

    override suspend fun ensureDirectory(relativePath: String) {
        directories.add(relativePath)
        rootDirectory.resolve(relativePath).createDirectories()
    }

    override suspend fun resolvePath(relativePath: String): Path {
        val path = rootDirectory.resolve(relativePath)
        path.parent?.createDirectories()
        return path
    }
}

class FakeApiClient : ApiClient {
    var arabicResponse: QuranEditionResponse? = null
    var urduResponse: QuranEditionResponse? = null
    var metaResponse: MetaResponse? = null
    var shouldThrow: Boolean = false

    override suspend fun fetchArabicQuran(): QuranEditionResponse {
        if (shouldThrow) throw QuranError.InvalidResponse
        return arabicResponse ?: throw QuranError.InvalidResponse
    }

    override suspend fun fetchUrduTranslation(): QuranEditionResponse {
        if (shouldThrow) throw QuranError.InvalidResponse
        return urduResponse ?: throw QuranError.InvalidResponse
    }

    override suspend fun fetchMeta(): MetaResponse {
        if (shouldThrow) throw QuranError.InvalidResponse
        return metaResponse ?: throw QuranError.InvalidResponse
    }

    override fun surahAudioUri(surahNumber: Int): URI =
        URI.create("https://example.com/audio/$surahNumber.mp3")

    override fun ayahAudioUri(absoluteNumber: Int): URI =
        URI.create("https://example.com/ayah/$absoluteNumber.mp3")
}

class FakeDownloadService : DownloadService {
    val downloadedUris = mutableListOf<URI>()
    var shouldThrow: Boolean = false

    override suspend fun download(from: URI, to: Path) {
        if (shouldThrow) throw QuranError.DownloadFailed("Mock failure")
        downloadedUris.add(from)
        to.parent?.createDirectories()
        to.writeBytes("mock-audio".encodeToByteArray())
    }
}

object TestFixtures {
    val surah1 = Surah(
        number = 1,
        name = "سُورَةُ ٱلْفَاتِحَةِ",
        englishName = "Al-Faatiha",
        englishNameTranslation = "The Opening",
        revelationType = "Meccan",
        numberOfAyahs = 7,
    )

    val ayah1 = Ayah(
        number = 1,
        numberInSurah = 1,
        surahNumber = 1,
        arabicText = "بِسْمِ ٱللَّهِ",
        urduText = "شروع الله کا نام لے کر",
        juz = 1,
        page = 1,
    )

    val ayah2 = Ayah(
        number = 2,
        numberInSurah = 2,
        surahNumber = 1,
        arabicText = "ٱلْحَمْدُ لِلَّهِ",
        urduText = "سب تعریفیں",
        juz = 1,
        page = 1,
    )

    val juz1 = Juz(number = 1, startSurah = 1, startAyah = 1)

    fun makeBundle(): StoredQuranBundle =
        StoredQuranBundle(
            surahs = listOf(surah1),
            ayahsBySurah = mapOf(1 to listOf(ayah1, ayah2)),
            juzs = listOf(juz1),
        )

    fun makeArabicResponse(): QuranEditionResponse =
        QuranEditionResponse(
            code = 200,
            status = "OK",
            data = QuranEditionData(
                surahs = listOf(
                    ApiSurah(
                        number = 1,
                        name = surah1.name,
                        englishName = surah1.englishName,
                        englishNameTranslation = surah1.englishNameTranslation,
                        revelationType = surah1.revelationType,
                        ayahs = listOf(
                            ApiAyah(1, ayah1.arabicText, 1, 1, 1),
                            ApiAyah(2, ayah2.arabicText, 2, 1, 1),
                        ),
                    ),
                ),
            ),
        )

    fun makeUrduResponse(): QuranEditionResponse =
        QuranEditionResponse(
            code = 200,
            status = "OK",
            data = QuranEditionData(
                surahs = listOf(
                    ApiSurah(
                        number = 1,
                        name = surah1.name,
                        englishName = surah1.englishName,
                        englishNameTranslation = surah1.englishNameTranslation,
                        revelationType = surah1.revelationType,
                        ayahs = listOf(
                            ApiAyah(1, ayah1.urduText, 1, 1, 1),
                            ApiAyah(2, ayah2.urduText, 2, 1, 1),
                        ),
                    ),
                ),
            ),
        )

    fun makeMetaResponse(): MetaResponse =
        MetaResponse(
            code = 200,
            status = "OK",
            data = MetaData(
                surahs = MetaSurahs(
                    references = listOf(
                        MetaSurahReference(
                            number = 1,
                            name = surah1.name,
                            englishName = surah1.englishName,
                            englishNameTranslation = surah1.englishNameTranslation,
                            numberOfAyahs = 7,
                            revelationType = "Meccan",
                        ),
                    ),
                ),
                juzs = MetaJuzs(references = listOf(MetaJuzReference(surah = 1, ayah = 1))),
            ),
        )
}
