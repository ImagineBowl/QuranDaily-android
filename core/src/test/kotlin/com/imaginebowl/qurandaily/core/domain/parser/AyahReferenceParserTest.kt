package com.imaginebowl.qurandaily.core.domain.parser

import com.imaginebowl.qurandaily.core.domain.model.Surah
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AyahReferenceParserTest {
    private val yaseen = Surah(
        number = 36,
        name = "سُورَةُ يس",
        englishName = "Ya-Sin",
        englishNameTranslation = "Yaseen",
        revelationType = "Meccan",
        numberOfAyahs = 83,
    )

    @Test
    fun parse_returnsSurahAyah_whenQueryUsesColon() {
        val reference = AyahReferenceParser.parse("2:255")
        assertEquals(AyahReference.SurahAyah(surah = 2, ayah = 255), reference)
    }

    @Test
    fun parse_returnsSurahAyah_whenQueryUsesDash() {
        val reference = AyahReferenceParser.parse("2-255")
        assertEquals(AyahReference.SurahAyah(surah = 2, ayah = 255), reference)
    }

    @Test
    fun parse_returnsAbsoluteNumber_whenQueryIsNumeric() {
        val reference = AyahReferenceParser.parse("262")
        assertEquals(AyahReference.AbsoluteNumber(262), reference)
    }

    @Test
    fun parse_returnsSurahAyah_whenNamedSurahUsesColon() {
        val reference = AyahReferenceParser.parse("Yaseen:35", surahs = listOf(yaseen))
        assertEquals(AyahReference.SurahAyah(surah = 36, ayah = 35), reference)
    }

    @Test
    fun parse_returnsSurahAyah_whenNamedSurahUsesSpace() {
        val reference = AyahReferenceParser.parse("Ya-Sin 35", surahs = listOf(yaseen))
        assertEquals(AyahReference.SurahAyah(surah = 36, ayah = 35), reference)
    }

    @Test
    fun parse_returnsSurahAyah_whenYasinSpelling() {
        val reference = AyahReferenceParser.parse("Yasin:35", surahs = listOf(yaseen))
        assertEquals(AyahReference.SurahAyah(surah = 36, ayah = 35), reference)
    }

    @Test
    fun parse_returnsNull_whenQueryInvalid() {
        assertNull(AyahReferenceParser.parse("abc"))
        assertNull(AyahReferenceParser.parse("999:999"))
        assertNull(AyahReferenceParser.parse("Yaseen:35", surahs = emptyList()))
    }
}
