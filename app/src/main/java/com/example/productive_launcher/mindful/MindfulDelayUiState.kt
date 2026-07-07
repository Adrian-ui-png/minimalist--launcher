package com.example.productive_launcher.mindful

import android.graphics.drawable.Drawable

sealed interface MindfulDelayUiState {
    data object Loading : MindfulDelayUiState

    data class IntentSelection(
        val appName: String,
        val appIcon: Drawable?,
        val selectedIntent: String?,
        val isContinueEnabled: Boolean
    ) : MindfulDelayUiState

    data class Countdown(
        val appName: String,
        val label: String,
        val currentNumber: Int
    ) : MindfulDelayUiState

    data object Launching : MindfulDelayUiState
}
