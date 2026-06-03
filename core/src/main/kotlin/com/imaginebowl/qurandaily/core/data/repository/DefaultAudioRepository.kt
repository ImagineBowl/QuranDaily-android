package com.imaginebowl.qurandaily.core.data.repository

import com.imaginebowl.qurandaily.core.domain.model.QuranError
import com.imaginebowl.qurandaily.core.domain.model.StoragePaths
import com.imaginebowl.qurandaily.core.domain.repository.ApiClient
import com.imaginebowl.qurandaily.core.domain.repository.AudioRepository
import com.imaginebowl.qurandaily.core.domain.repository.DownloadService
import com.imaginebowl.qurandaily.core.domain.repository.QuranRepository
import com.imaginebowl.qurandaily.core.domain.repository.StorageService
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries

class DefaultAudioRepository(
    private val storage: StorageService,
    private val downloadService: DownloadService,
    private val apiClient: ApiClient,
    private val quranRepository: QuranRepository,
) : AudioRepository {
    override suspend fun isSurahDownloaded(surahNumber: Int): Boolean {
        val path = localFilePath(surahNumber)
        return path.exists()
    }

    override suspend fun localAudioPath(forSurah: Int): Path? {
        val path = localFilePath(forSurah)
        return path.takeIf { it.exists() }
    }

    override fun streamingAudioUri(forSurah: Int): URI =
        apiClient.surahAudioUri(forSurah)

    override suspend fun ayahStreamingUri(surahNumber: Int, ayahInSurah: Int): URI {
        val ayah = quranRepository.fetchAyah(surahNumber, ayahInSurah)
            ?: throw QuranError.AyahNotFound
        return apiClient.ayahAudioUri(ayah.number)
    }

    override suspend fun playbackUri(forSurah: Int): URI {
        val local = localAudioPath(forSurah)
        return local?.toAbsolutePath()?.toUri() ?: streamingAudioUri(forSurah)
    }

    override suspend fun downloadSurahAudio(surahNumber: Int): Path {
        val destination = localFilePath(surahNumber)
        if (destination.exists()) return destination

        storage.ensureDirectory(StoragePaths.AUDIO_DIRECTORY)
        val remote = apiClient.surahAudioUri(surahNumber)
        try {
            downloadService.download(from = remote, to = destination)
            return destination
        } catch (error: Exception) {
            throw QuranError.DownloadFailed(error.message ?: "Unknown error")
        }
    }

    override suspend fun downloadedSurahNumbers(): List<Int> {
        val directory = storage.resolvePath(StoragePaths.AUDIO_DIRECTORY)
        if (!directory.exists()) return emptyList()
        return directory.listDirectoryEntries()
            .mapNotNull { path ->
                path.fileName.toString().removeSuffix(".mp3").toIntOrNull()
            }
            .sorted()
    }

    override suspend fun clearAudioCache() {
        val directory = storage.resolvePath(StoragePaths.AUDIO_DIRECTORY)
        if (directory.exists()) {
            Files.walk(directory)
                .sorted(Comparator.reverseOrder())
                .forEach { Files.deleteIfExists(it) }
        }
    }

    private suspend fun localFilePath(surahNumber: Int): Path {
        val directory = storage.resolvePath(StoragePaths.AUDIO_DIRECTORY)
        return directory.resolve(String.format("%03d.mp3", surahNumber))
    }

}
