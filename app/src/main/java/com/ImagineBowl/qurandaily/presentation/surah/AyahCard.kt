package com.imaginebowl.qurandaily.presentation.surah

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imaginebowl.qurandaily.core.domain.model.ArabicFontChoice
import com.imaginebowl.qurandaily.core.domain.model.Ayah
import com.imaginebowl.qurandaily.core.domain.model.UrduFontChoice
import com.imaginebowl.qurandaily.ui.theme.Accent
import com.imaginebowl.qurandaily.ui.theme.AppDimensions
import com.imaginebowl.qurandaily.ui.theme.arabicTextStyle
import com.imaginebowl.qurandaily.ui.theme.urduTextStyle

@Composable
fun AyahCard(
    ayah: Ayah,
    fontSize: Double,
    arabicFont: ArabicFontChoice = ArabicFontChoice.AMIRI_QURAN,
    urduFont: UrduFontChoice = UrduFontChoice.NOTO_NASTALIQ,
    isBookmarked: Boolean,
    isHighlighted: Boolean,
    onBookmark: () -> Unit,
    onAyahTap: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val background = if (isHighlighted) {
        Accent.copy(alpha = 0.14f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val border = if (isHighlighted) BorderStroke(2.dp, Accent) else null

    Surface(
        modifier = modifier
            .then(if (onAyahTap != null) Modifier.clickable(onClick = onAyahTap) else Modifier),
        shape = RoundedCornerShape(AppDimensions.cardCornerRadius),
        color = background,
        border = border,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = ayah.displayReference,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = onBookmark,
                    modifier = Modifier.size(AppDimensions.minimumTapSize),
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = if (isBookmarked) "Remove bookmark" else "Bookmark ayah",
                        tint = Accent,
                    )
                }
            }
            Text(
                text = ayah.arabicText,
                style = arabicTextStyle(arabicFont, fontSize),
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            )
            Text(
                text = ayah.urduText,
                style = urduTextStyle(urduFont, fontSize),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
