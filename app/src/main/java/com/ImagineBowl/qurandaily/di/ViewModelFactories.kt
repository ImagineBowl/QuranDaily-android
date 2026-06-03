package com.imaginebowl.qurandaily.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.imaginebowl.qurandaily.core.domain.model.Surah
import com.imaginebowl.qurandaily.presentation.audio.SharedAudioViewModel
import com.imaginebowl.qurandaily.presentation.download.DownloadViewModel
import com.imaginebowl.qurandaily.presentation.bookmarks.BookmarksViewModel
import com.imaginebowl.qurandaily.presentation.listen.ListenViewModel
import com.imaginebowl.qurandaily.presentation.settings.SettingsViewModel
import com.imaginebowl.qurandaily.presentation.read.QuranViewModel
import com.imaginebowl.qurandaily.presentation.surah.SurahDetailViewModel

class DownloadViewModelFactory(
    private val container: AppContainer,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DownloadViewModel::class.java)) {
            return DownloadViewModel(
                container.downloadQuranUseCase,
                container.fetchQuranUseCase,
            ) as T
        }
        throw illegal(modelClass)
    }
}

class BookmarksViewModelFactory(
    private val container: AppContainer,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BookmarksViewModel::class.java)) {
            return BookmarksViewModel(container.bookmarkRepository) as T
        }
        throw illegal(modelClass)
    }
}

class SettingsViewModelFactory(
    private val container: AppContainer,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(
                container.settingsRepository,
                container.storageInfoUseCase,
                container.clearCacheUseCase,
            ) as T
        }
        throw illegal(modelClass)
    }
}

class ListenViewModelFactory(
    private val container: AppContainer,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListenViewModel::class.java)) {
            return ListenViewModel(
                container.fetchQuranUseCase,
                container.recentListenRepository,
                container.settingsRepository,
            ) as T
        }
        throw illegal(modelClass)
    }
}

class QuranViewModelFactory(
    private val container: AppContainer,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuranViewModel::class.java)) {
            return QuranViewModel(
                container.fetchQuranUseCase,
                container.readingPositionRepository,
            ) as T
        }
        throw illegal(modelClass)
    }
}

class SurahDetailViewModelFactory(
    private val container: AppContainer,
    private val surah: Surah,
    private val initialAyah: Int,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SurahDetailViewModel::class.java)) {
            return SurahDetailViewModel(
                surah = surah,
                fetchQuranUseCase = container.fetchQuranUseCase,
                bookmarkRepository = container.bookmarkRepository,
                readingPositionRepository = container.readingPositionRepository,
                settingsRepository = container.settingsRepository,
                initialAyah = initialAyah,
            ) as T
        }
        throw illegal(modelClass)
    }
}

class SharedAudioViewModelFactory(
    private val container: AppContainer,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SharedAudioViewModel::class.java)) {
            return SharedAudioViewModel(
                appContext = container.appContext,
                audioPlayer = container.audioPlayer,
                audioRepository = container.audioRepository,
                fetchQuranUseCase = container.fetchQuranUseCase,
            ) as T
        }
        throw illegal(modelClass)
    }
}

private fun illegal(modelClass: Class<*>): Nothing =
    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
