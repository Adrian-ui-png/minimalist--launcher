package com.example.productive_launcher.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.productive_launcher.ui.theme.DesignTokens
import com.example.productive_launcher.ui.theme.MotionSystem
import com.example.productive_launcher.ui.theme.SharedTypography

@Composable
fun HeroClock(
    time: String,
    modifier: Modifier = Modifier,
    isTimerMode: Boolean = false
) {
    val fontSize by animateFloatAsState(
        targetValue = if (isTimerMode) 96f else 86f,
        animationSpec = tween(
            durationMillis = 500,
            delayMillis = if (isTimerMode) 300 else 100,
            easing = MotionSystem.StandardEasing
        ),
        label = "heroClockFontSize"
    )

    val letterSpacing by animateFloatAsState(
        targetValue = if (isTimerMode) -3f else -2f,
        animationSpec = tween(
            durationMillis = 500,
            delayMillis = if (isTimerMode) 300 else 100,
            easing = MotionSystem.StandardEasing
        ),
        label = "heroClockLetterSpacing"
    )

    val fontWeight = if (isTimerMode) FontWeight.Light else FontWeight.ExtraLight

    Text(
        text = time,
        style = SharedTypography.HeroClock.copy(
            fontSize = fontSize.sp,
            letterSpacing = letterSpacing.sp,
            fontWeight = fontWeight
        ),
        modifier = modifier,
        textAlign = TextAlign.Center
    )
}

@Deprecated("Use HeroClock with isTimerMode = true instead for smooth morphing")
@Composable
fun HeroTimer(
    remainingTimeFormatted: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = remainingTimeFormatted,
        style = SharedTypography.HeroTimer,
        modifier = modifier,
        textAlign = TextAlign.Center
    )
}

@Composable
fun QuoteCard(
    quote: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
    ) {
        // Split the quote cleanly or render together using SharedTypography
        Text(
            text = quote,
            style = SharedTypography.Quote,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun FocusButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White.copy(alpha = DesignTokens.OpacityClutter),
    contentColor: Color = Color.White
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(
            durationMillis = MotionSystem.DurationFast,
            easing = MotionSystem.StandardEasing
        ),
        label = "focusButtonScale"
    )

    Box(
        modifier = modifier
            .graphicsLayer(scaleX = buttonScale, scaleY = buttonScale)
            .clip(RoundedCornerShape(DesignTokens.CornerRadiusPill))
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = DesignTokens.SpacingMedium, vertical = DesignTokens.SpacingSmall),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = contentColor,
            fontSize = 14.sp,
            style = SharedTypography.TaskText
        )
    }
}

@Composable
fun PrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.96f else 1f,
        animationSpec = tween(
            durationMillis = MotionSystem.DurationFast,
            easing = MotionSystem.StandardEasing
        ),
        label = "primaryBtnScale"
    )

    val backgroundAlpha = if (!enabled) {
        DesignTokens.OpacityDisabled
    } else if (isPressed) {
        0.15f
    } else {
        0.08f
    }

    Box(
        modifier = modifier
            .graphicsLayer(scaleX = buttonScale, scaleY = buttonScale)
            .clip(RoundedCornerShape(DesignTokens.CornerRadiusMedium))
            .background(Color.White.copy(alpha = backgroundAlpha))
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White.copy(alpha = if (enabled) DesignTokens.OpacityFull else DesignTokens.OpacityDisabled),
            style = SharedTypography.TaskText
        )
    }
}
