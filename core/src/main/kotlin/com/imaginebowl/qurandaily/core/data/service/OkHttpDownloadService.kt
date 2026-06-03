package com.imaginebowl.qurandaily.core.data.service

import com.imaginebowl.qurandaily.core.domain.model.QuranError
import com.imaginebowl.qurandaily.core.domain.repository.DownloadService
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.writeBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class OkHttpDownloadService(
    private val okHttpClient: OkHttpClient = OkHttpClient(),
) : DownloadService {
    override suspend fun download(from: URI, to: Path) = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(from.toURL()).build()
        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw QuranError.DownloadFailed("Invalid server response.")
            }
            val bytes = response.body?.bytes() ?: throw QuranError.DownloadFailed("Empty response.")
            to.parent?.createDirectories()
            if (to.exists()) {
                Files.delete(to)
            }
            to.writeBytes(bytes)
        }
    }
}
