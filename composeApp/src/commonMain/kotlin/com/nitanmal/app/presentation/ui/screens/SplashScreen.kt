package com.nitanmal.app.presentation.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val SplashBg1 = Color(0xFF0F172A)
private val SplashBg2 = Color(0xFF1E293B)
private val BrandCyan = Color(0xFF06B6D4)
private val BrandPurple = Color(0xFFA855F7)

@Composable
fun SplashScreen(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(SplashBg1, SplashBg2)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Marca con degradado
            Text(
                text = "NITANMAL",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 6.sp,
                    fontSize = 42.sp
                ),
                color = BrandCyan,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(alpha)
            )

            Text(
                text = "Nitan Mal",
                style = MaterialTheme.typography.bodyMedium.copy(letterSpacing = 2.sp),
                color = Color.White.copy(alpha = 0.4f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            CircularProgressIndicator(
                color = BrandCyan,
                trackColor = BrandPurple.copy(alpha = 0.2f),
                strokeWidth = 2.5.dp,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
