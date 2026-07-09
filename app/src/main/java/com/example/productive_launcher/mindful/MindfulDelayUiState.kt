package com.example.productive_launcher.mindful

sealed interface MindfulDelayUiState {
    data object Loading : MindfulDelayUiState

    data class IntentSelection(
        val packageName: String,
        val appName: String,
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
