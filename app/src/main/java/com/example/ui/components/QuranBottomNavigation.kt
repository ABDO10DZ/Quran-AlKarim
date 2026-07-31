package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag

sealed class Screen(val route: String, val titleArabic: String, val titleEnglish: String, val icon: ImageVector) {
    object SurahList : Screen("surah_list", "السور", "Surahs", Icons.Default.MenuBook)
    object QuranReader : Screen("quran_reader", "القارئ", "Reader", Icons.Default.AutoStories)
    object Search : Screen("search", "البحث", "Search", Icons.Default.Search)
    object Bookmarks : Screen("bookmarks", "العلامات", "Bookmarks", Icons.Default.Bookmark)
    object Tajweed : Screen("tajweed", "التجويد", "Tajweed", Icons.Default.MenuBook)
    object PrayerQibla : Screen("prayer_qibla", "القبلة", "Qibla", Icons.Default.CompassCalibration)
    object HijriCalendar : Screen("hijri_calendar", "التقويم", "Hijri", Icons.Default.Event)
    object RecitersAudio : Screen("reciters_audio", "التلاوات", "Reciters", Icons.Default.Mic)
    object Settings : Screen("settings", "الإعدادات", "Settings", Icons.Default.Settings)
    object About : Screen("about", "حول", "About", Icons.Default.MenuBook)
}

val primaryNavScreens = listOf(
    Screen.SurahList,
    Screen.QuranReader,
    Screen.Search,
    Screen.Bookmarks,
    Screen.PrayerQibla,
    Screen.Settings
)

@Composable
fun QuranBottomNavigation(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    isArabic: Boolean = true,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.testTag("quran_bottom_nav"),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        primaryNavScreens.forEach { screen ->
            val selected = currentRoute == screen.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(screen.route) },
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = if (isArabic) screen.titleArabic else screen.titleEnglish
                    )
                },
                label = {
                    Text(
                        text = if (isArabic) screen.titleArabic else screen.titleEnglish,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}
