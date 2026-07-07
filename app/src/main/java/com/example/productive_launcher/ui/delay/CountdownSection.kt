package com.example.productive_launcher.ui.delay

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.productive_launcher.mindful.MindfulDelayUiState

@Composable
fun CountdownSection(
    state: MindfulDelayUiState.Countdown,
    haptic: HapticFeedback,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayNumber = maxOf(0, state.currentNumber)

    LaunchedEffect(displayNumber) {
        if (displayNumber > 0) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Opening ${state.appName}",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        AnimatedContent(
            targetState = displayNumber,
            transitionSpec = {
                (fadeIn(tween(180)) + scaleIn(
                    initialScale = 0.5f,
                    animationSpec = tween(180)
                )).togetherWith(
                    fadeOut(tween(150)) + scaleOut(
                        targetScale = 1.3f,
                        animationSpec = tween(150)
                    )
                )
            },
            label = "countdown_number"
        ) { number ->
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 96.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics {
                    contentDescription = "$number seconds remaining"
                }
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        TextButton(onClick = onCancel) {
            Text(
                text = "Cancel",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
