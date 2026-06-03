package com.imaginebowl.qurandaily.ui.util

import com.imaginebowl.qurandaily.core.domain.model.StorageInfo
import java.util.Locale

fun StorageInfo.formattedQuranData(): String = formatBytes(quranDataBytes)

fun StorageInfo.formattedAudio(): String = formatBytes(audioBytes)

fun StorageInfo.formattedTotal(): String = formatBytes(totalBytes)

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "Zero KB"
    val unit = 1024.0
    var value = bytes.toDouble()
    var unitIndex = 0
    val labels = arrayOf("B", "KB", "MB", "GB")
    while (value >= unit && unitIndex < labels.lastIndex) {
        value /= unit
        unitIndex++
    }
    return String.format(Locale.getDefault(), "%.1f %s", value, labels[unitIndex])
}
