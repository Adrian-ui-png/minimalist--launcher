package com.example.productive_launcher.ui.animation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import com.example.productive_launcher.ui.theme.FocusThemeConfig
import com.example.productive_launcher.ui.theme.LocalFocusThemeConfig
import com.example.productive_launcher.ui.theme.MotionSystem

data class WallpaperAnimationProperties(
    val overlayAlpha: Float,
    val blurAmount: Float,
    val contentOpacity: Float
)

@Composable
fun animateWallpaperProperties(
    config: FocusThemeConfig = LocalFocusThemeConfig.current
): State<WallpaperAnimationProperties> {
    val targetAlpha = config.wallpaperOverlayAlpha
    val targetBlur = config.blurAmountDp
    val targetOpacity = if (config.isFocusActive) 0.4f else 1.0f

    val alphaState = animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(
            durationMillis = MotionSystem.DurationSlow,
            easing = MotionSystem.StandardEasing
        ),
        label = "wallpaperAlpha"
    )

    val blurState = animateFloatAsState(
        targetValue = targetBlur,
        animationSpec = tween(
            durationMillis = MotionSystem.DurationSlow,
            easing = MotionSystem.StandardEasing
        ),
        label = "wallpaperBlur"
    )

    val opacityState = animateFloatAsState(
        targetValue = targetOpacity,
        animationSpec = tween(
            durationMillis = MotionSystem.DurationSlow,
            easing = MotionSystem.StandardEasing
        ),
        label = "contentOpacity"
    )

    return remember {
        derivedStateOf {
            WallpaperAnimationProperties(
                overlayAlpha = alphaState.value,
                blurAmount = blurState.value,
                contentOpacity = opacityState.value
            )
        }
    }
}
