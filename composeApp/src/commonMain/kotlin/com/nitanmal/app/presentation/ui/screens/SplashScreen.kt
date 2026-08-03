package com.nitanmal.app.presentation.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import nitanmal.composeapp.generated.resources.Res
import nitanmal.composeapp.generated.resources.logo_nitanmal
import org.jetbrains.compose.resources.painterResource

private val BrandPurple = Color(0xFFa855f7)
private val BrandBlue = Color(0xFF3b82f6)

/** Splash: el logo neón sobre negro (misma estética del logo oficial). */
@Composable
fun SplashScreen(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.75f,
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
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Image(
                painter = painterResource(Res.drawable.logo_nitanmal),
                contentDescription = "Ni Tan Mal",
                modifier = Modifier
                    .size(220.dp)
                    .alpha(alpha)
            )

            CircularProgressIndicator(
                color = BrandPurple,
                trackColor = BrandBlue.copy(alpha = 0.2f),
                strokeWidth = 2.5.dp,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
