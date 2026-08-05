package com.nitanmal.app.presentation.ui.components.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Fondo nocturno con orbes neón difuminados detrás del contenido,
 * igual que los BackgroundBlobs de la web.
 */
@Composable
fun FondoNocturno(
    modifier: Modifier = Modifier,
    /** false cuando ya se está dentro de otro FondoNocturno (evita doblar fondo e insets). */
    activo: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val esquema = MaterialTheme.colorScheme
    if (!activo) {
        Box(modifier = modifier.fillMaxSize()) { content() }
        return
    }
    Box(modifier = modifier.fillMaxSize().background(esquema.background)) {
        Box(
            modifier = Modifier
                .size(360.dp)
                .align(Alignment.TopStart)
                .offset(x = (-100).dp, y = (-80).dp)
                .background(
                    Brush.radialGradient(
                        listOf(esquema.primary.copy(alpha = 0.16f), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(400.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 110.dp, y = 100.dp)
                .background(
                    Brush.radialGradient(
                        listOf(esquema.secondary.copy(alpha = 0.14f), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.Center)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFFf59e0b).copy(alpha = 0.06f), Color.Transparent)
                    )
                )
        )
        content()
    }
}

/**
 * Panel de vidrio como el GlassPanel de la web: fondo translúcido
 * y borde blanco sutil. Incluye el clip a la forma.
 */
@Composable
fun Modifier.glass(radius: Dp = 20.dp): Modifier {
    val shape = RoundedCornerShape(radius)
    return this
        .clip(shape)
        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.55f))
        .border(1.dp, Color.White.copy(alpha = 0.10f), shape)
}
