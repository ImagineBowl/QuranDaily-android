package com.imaginebowl.qurandaily.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.imaginebowl.qurandaily.core.domain.model.AppThemeMode
import com.imaginebowl.qurandaily.data.billing.TipProduct
import com.imaginebowl.qurandaily.core.domain.model.ArabicFontChoice
import com.imaginebowl.qurandaily.core.domain.model.UrduFontChoice
import com.imaginebowl.qurandaily.ui.theme.Accent
import com.imaginebowl.qurandaily.ui.util.formattedAudio
import com.imaginebowl.qurandaily.ui.util.formattedQuranData
import com.imaginebowl.qurandaily.ui.util.formattedTotal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalContext.current as ComponentActivity

    DisposableEffect(activity) {
        viewModel.attachPurchaseHost(activity)
        onDispose { viewModel.detachPurchaseHost() }
    }

    LaunchedEffect(Unit) {
        viewModel.load()
        viewModel.loadTips()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        if (uiState.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator(color = Accent)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SettingsSection(title = "Reading") {
                Text("Font Size: ${uiState.settings.fontSize.toInt()}", fontSize = 18.sp)
                Slider(
                    value = uiState.settings.fontSize.toFloat(),
                    onValueChange = { viewModel.updateFontSize(it.toDouble()) },
                    valueRange = 16f..36f,
                    steps = 19,
                )
                FontPickerRow(
                    label = "Arabic Font",
                    options = ArabicFontChoice.entries.map { it.displayName to it },
                    selected = uiState.settings.arabicFont,
                    onSelect = { viewModel.updateArabicFont(it) },
                )
                FontPickerRow(
                    label = "Urdu Font",
                    options = UrduFontChoice.entries.map { it.displayName to it },
                    selected = uiState.settings.urduFont,
                    onSelect = { viewModel.updateUrduFont(it) },
                )
            }

            SettingsSection(title = "Appearance") {
                FontPickerRow(
                    label = "Theme",
                    options = AppThemeMode.entries.map { it.displayName to it },
                    selected = uiState.settings.theme,
                    onSelect = { viewModel.updateTheme(it) },
                )
            }

            SettingsSection(title = "Storage") {
                LabeledValue("Quran Data", uiState.storageInfo.formattedQuranData())
                LabeledValue("Audio", uiState.storageInfo.formattedAudio())
                LabeledValue("Total", uiState.storageInfo.formattedTotal(), bold = true)
                DestructiveButton("Clear Quran Cache", onClick = { viewModel.clearQuranCache() })
                DestructiveButton("Clear Audio Cache", onClick = { viewModel.clearAudioCache() })
                DestructiveButton("Clear All Cache", onClick = { viewModel.clearAllCache() })
            }

            SettingsSection(title = "Support") {
                Text(
                    text = "Help keep QuranDaily free",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                )
                Text(
                    text = "QuranDaily is free with no account required. If it helps your daily reading, you can optionally leave a tip to support its development.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                )
                uiState.tipsUnavailableReason?.let { reason ->
                    Text(
                        text = reason,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                    )
                }
                if (uiState.tipOptions.isEmpty()) {
                    Text(
                        text = "Tip options are unavailable right now.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    uiState.tipOptions.forEach { product ->
                        TipRow(
                            product = product,
                            enabled = !uiState.isPurchasing,
                            onClick = { viewModel.tip(product) },
                        )
                    }
                }
                Text(
                    text = "Completely optional. No features are locked behind support.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                )
            }

            SettingsSection(title = "App Info") {
                LabeledValue("App", "QuranDaily")
                LabeledValue("Version", "1.0.0")
                LabeledValue("Data Source", "AlQuran Cloud")
            }

            uiState.statusMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            content()
        }
    }
}

@Composable
private fun LabeledValue(
    label: String,
    value: String,
    bold: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label)
        Text(
            text = value,
            fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

@Composable
private fun DestructiveButton(
    text: String,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(text, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun <T> FontPickerRow(
    label: String,
    options: List<Pair<String, T>>,
    selected: T,
    onSelect: (T) -> Unit,
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(label, fontWeight = FontWeight.Medium)
        options.forEach { (name, value) ->
            TextButton(
                onClick = { onSelect(value) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(name)
                    if (value == selected) {
                        Text("✓", color = Accent, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        HorizontalDivider()
    }
}

@Composable
private fun TipRow(
    product: TipProduct,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            androidx.compose.material3.Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = Accent,
            )
            Text(
                text = product.displayName,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
            )
            Surface(
                color = Accent.copy(alpha = 0.15f),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(
                    text = product.formattedPrice,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    color = Accent,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}
