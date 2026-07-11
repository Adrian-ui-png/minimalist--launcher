package com.example.productive_launcher.ui.theme

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring

object MotionSystem {
    // Animation Durations (ms)
    const val DurationFast = 180
    const val DurationNormal = 280
    const val DurationSlow = 450

    // Standard Easing
    val StandardEasing: Easing = FastOutSlowInEasing

    // Spring Configurations
    val DefaultSpring: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow
    )
    
    val TightSpring: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )
}
