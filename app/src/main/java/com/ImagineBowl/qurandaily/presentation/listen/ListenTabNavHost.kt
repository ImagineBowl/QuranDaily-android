package com.imaginebowl.qurandaily.presentation.listen

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.imaginebowl.qurandaily.di.AppContainer
import com.imaginebowl.qurandaily.di.ListenViewModelFactory
import com.imaginebowl.qurandaily.di.SurahDetailViewModelFactory
import com.imaginebowl.qurandaily.navigation.Routes
import com.imaginebowl.qurandaily.presentation.audio.SharedAudioViewModel
import com.imaginebowl.qurandaily.presentation.surah.SurahDetailViewModel
import com.imaginebowl.qurandaily.presentation.surah.SurahReadListenScreen
import com.imaginebowl.qurandaily.ui.theme.Accent

@Composable
fun ListenTabNavHost(
    container: AppContainer,
    sharedAudioViewModel: SharedAudioViewModel,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val activity = LocalContext.current as ComponentActivity
    val listenViewModel: ListenViewModel = viewModel(
        viewModelStoreOwner = activity,
        factory = ListenViewModelFactory(container),
    )
    val backStackEntry by navController.currentBackStackEntryAsState()
    val atListenRoot = backStackEntry?.destination?.route == Routes.LISTEN_HOME

    NavHost(
        navController = navController,
        startDestination = Routes.LISTEN_HOME,
        modifier = modifier,
    ) {
        composable(Routes.LISTEN_HOME) {
            ListenScreen(
                viewModel = listenViewModel,
                sharedAudioViewModel = sharedAudioViewModel,
                showMiniPlayer = atListenRoot,
                onReadAndListen = { surah, ayah ->
                    navController.navigate(Routes.surahRead(surah, ayah, autoPlay = true))
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

            val listenState by listenViewModel.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) {
                if (listenState.surahs.isEmpty()) listenViewModel.load()
            }
            val surah = listenState.surahs.firstOrNull { it.number == surahNumber }

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
                tracksReadingPosition = false,
                tracksRecentListens = true,
                onRecordRecentListen = { sn, name, ayah ->
                    listenViewModel.recordRecentListen(sn, name, ayah)
                },
                surahsForPlayer = listenState.surahs,
            )
        }
    }
}
