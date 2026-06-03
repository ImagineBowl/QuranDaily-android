package com.imaginebowl.qurandaily.core.domain.parser

import com.imaginebowl.qurandaily.core.domain.model.Surah

sealed class AyahReference {
    data class SurahAyah(val surah: Int, val ayah: Int) : AyahReference()

    data class AbsoluteNumber(val number: Int) : AyahReference()
}

object AyahReferenceParser {
    private val SEPARATORS = charArrayOf(':', '.', '/', '-', ' ', '\t')

    fun parse(query: String, surahs: List<Surah> = emptyList()): AyahReference? {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return null

        val hasSeparator = trimmed.any { it in SEPARATORS }
        if (hasSeparator) {
            val lastSeparatorIndex = trimmed.indexOfLast { it in SEPARATORS }
            if (lastSeparatorIndex < 0) return null

            val surahPart = trimmed.substring(0, lastSeparatorIndex).trim()
            val ayahPart = trimmed.substring(lastSeparatorIndex + 1).trim()

            val ayah = ayahPart.toIntOrNull() ?: return null
            if (surahPart.isEmpty() || ayah < 1) return null

            val surahNumber = surahPart.toIntOrNull()
            if (surahNumber != null && surahNumber in 1..114) {
                return AyahReference.SurahAyah(surah = surahNumber, ayah = ayah)
            }

            val matchedSurah = matchSurahNumber(name = surahPart, surahs = surahs) ?: return null
            return AyahReference.SurahAyah(surah = matchedSurah, ayah = ayah)
        }

        val absolute = trimmed.toIntOrNull()
        if (absolute != null && absolute in 1..6236) {
            return AyahReference.AbsoluteNumber(absolute)
        }

        return null
    }

    fun matchSurahNumber(name: String, surahs: List<Surah>): Int? {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return null

        trimmed.toIntOrNull()?.let { number ->
            if (number in 1..114) return number
        }

        val query = foldTransliteration(trimmed)

        for (surah in surahs) {
            if (foldTransliteration(surah.englishName) == query) return surah.number
            if (foldTransliteration(surah.englishNameTranslation) == query) return surah.number
        }

        for (surah in surahs) {
            val english = foldTransliteration(surah.englishName)
            val translation = foldTransliteration(surah.englishNameTranslation)

            if (english.startsWith(query) || query.startsWith(english)) return surah.number
            if (translation.startsWith(query) || query.startsWith(translation)) return surah.number
            if (surah.name.contains(trimmed)) return surah.number
        }

        return null
    }

    private fun foldTransliteration(value: String): String =
        normalizeName(value)
            .replace("ee", "i")
            .replace("oo", "u")

    private fun normalizeName(value: String): String =
        value
            .lowercase()
            .replace("-", "")
            .replace(" ", "")
            .replace("'", "")
}
