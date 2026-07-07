package com.example.productive_launcher.mindful

data class IntentOption(
    val emoji: String,
    val label: String,
    val key: String
)

val intentOptions = listOf(
    IntentOption("💬", "Reply to someone", "reply"),
    IntentOption("📚", "Learn", "study"),
    IntentOption("🎬", "Entertainment", "entertainment"),
    IntentOption("🤔", "Just checking", "just_checking")
)
