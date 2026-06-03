package com.imaginebowl.qurandaily

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.imaginebowl.qurandaily.core.domain.model.AppThemeMode
import com.imaginebowl.qurandaily.presentation.root.RootScreen
import com.imaginebowl.qurandaily.ui.theme.QuranDailyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuranDailyTheme(themeMode = AppThemeMode.DARK) {
                RootScreen(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
