package com.imaginebowl.qurandaily

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.imaginebowl.qurandaily.core.domain.model.AppThemeMode
import com.imaginebowl.qurandaily.di.AppContainerOwner
import com.imaginebowl.qurandaily.presentation.root.RootScreen
import com.imaginebowl.qurandaily.ui.theme.QuranDailyTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as AppContainerOwner).appContainer
        setContent {
            var themeMode by remember { mutableStateOf<AppThemeMode?>(null) }

            LaunchedEffect(Unit) {
                themeMode = withContext(Dispatchers.IO) {
                    container.settingsRepository.fetchSettings().theme
                }
            }

            val mode = themeMode
            if (mode != null) {
                QuranDailyTheme(themeMode = mode) {
                    RootScreen(
                        onThemeChanged = { themeMode = it },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}
