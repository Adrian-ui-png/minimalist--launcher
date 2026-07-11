package com.example.productive_launcher.data.model

enum class FocusState {
    Idle,
    Running,
    Paused,
    Completed,
    Cancelled
}

data class FocusSession(
    val sessionTitle: String,
    val duration: Long, // Duration of focus session in milliseconds
    val elapsedTime: Long, // Elapsed time in milliseconds
    val state: FocusState
)
