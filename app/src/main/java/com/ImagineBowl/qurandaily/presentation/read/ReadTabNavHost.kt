package com.imaginebowl.qurandaily.presentation.read

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.imaginebowl.qurandaily.di.AppContainer
import com.imaginebowl.qurandaily.di.QuranViewModelFactory
import com.imaginebowl.qurandaily.di.SurahDetailViewModelFactory
import com.imaginebowl.qurandaily.navigation.Routes
import com.imaginebowl.qurandaily.presentation.audio.SharedAudioViewModel
import com.imaginebowl.qurandaily.presentation.surah.SurahDetailViewModel
import com.imaginebowl.qurandaily.presentation.surah.SurahReadListenScreen
import com.imaginebowl.qurandaily.ui.theme.Accent
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment

@Composable
fun ReadTabNavHost(
    container: AppContainer,
    sharedAudioViewModel: SharedAudioViewModel,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val activity = LocalContext.current as ComponentActivity
    val quranViewModel: QuranViewModel = viewModel(
        viewModelStoreOwner = activity,
        factory = QuranViewModelFactory(container),
    )

    NavHost(
        navController = navController,
        startDestination = Routes.SURAH_LIST,
        modifier = modifier,
    ) {
        composable(Routes.SURAH_LIST) {
            SurahListScreen(
                viewModel = quranViewModel,
                sharedAudioViewModel = sharedAudioViewModel,
                onOpenSurah = { surah, ayah, autoPlay ->
                    navController.navigate(Routes.surahRead(surah, ayah, autoPlay))
                },
            )
        }
        composable(
            route = Routes.SURAH_READ_PATTERN,
            arguments = listOf(
                navArgument("surahNumber") { type = NavType.IntType },
                navArgument("ayahNumber") { type = NavType.IntType },
                navArgument("autoPlay") { type = NavType.BoolType },
            ),
        ) { entry ->
            val surahNumber = entry.arguments?.getInt("surahNumber") ?: 1
            val ayahNumber = entry.arguments?.getInt("ayahNumber") ?: 1
            val autoPlay = entry.arguments?.getBoolean("autoPlay") ?: false

            val listState by quranViewModel.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) {
                if (listState.surahs.isEmpty()) quranViewModel.load()
            }
            val surah = listState.surahs.firstOrNull { it.number == surahNumber }

            if (surah == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Accent)
                }
                return@composable
            }

            val detailViewModel: SurahDetailViewModel = viewModel(
                factory = SurahDetailViewModelFactory(container, surah, ayahNumber),
            )

            SurahReadListenScreen(
                surahNumber = surahNumber,
                ayahNumber = ayahNumber,
                autoPlay = autoPlay,
                sharedAudioViewModel = sharedAudioViewModel,
                detailViewModel = detailViewModel,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
