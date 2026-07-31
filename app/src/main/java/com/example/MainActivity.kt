package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.QuranBottomNavigation
import com.example.ui.components.Screen
import com.example.ui.screens.*
import com.example.ui.theme.QuranAppTheme
import com.example.ui.viewmodel.AudioViewModel
import com.example.ui.viewmodel.PrayerViewModel
import com.example.ui.viewmodel.QuranViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuranAppMainContent()
        }
    }
}

@Composable
fun QuranAppMainContent() {
    val quranViewModel: QuranViewModel = viewModel()
    val prayerViewModel: PrayerViewModel = viewModel()
    val audioViewModel: AudioViewModel = viewModel()

    // Request permissions on first launch
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    LaunchedEffect(Unit) {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionsLauncher.launch(permissions.toTypedArray())
    }

    val appSettings by quranViewModel.appSettings.collectAsState()
    val settings = appSettings ?: com.example.data.model.AppSettings()

    val themeMode = settings.themeMode
    val isArabic = settings.isArabicDefault

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.SurahList.route

    QuranAppTheme(themeMode = themeMode) {
        Scaffold(
            bottomBar = {
                QuranBottomNavigation(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.SurahList.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    isArabic = isArabic
                )
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.SurahList.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.SurahList.route) {
                    SurahListScreen(
                        viewModel = quranViewModel,
                        onSurahClick = { surahId ->
                            quranViewModel.loadSurah(surahId)
                            navController.navigate(Screen.QuranReader.route)
                        },
                        onNavigate = { route -> navController.navigate(route) }
                    )
                }

                composable(Screen.QuranReader.route) {
                    QuranReaderScreen(
                        viewModel = quranViewModel,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(Screen.Search.route) {
                    SearchScreen(
                        viewModel = quranViewModel,
                        onNavigateToSurah = { surahId ->
                            quranViewModel.loadSurah(surahId)
                            navController.navigate(Screen.QuranReader.route)
                        }
                    )
                }

                composable(Screen.Bookmarks.route) {
                    BookmarksScreen(
                        viewModel = quranViewModel,
                        onNavigateToSurah = { surahId ->
                            quranViewModel.loadSurah(surahId)
                            navController.navigate(Screen.QuranReader.route)
                        }
                    )
                }

                composable(Screen.Tajweed.route) {
                    TajweedGuideScreen(
                        viewModel = quranViewModel
                    )
                }

                composable(Screen.PrayerQibla.route) {
                    PrayerTimesScreen(
                        prayerViewModel = prayerViewModel,
                        quranViewModel = quranViewModel,
                        onOpenQibla = { navController.navigate("qibla_finder") }
                    )
                }

                composable("qibla_finder") {
                    QiblaFinderScreen(
                        prayerViewModel = prayerViewModel,
                        quranViewModel = quranViewModel,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(Screen.HijriCalendar.route) {
                    HijriCalendarScreen(
                        prayerViewModel = prayerViewModel,
                        quranViewModel = quranViewModel
                    )
                }

                composable(Screen.RecitersAudio.route) {
                    RecitersAudioScreen(
                        audioViewModel = audioViewModel,
                        quranViewModel = quranViewModel
                    )
                }

                composable(Screen.Settings.route) {
                    SettingsAccessibilityScreen(
                        viewModel = quranViewModel
                    )
                }

                composable(Screen.About.route) {
                    AboutScreen(
                        viewModel = quranViewModel,
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
