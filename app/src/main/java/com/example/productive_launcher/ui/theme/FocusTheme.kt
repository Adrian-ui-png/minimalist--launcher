package com.example.productive_launcher.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf

data class FocusThemeConfig(
    val isFocusActive: Boolean = false,
    val wallpaperOverlayAlpha: Float = 0.70f, // Darker wallpaper overlay for focus
    val blurAmountDp: Float = 20f,
    val textScaleFactor: Float = 1.2f,       // Larger typography scale
    val isClutterReduced: Boolean = true      // Reduced UI clutter flag
)

val LocalFocusThemeConfig = compositionLocalOf { FocusThemeConfig() }

@Composable
fun FocusThemeProvider(
    isFocusActive: Boolean,
    content: @Composable () -> Unit
) {
    val config = if (isFocusActive) {
        FocusThemeConfig(
            isFocusActive = true,
            wallpaperOverlayAlpha = 0.70f,
            blurAmountDp = 20f,
            textScaleFactor = 1.2f,
            isClutterReduced = true
        )
    } else {
        FocusThemeConfig(
            isFocusActive = false,
            wallpaperOverlayAlpha = 0.35f,
            blurAmountDp = 0f,
            textScaleFactor = 1.0f,
            isClutterReduced = false
        )
    }
    CompositionLocalProvider(LocalFocusThemeConfig provides config) {
        content()
    }
}
