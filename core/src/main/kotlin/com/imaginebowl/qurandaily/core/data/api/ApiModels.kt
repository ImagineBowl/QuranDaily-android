package com.imaginebowl.qurandaily.core.data.api

import kotlinx.serialization.Serializable

@Serializable
data class QuranEditionResponse(
    val code: Int,
    val status: String,
    val data: QuranEditionData,
)

@Serializable
data class QuranEditionData(
    val surahs: List<ApiSurah>,
)

@Serializable
data class ApiSurah(
    val number: Int,
    val name: String,
    val englishName: String,
    val englishNameTranslation: String,
    val revelationType: String,
    val ayahs: List<ApiAyah>,
)

@Serializable
data class ApiAyah(
    val number: Int,
    val text: String,
    val numberInSurah: Int,
    val juz: Int,
    val page: Int,
)

@Serializable
data class MetaResponse(
    val code: Int,
    val status: String,
    val data: MetaData,
)

@Serializable
data class MetaData(
    val surahs: MetaSurahs,
    val juzs: MetaJuzs,
)

@Serializable
data class MetaSurahs(
    val references: List<MetaSurahReference>,
)

@Serializable
data class MetaSurahReference(
    val number: Int,
    val name: String,
    val englishName: String,
    val englishNameTranslation: String,
    val numberOfAyahs: Int,
    val revelationType: String,
)

@Serializable
data class MetaJuzs(
    val references: List<MetaJuzReference>,
)

@Serializable
data class MetaJuzReference(
    val surah: Int,
    val ayah: Int,
)
