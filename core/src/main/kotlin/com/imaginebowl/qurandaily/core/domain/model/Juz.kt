package com.imaginebowl.qurandaily.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Juz(
    val number: Int,
    val startSurah: Int,
    val startAyah: Int,
) {
    val displayName: String
        get() = "Juz $number"
}
