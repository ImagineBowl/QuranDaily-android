package com.imaginebowl.qurandaily.core.domain.model

import java.time.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class RecentListen(
    val surahNumber: Int,
    val surahName: String,
    val ayahNumber: Int,
    @Contextual val listenedAt: Instant = Instant.now(),
)
