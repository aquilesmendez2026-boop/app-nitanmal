package com.nitanmal.app.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Dos modos de color, ambos nocturnos:
 * - WEB: la paleta original del sitio (cyan + magenta neón sobre tinta).
 * - NEON: la paleta del logo (morado + azul neón sobre negro).
 */
enum class TemaApp { WEB, NEON }

// ── Paleta WEB (espejo de src/theme/tokens.ts del sitio) ──
private val WebColorScheme = darkColorScheme(
    primary = Color(0xFF22d3ee),        // neon.cyan
    secondary = Color(0xFFd946ef),      // neon.magenta
    tertiary = Color(0xFF22c55e),       // éxito/aprobado
    background = Color(0xFF06060c),     // ink.950
    surface = Color(0xFF161626),        // ink.700 (paneles glass)
    onPrimary = Color(0xFF06060c),
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFFe7e7ef),   // ink.50
    onSurface = Color(0xFFe7e7ef),
    onSurfaceVariant = Color(0xFF9a9ab0), // ink.200
    outline = Color(0xFF33334a),        // ink.500
    primaryContainer = Color(0xFF0e3a44),
    onPrimaryContainer = Color(0xFF67e8f9),
    error = Color(0xFFef4444)
)

// ── Paleta NEÓN (del logo: morado + azul sobre negro) ──
private val NeonColorScheme = darkColorScheme(
    primary = Color(0xFFa855f7),        // morado neón
    secondary = Color(0xFF3b82f6),      // azul neón
    tertiary = Color(0xFF22c55e),
    background = Color(0xFF000000),
    surface = Color(0xFF14101f),        // negro violáceo
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFFf5f3ff),
    onSurface = Color(0xFFf5f3ff),
    onSurfaceVariant = Color(0xFFa78bca),
    outline = Color(0xFF3b2a5a),
    primaryContainer = Color(0xFF2e1065),
    onPrimaryContainer = Color(0xFFd8b4fe),
    error = Color(0xFFef4444)
)

@Composable
fun NitanmalTheme(
    tema: TemaApp = TemaApp.WEB,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = when (tema) {
            TemaApp.WEB -> WebColorScheme
            TemaApp.NEON -> NeonColorScheme
        },
        typography = NitanmalTypography,
        content = content
    )
}
