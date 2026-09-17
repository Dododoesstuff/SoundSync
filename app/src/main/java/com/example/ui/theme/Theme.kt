package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.data.model.AccentTheme
import com.example.data.model.ThemeMode

@Composable
fun SoundSyncTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    accentTheme: AccentTheme = AccentTheme.SPOTIFY_GREEN,
    content: @Composable () -> Unit
) {
    val primaryColor = Color(accentTheme.hex)
    val isSystemDark = isSystemInDarkTheme()

    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK, ThemeMode.AMOLED_BLACK, ThemeMode.CYBERPUNK_NIGHT, ThemeMode.HIGH_CONTRAST -> true
    }

    val colorScheme: ColorScheme = when (themeMode) {
        ThemeMode.LIGHT -> lightColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            primaryContainer = primaryColor.copy(alpha = 0.15f),
            onPrimaryContainer = primaryColor,
            secondary = SpotifyGreen,
            tertiary = YouTubeRed,
            background = LightBackground,
            onBackground = Color(0xFF0F172A),
            surface = LightSurface,
            onSurface = Color(0xFF0F172A),
            surfaceVariant = LightCard,
            onSurfaceVariant = Color(0xFF475569),
            outline = LightBorder
        )
        ThemeMode.AMOLED_BLACK -> darkColorScheme(
            primary = primaryColor,
            onPrimary = Color.Black,
            primaryContainer = primaryColor.copy(alpha = 0.25f),
            onPrimaryContainer = Color.White,
            secondary = SpotifyGreenLight,
            tertiary = YouTubeRed,
            background = AmoledBackground,
            onBackground = Color.White,
            surface = AmoledSurface,
            onSurface = Color.White,
            surfaceVariant = AmoledCard,
            onSurfaceVariant = Color(0xFFD4D4D8),
            outline = AmoledBorder
        )
        ThemeMode.CYBERPUNK_NIGHT -> darkColorScheme(
            primary = primaryColor,
            onPrimary = Color.Black,
            primaryContainer = primaryColor.copy(alpha = 0.3f),
            onPrimaryContainer = CyanPulse,
            secondary = CyanPulse,
            tertiary = RoseQuartz,
            background = Color(0xFF0B0E17),
            onBackground = Color(0xFFF1F5F9),
            surface = Color(0xFF131826),
            onSurface = Color(0xFFF1F5F9),
            surfaceVariant = Color(0xFF1E2638),
            onSurfaceVariant = Color(0xFF94A3B8),
            outline = Color(0xFF334155)
        )
        ThemeMode.HIGH_CONTRAST -> darkColorScheme(
            primary = HighContrastYellow,
            onPrimary = Color.Black,
            primaryContainer = Color(0xFF333333),
            onPrimaryContainer = HighContrastYellow,
            secondary = Color(0xFF00FF66),
            tertiary = Color(0xFFFF3333),
            background = HighContrastBackground,
            onBackground = HighContrastText,
            surface = HighContrastSurface,
            onSurface = HighContrastText,
            surfaceVariant = Color(0xFF1E1E1E),
            onSurfaceVariant = HighContrastText,
            outline = HighContrastBorder
        )
        ThemeMode.DARK, ThemeMode.SYSTEM -> darkColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            primaryContainer = primaryColor.copy(alpha = 0.2f),
            onPrimaryContainer = Color.White,
            secondary = SpotifyGreen,
            tertiary = YouTubeRed,
            background = DarkBackground,
            onBackground = Color(0xFFF8FAFC),
            surface = DarkSurface,
            onSurface = Color(0xFFF8FAFC),
            surfaceVariant = DarkCard,
            onSurfaceVariant = Color(0xFFCBD5E1),
            outline = DarkBorder
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
