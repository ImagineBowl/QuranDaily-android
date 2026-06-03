package com.imaginebowl.qurandaily.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class StoredQuranBundle(
    val surahs: List<Surah>,
    val ayahsBySurah: Map<Int, List<Ayah>>,
    val juzs: List<Juz>,
)

object StoragePaths {
    const val QURAN_BUNDLE = "quran_bundle.json"
    const val BOOKMARKS = "bookmarks.json"
    const val SETTINGS = "settings.json"
    const val READING_POSITION = "reading_position.json"
    const val RECENT_LISTENS = "recent_listens.json"
    const val AUDIO_DIRECTORY = "Audio"
}
