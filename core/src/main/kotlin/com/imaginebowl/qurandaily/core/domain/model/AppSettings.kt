package com.imaginebowl.qurandaily.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
    ;

    val displayName: String
        get() = when (this) {
            SYSTEM -> "System"
            LIGHT -> "Light"
            DARK -> "Dark"
        }
}

@Serializable
enum class ArabicFontChoice {
    AMIRI_QURAN,
    SYSTEM_SERIF,
    ;

    val displayName: String
        get() = when (this) {
            AMIRI_QURAN -> "Amiri Quran"
            SYSTEM_SERIF -> "System Serif"
        }
}

@Serializable
enum class UrduFontChoice {
    NOTO_NASTALIQ,
    SYSTEM,
    ;

    val displayName: String
        get() = when (this) {
            NOTO_NASTALIQ -> "Noto Nastaliq Urdu"
            SYSTEM -> "System"
        }
}

@Serializable
data class AppSettings(
    val fontSize: Double = 22.0,
    val theme: AppThemeMode = AppThemeMode.DARK,
    val showEnglishTranslation: Boolean = false,
    val arabicFont: ArabicFontChoice = ArabicFontChoice.AMIRI_QURAN,
    val urduFont: UrduFontChoice = UrduFontChoice.NOTO_NASTALIQ,
) {
    companion object {
        val DEFAULT = AppSettings()
    }
}

@Serializable
enum class SearchMode {
    AYAH,
    SURAH,
    TEXT,
    ;

    val title: String
        get() = when (this) {
            SURAH -> "Surah"
            AYAH -> "Ayah"
            TEXT -> "Text"
        }
}

sealed class DownloadProgress {
    data object Idle : DownloadProgress()

    data class Downloading(
        val message: String,
        val fraction: Double,
    ) : DownloadProgress()

    data object Completed : DownloadProgress()

    data class Failed(val message: String) : DownloadProgress()
}

data class StorageInfo(
    val quranDataBytes: Long,
    val audioBytes: Long,
) {
    val totalBytes: Long
        get() = quranDataBytes + audioBytes
}
