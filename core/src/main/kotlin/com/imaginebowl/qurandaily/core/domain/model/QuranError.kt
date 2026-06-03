package com.imaginebowl.qurandaily.core.domain.model

sealed class QuranError : Exception() {
    data object NotDownloaded : QuranError()

    data object InvalidResponse : QuranError()

    data class SurahNotFound(val number: Int) : QuranError()

    data object AyahNotFound : QuranError()

    data object AudioNotAvailable : QuranError()

    data object AudioNotDownloaded : QuranError()

    data class DownloadFailed(val reason: String) : QuranError()

    data class StorageFailed(val reason: String) : QuranError()

    override val message: String?
        get() = when (this) {
            NotDownloaded -> "Quran data is not downloaded yet."
            InvalidResponse -> "Received an invalid response from the server."
            is SurahNotFound -> "Surah ${number} was not found."
            AyahNotFound -> "Ayah was not found."
            AudioNotAvailable -> "Audio is not available for this surah."
            AudioNotDownloaded ->
                "Audio is not downloaded. Tap Download to save this surah for offline listening."
            is DownloadFailed -> "Download failed: $reason"
            is StorageFailed -> "Storage error: $reason"
        }
}
