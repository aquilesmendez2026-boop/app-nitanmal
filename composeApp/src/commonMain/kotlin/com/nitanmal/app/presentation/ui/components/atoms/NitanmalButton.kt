package com.nitanmal.app.presentation.ui.components.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

enum class NitanmalButtonVariant {
    Primary, Secondary, Accent
}

@Composable
fun NitanmalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: NitanmalButtonVariant = NitanmalButtonVariant.Primary,
    isLoading: Boolean = false,
    enabled: Boolean = true
) {
    val shape = RoundedCornerShape(12.dp)
    val colorScheme = MaterialTheme.colorScheme

    // Colors for each variant - using theme colors for dark/light mode support
    val (backgroundColor, borderColor, textColor, shadowElevation) = when (variant) {
        NitanmalButtonVariant.Primary -> Quadruple(
            if (enabled) colorScheme.primary else colorScheme.primary.copy(alpha = 0.5f),
            null, // No border for primary
            Color.White,
            if (enabled && !isLoading) 8.dp else 0.dp
        )
        NitanmalButtonVariant.Secondary -> Quadruple(
            colorScheme.surface, // Use surface color for background (white in light, dark in dark)
            if (enabled) colorScheme.secondary else colorScheme.secondary.copy(alpha = 0.5f),
            colorScheme.secondary,
            if (enabled && !isLoading) 4.dp else 0.dp
        )
        NitanmalButtonVariant.Accent -> Quadruple(
            if (enabled) colorScheme.tertiary else colorScheme.tertiary.copy(alpha = 0.5f),
            null, // No border for accent
            Color.White,
            if (enabled && !isLoading) 6.dp else 0.dp
        )
    }

    // Build modifier in CORRECT ORDER
    var buttonModifier = modifier.height(48.dp)

    // Add shadow if elevation > 0
    if (shadowElevation > 0.dp) {
        buttonModifier = buttonModifier.shadow(shadowElevation, shape)
    }

    // Clip to shape
    buttonModifier = buttonModifier.clip(shape)

    // Add background
    buttonModifier = buttonModifier.background(backgroundColor)

    // Add border for secondary
    if (borderColor != null) {
        buttonModifier = buttonModifier.border(2.dp, borderColor, shape)
    }

    // Add clickable last
    buttonModifier = buttonModifier.clickable(
        enabled = enabled && !isLoading,
        onClick = onClick
    )

    Box(
        modifier = buttonModifier,
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            Text(
                text = "•••",
                color = textColor,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = textColor
            )
        }
    }
}

// Helper class for multiple return values
data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
