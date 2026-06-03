package com.imaginebowl.qurandaily

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.imaginebowl.qurandaily.ui.theme.Accent
import com.imaginebowl.qurandaily.core.domain.model.AppThemeMode
import com.imaginebowl.qurandaily.di.AppContainerOwner
import com.imaginebowl.qurandaily.presentation.root.RootScreen
import com.imaginebowl.qurandaily.ui.theme.QuranDailyTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPlaybackNotificationPermissionIfNeeded()
        enableEdgeToEdge()
        val container = (application as AppContainerOwner).appContainer
        setContent {
            var themeMode by remember { mutableStateOf(AppThemeMode.DARK) }
            var isThemeReady by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                themeMode = withContext(Dispatchers.IO) {
                    container.settingsRepository.fetchSettings().theme
                }
                isThemeReady = true
            }

            QuranDailyTheme(themeMode = themeMode) {
                if (isThemeReady) {
                    RootScreen(
                        onThemeChanged = { themeMode = it },
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = Accent)
                    }
                }
            }
        }
    }

    private fun requestPlaybackNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
