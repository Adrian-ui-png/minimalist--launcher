package com.example.productive_launcher.data.model

import android.content.Intent
import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val launchIntent: Intent?
)
