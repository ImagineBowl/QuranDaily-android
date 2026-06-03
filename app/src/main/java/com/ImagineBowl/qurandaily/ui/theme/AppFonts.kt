package com.imaginebowl.qurandaily.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.imaginebowl.qurandaily.R
import com.imaginebowl.qurandaily.core.domain.model.ArabicFontChoice
import com.imaginebowl.qurandaily.core.domain.model.UrduFontChoice

private val AmiriQuranFamily = FontFamily(Font(R.font.amiri_quran_regular))
private val NotoNastaliqFamily = FontFamily(Font(R.font.noto_nastaliq_urdu_regular))

@Composable
fun arabicTextStyle(
    choice: ArabicFontChoice,
    fontSize: Double,
): TextStyle {
    val family = when (choice) {
        ArabicFontChoice.AMIRI_QURAN -> AmiriQuranFamily
        ArabicFontChoice.SYSTEM_SERIF -> FontFamily.Serif
    }
    val size = (fontSize + 6).sp
    return TextStyle(
        fontFamily = family,
        fontSize = size,
        lineHeight = (fontSize + 16).sp,
        fontWeight = FontWeight.Normal,
    )
}

@Composable
fun urduTextStyle(
    choice: UrduFontChoice,
    fontSize: Double,
): TextStyle {
    val family = when (choice) {
        UrduFontChoice.NOTO_NASTALIQ -> NotoNastaliqFamily
        UrduFontChoice.SYSTEM -> FontFamily.Default
    }
    val size = fontSize.sp
    return TextStyle(
        fontFamily = family,
        fontSize = size,
        lineHeight = (fontSize + 8).sp,
        fontWeight = FontWeight.Normal,
    )
}
