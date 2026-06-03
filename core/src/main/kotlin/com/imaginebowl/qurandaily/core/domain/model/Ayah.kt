package com.imaginebowl.qurandaily.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Ayah(
    val number: Int,
    val numberInSurah: Int,
    val surahNumber: Int,
    val arabicText: String,
    val urduText: String,
    val juz: Int,
    val page: Int,
) {
    val displayReference: String
        get() = "$surahNumber:$numberInSurah"
}

@Serializable
data class ReadingPosition(
    val surahNumber: Int,
    val ayahNumber: Int,
    val scrollAnchor: String? = null,
) {
    val hasSavedPosition: Boolean
        get() = surahNumber > 0

    companion object {
        val DEFAULT = ReadingPosition(surahNumber = 0, ayahNumber = 0, scrollAnchor = null)
    }
}
