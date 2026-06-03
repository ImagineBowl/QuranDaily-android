package com.imaginebowl.qurandaily.core.domain.model

import java.time.Instant
import java.util.UUID
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class Bookmark(
    @Contextual val id: UUID = UUID.randomUUID(),
    val surahNumber: Int,
    val ayahNumber: Int,
    val surahName: String,
    val arabicPreview: String,
    @Contextual val createdAt: Instant = Instant.now(),
) {
    val displayReference: String
        get() = "$surahName $surahNumber:$ayahNumber"
}
