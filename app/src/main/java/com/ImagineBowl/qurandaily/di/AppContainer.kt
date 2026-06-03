package com.imaginebowl.qurandaily.di

import android.content.Context
import com.imaginebowl.qurandaily.core.data.api.AlQuranApiClient
import com.imaginebowl.qurandaily.core.data.repository.DefaultAudioRepository
import com.imaginebowl.qurandaily.core.data.repository.DefaultBookmarkRepository
import com.imaginebowl.qurandaily.core.data.repository.DefaultQuranRepository
import com.imaginebowl.qurandaily.core.data.repository.DefaultReadingPositionRepository
import com.imaginebowl.qurandaily.core.data.repository.DefaultRecentListenRepository
import com.imaginebowl.qurandaily.core.data.repository.DefaultSettingsRepository
import com.imaginebowl.qurandaily.core.data.service.JsonStorageService
import com.imaginebowl.qurandaily.core.data.service.OkHttpDownloadService
import com.imaginebowl.qurandaily.core.domain.repository.ApiClient
import com.imaginebowl.qurandaily.core.domain.repository.AudioRepository
import com.imaginebowl.qurandaily.core.domain.repository.BookmarkRepository
import com.imaginebowl.qurandaily.core.domain.repository.DownloadService
import com.imaginebowl.qurandaily.core.domain.repository.QuranRepository
import com.imaginebowl.qurandaily.core.domain.repository.ReadingPositionRepository
import com.imaginebowl.qurandaily.core.domain.repository.RecentListenRepository
import com.imaginebowl.qurandaily.core.domain.repository.SettingsRepository
import com.imaginebowl.qurandaily.core.domain.repository.StorageService
import com.imaginebowl.qurandaily.core.domain.usecase.ClearCacheUseCase
import com.imaginebowl.qurandaily.core.domain.usecase.DownloadQuranUseCase
import com.imaginebowl.qurandaily.core.domain.usecase.FetchQuranUseCase
import com.imaginebowl.qurandaily.core.domain.usecase.SearchQuranUseCase
import com.imaginebowl.qurandaily.core.domain.usecase.StorageInfoUseCase
import com.imaginebowl.qurandaily.data.audio.QuranAudioPlayer
import com.imaginebowl.qurandaily.data.billing.PlayTipJarService
import com.imaginebowl.qurandaily.data.billing.TipJarService

/**
 * Mirrors iOS `AppContainer` — manual constructor injection for production and tests.
 */
class AppContainer(
    context: Context,
    storageOverride: StorageService? = null,
    apiClientOverride: ApiClient? = null,
    downloadServiceOverride: DownloadService? = null,
) {
    val appContext: Context = context.applicationContext

    val storageService: StorageService =
        storageOverride ?: JsonStorageService(context.filesDir.toPath())

    val apiClient: ApiClient =
        apiClientOverride ?: AlQuranApiClient()

    val downloadService: DownloadService =
        downloadServiceOverride ?: OkHttpDownloadService()

    val quranRepository: QuranRepository =
        DefaultQuranRepository(storageService)

    val audioRepository: AudioRepository =
        DefaultAudioRepository(
            storage = storageService,
            downloadService = downloadService,
            apiClient = apiClient,
            quranRepository = quranRepository,
        )

    val bookmarkRepository: BookmarkRepository =
        DefaultBookmarkRepository(storageService)

    val readingPositionRepository: ReadingPositionRepository =
        DefaultReadingPositionRepository(storageService)

    val settingsRepository: SettingsRepository =
        DefaultSettingsRepository(storageService)

    val recentListenRepository: RecentListenRepository =
        DefaultRecentListenRepository(storageService)

    val fetchQuranUseCase: FetchQuranUseCase =
        FetchQuranUseCase(quranRepository)

    val downloadQuranUseCase: DownloadQuranUseCase =
        DownloadQuranUseCase(apiClient, quranRepository)

    val searchQuranUseCase: SearchQuranUseCase =
        SearchQuranUseCase(quranRepository)

    val storageInfoUseCase: StorageInfoUseCase =
        StorageInfoUseCase(storageService)

    val clearCacheUseCase: ClearCacheUseCase =
        ClearCacheUseCase(quranRepository, audioRepository)

    val audioPlayer: QuranAudioPlayer = QuranAudioPlayer(
        context = appContext,
        audioRepository = audioRepository,
        fetchQuranUseCase = fetchQuranUseCase,
    )

    val tipJarService: TipJarService = PlayTipJarService(appContext)
}
