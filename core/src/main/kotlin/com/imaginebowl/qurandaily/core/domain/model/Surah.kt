package com.imaginebowl.qurandaily.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Surah(
    val number: Int,
    val name: String,
    val englishName: String,
    val englishNameTranslation: String,
    val revelationType: String,
    val numberOfAyahs: Int,
)

@Serializable
data class SurahWithAyahs(
    val surah: Surah,
    val ayahs: List<Ayah>,
)
