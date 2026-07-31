package com.nitanmal.app.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Nitanmal Brand Colors — misma paleta base que el ecosistema Umine
val NitanmalPrimary = Color(0xFF06b6d4)      // Cyan-500
val NitanmalSecondary = Color(0xFFa855f7)    // Purple-500
val NitanmalAccent = Color(0xFF22c55e)       // Green-500

// Neutral Colors
val NitanmalBackgroundLight = Color(0xFFf8fafc)  // Slate-50
val NitanmalSurfaceLight = Color(0xFFFFFFFF)     // White
val NitanmalTextLight = Color(0xFF1e293b)        // Slate-800

val NitanmalBackgroundDark = Color(0xFF0f172a)   // Slate-900
val NitanmalSurfaceDark = Color(0xFF1e293b)      // Slate-800
val NitanmalTextDark = Color(0xFFf8fafc)         // Slate-50

// Light Theme
private val LightColorScheme = lightColorScheme(
    primary = NitanmalPrimary,
    secondary = NitanmalSecondary,
    tertiary = NitanmalAccent,
    background = NitanmalBackgroundLight,
    surface = NitanmalSurfaceLight,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = NitanmalTextLight,
    onSurface = NitanmalTextLight,
    outline = Color(0xFFe2e8f0)  // Slate-200
)

// Dark Theme
private val DarkColorScheme = darkColorScheme(
    primary = NitanmalPrimary,
    secondary = NitanmalSecondary,
    tertiary = NitanmalAccent,
    background = NitanmalBackgroundDark,
    surface = NitanmalSurfaceDark,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = NitanmalTextDark,
    onSurface = NitanmalTextDark,
    outline = Color(0xFF334155)  // Slate-700
)

@Composable
fun NitanmalTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = NitanmalTypography,
        content = content
    )
}
