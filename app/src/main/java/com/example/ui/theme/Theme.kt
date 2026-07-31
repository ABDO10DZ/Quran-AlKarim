package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val EmeraldDarkColorScheme = darkColorScheme(
    primary = GoldAccent,
    onPrimary = DarkBackground,
    primaryContainer = EmeraldPrimaryVariant,
    onPrimaryContainer = GoldLight,
    secondary = EmeraldPrimary,
    onSecondary = TextWhite,
    background = DarkBackground,
    onBackground = TextWhite,
    surface = DarkSurface,
    onSurface = TextWhite,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextMuted,
    tertiary = GoldLight
)

val ClassicLightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = LightSurfaceVariant,
    onPrimaryContainer = EmeraldPrimaryVariant,
    secondary = GoldAccent,
    onSecondary = DarkBackground,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    tertiary = GoldAccent
)

val HighContrastColorScheme = darkColorScheme(
    primary = HighContrastGold,
    onPrimary = HighContrastBg,
    primaryContainer = HighContrastSurface,
    onPrimaryContainer = HighContrastGold,
    secondary = HighContrastGold,
    onSecondary = HighContrastBg,
    background = HighContrastBg,
    onBackground = HighContrastText,
    surface = HighContrastSurface,
    onSurface = HighContrastText,
    surfaceVariant = Color(0xFF262626),
    onSurfaceVariant = HighContrastGold,
    tertiary = HighContrastGold
)

val SepiaColorScheme = lightColorScheme(
    primary = SepiaPrimary,
    onPrimary = SepiaBg,
    primaryContainer = SepiaSurface,
    onPrimaryContainer = SepiaPrimary,
    secondary = GoldAccent,
    onSecondary = SepiaBg,
    background = SepiaBg,
    onBackground = SepiaText,
    surface = SepiaSurface,
    onSurface = SepiaText,
    surfaceVariant = Color(0xFFDFD1B5),
    onSurfaceVariant = SepiaPrimary,
    tertiary = GoldAccent
)

@Composable
fun QuranAppTheme(
    themeMode: String = "EMERALD_DARK",
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        "CLASSIC_LIGHT" -> ClassicLightColorScheme
        "HIGH_CONTRAST" -> HighContrastColorScheme
        "SEPIA" -> SepiaColorScheme
        else -> EmeraldDarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
