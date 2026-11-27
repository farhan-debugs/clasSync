package com.classsync.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Typography using system fonts (Inter-equivalent on most Android devices).
 * To use Plus Jakarta Sans or Inter:
 *   1. Download the .ttf files from Google Fonts
 *   2. Place them in app/src/main/res/font/
 *   3. Replace FontFamily.Default below with your custom FontFamily
 *
 * Example:
 *   val PlusJakartaSans = FontFamily(
 *       Font(R.font.plus_jakarta_sans_regular, FontWeight.Normal),
 *       Font(R.font.plus_jakarta_sans_medium,  FontWeight.Medium),
 *       Font(R.font.plus_jakarta_sans_semibold, FontWeight.SemiBold),
 *       Font(R.font.plus_jakarta_sans_bold,     FontWeight.Bold),
 *   )
 */
private val AppFontFamily = FontFamily.Default

val AppTypography = Typography(
    // Hero stat numbers  36sp Bold
    displayLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontSize   = 36.sp,
        fontWeight = FontWeight.Bold,
        color      = PureWhite,
        lineHeight = 44.sp
    ),
    // Screen / hero titles  26sp SemiBold
    displayMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontSize   = 26.sp,
        fontWeight = FontWeight.SemiBold,
        color      = PureWhite,
        lineHeight = 32.sp
    ),
    // Section headers  18sp SemiBold
    titleLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontSize   = 18.sp,
        fontWeight = FontWeight.SemiBold,
        color      = PureWhite,
        lineHeight = 24.sp
    ),
    // Card labels / body  14sp Medium
    bodyMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontSize   = 14.sp,
        fontWeight = FontWeight.Medium,
        color      = PureWhite,
        lineHeight = 20.sp
    ),
    // Micro labels / badges  12sp Regular
    labelSmall = TextStyle(
        fontFamily = AppFontFamily,
        fontSize   = 12.sp,
        fontWeight = FontWeight.Normal,
        color      = MutedSilver,
        lineHeight = 16.sp
    ),
    // Body text  16sp Normal
    bodyLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontSize   = 16.sp,
        fontWeight = FontWeight.Normal,
        color      = PureWhite,
        lineHeight = 24.sp
    ),
    // Label medium  13sp Medium
    labelMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontSize   = 13.sp,
        fontWeight = FontWeight.Medium,
        color      = MutedSilver,
        lineHeight = 18.sp
    )
)
