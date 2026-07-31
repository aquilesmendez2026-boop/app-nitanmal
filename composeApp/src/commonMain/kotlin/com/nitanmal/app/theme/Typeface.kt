package com.nitanmal.app.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

// HOW TO ADD REAL FONTS:
// 1. Download from Google Fonts:
//    - Outfit: https://fonts.google.com/specimen/Outfit
//    - Inter: https://fonts.google.com/specimen/Inter
// 2. Place .ttf files in: composeApp/src/commonMain/composeResources/font/
//    (lowercase_snake_case: outfit_regular.ttf, inter_regular.ttf, etc.)
// 3. Replace the FontFamily.SansSerif placeholders below with families
//    built from org.jetbrains.compose.resources.Font.

object NitanmalFonts {
    // Placeholders — reemplazar con Outfit / Inter al agregar los .ttf
    val Outfit = FontFamily.SansSerif
    val Inter = FontFamily.SansSerif
}

val NitanmalTypography = Typography(
    // Headings - Outfit
    displayLarge = TextStyle(
        fontFamily = NitanmalFonts.Outfit,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.02).em
    ),
    displayMedium = TextStyle(
        fontFamily = NitanmalFonts.Outfit,
        fontWeight = FontWeight.SemiBold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = (-0.02).em
    ),
    headlineLarge = TextStyle(
        fontFamily = NitanmalFonts.Outfit,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.02).em
    ),
    headlineMedium = TextStyle(
        fontFamily = NitanmalFonts.Outfit,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.02).em
    ),
    headlineSmall = TextStyle(
        fontFamily = NitanmalFonts.Outfit,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.01).em
    ),

    // Body - Inter
    bodyLarge = TextStyle(
        fontFamily = NitanmalFonts.Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.em
    ),
    bodyMedium = TextStyle(
        fontFamily = NitanmalFonts.Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.em
    ),
    bodySmall = TextStyle(
        fontFamily = NitanmalFonts.Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.em
    ),

    // Labels - Inter
    labelLarge = TextStyle(
        fontFamily = NitanmalFonts.Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.em
    ),
    labelMedium = TextStyle(
        fontFamily = NitanmalFonts.Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.1.em
    ),
    labelSmall = TextStyle(
        fontFamily = NitanmalFonts.Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.1.em
    )
)
