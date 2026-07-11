package com.example.productive_launcher.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object SharedTypography {
    val HeroClock = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraLight,
        fontSize = 86.sp,
        lineHeight = 86.sp,
        letterSpacing = (-2).sp,
        color = Color.White
    )

    val HeroTimer = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Light,
        fontSize = 96.sp,
        lineHeight = 96.sp,
        letterSpacing = (-3).sp,
        color = Color.White
    )

    val Greeting = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Light,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        color = Color.White
    )

    val FocusTitle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.2.sp,
        color = Color.White
    )

    val Quote = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontStyle = FontStyle.Italic,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        color = Color.White.copy(alpha = DesignTokens.OpacityMedium)
    )

    val TaskText = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = Color.White
    )

    val SectionLabel = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.sp,
        color = Color.White.copy(alpha = DesignTokens.OpacitySecondary)
    )
}
