package com.example.productive_launcher.ui.delay

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import com.example.productive_launcher.mindful.DelayViewModel
import com.example.productive_launcher.mindful.MindfulDelayUiState

@Composable
fun DelayScreen(
    viewModel: DelayViewModel,
    onGoBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic: HapticFeedback = LocalHapticFeedback.current

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (val state = uiState) {
            is MindfulDelayUiState.Loading -> {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
            }

            is MindfulDelayUiState.IntentSelection -> {
                IntentSelectionSection(
                    state = state,
                    onIntentSelected = viewModel::onIntentSelected,
                    onContinue = viewModel::onContinue,
                    onGoBack = onGoBack
                )
            }

            is MindfulDelayUiState.Countdown -> {
                CountdownSection(
                    state = state,
                    haptic = haptic,
                    onCancel = {
                        viewModel.cancelCountdown()
                        onGoBack()
                    }
                )
            }

            is MindfulDelayUiState.Launching -> {
                Text(
                    text = "Opening...",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
