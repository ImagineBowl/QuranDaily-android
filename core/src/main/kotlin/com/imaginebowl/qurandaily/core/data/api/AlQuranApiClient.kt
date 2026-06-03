package com.imaginebowl.qurandaily.core.data.api

import com.imaginebowl.qurandaily.core.data.JsonCodec
import com.imaginebowl.qurandaily.core.domain.model.QuranError
import com.imaginebowl.qurandaily.core.domain.repository.ApiClient
import java.net.URI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.serializer
import okhttp3.OkHttpClient
import okhttp3.Request

class AlQuranApiClient(
    private val okHttpClient: OkHttpClient = OkHttpClient(),
) : ApiClient {
    private val baseUrl = "https://api.alquran.cloud/v1"

    override suspend fun fetchArabicQuran(): QuranEditionResponse =
        fetchEdition("quran-uthmani")

    override suspend fun fetchUrduTranslation(): QuranEditionResponse =
        fetchEdition("ur.jalandhry")

    override suspend fun fetchMeta(): MetaResponse =
        performRequest("$baseUrl/meta")

    override fun surahAudioUri(surahNumber: Int): URI =
        URI.create("https://cdn.islamic.network/quran/audio-surah/128/ar.alafasy/$surahNumber.mp3")

    override fun ayahAudioUri(absoluteNumber: Int): URI =
        URI.create("https://cdn.islamic.network/quran/audio/128/ar.alafasy/$absoluteNumber.mp3")

    private suspend fun fetchEdition(edition: String): QuranEditionResponse =
        performRequest("$baseUrl/quran/$edition")

    private suspend inline fun <reified T> performRequest(url: String): T = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(url).build()
        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw QuranError.InvalidResponse
            }
            val body = response.body?.string() ?: throw QuranError.InvalidResponse
            try {
                JsonCodec.json.decodeFromString(serializer<T>(), body)
            } catch (_: Exception) {
                throw QuranError.InvalidResponse
            }
        }
    }
}
