package com.imaginebowl.qurandaily.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SearchResult(
    val id: String,
    val surahNumber: Int,
    val ayahNumber: Int,
    val surahName: String,
    val arabicText: String,
    val urduText: String,
    val matchType: MatchType,
    val absoluteAyahNumber: Int? = null,
) {
    val displayReference: String
        get() = "$surahNumber:$ayahNumber"

    @Serializable
    enum class MatchType {
        SURAH_NAME,
        SURAH_NUMBER,
        JUZ,
        TEXT,
        AYAH_REFERENCE,
    }
}
