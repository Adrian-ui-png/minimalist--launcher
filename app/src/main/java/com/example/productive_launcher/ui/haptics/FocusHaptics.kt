package com.example.productive_launcher.ui.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object FocusHaptics {
    private fun getVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun performSuccess(context: Context) {
        try {
            val vibrator = getVibrator(context) ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 50, 50, 50), intArrayOf(0, 255, 0, 255), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(100)
            }
        } catch (e: Exception) {
            // Safe fallback: ignore haptic if vibrator is unavailable or errors out
        }
    }

    fun performSelection(context: Context) {
        try {
            val vibrator = getVibrator(context) ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(20)
            }
        } catch (e: Exception) {
            // Safe fallback: ignore haptic if vibrator is unavailable or errors out
        }
    }

    fun performFocusStart(context: Context) {
        try {
            val vibrator = getVibrator(context) ?: return
            // A calm, rising waveform feel
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 60, 80, 100),
                        intArrayOf(0, 64, 128, 255),
                        -1
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(240)
            }
        } catch (e: Exception) {
            // Safe fallback: ignore haptic if vibrator is unavailable or errors out
        }
    }

    fun performSessionComplete(context: Context) {
        try {
            val vibrator = getVibrator(context) ?: return
            // Double pulse for congratulations/completion
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 150, 100, 250),
                        intArrayOf(0, 200, 0, 255),
                        -1
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(500)
            }
        } catch (e: Exception) {
            // Safe fallback: ignore haptic if vibrator is unavailable or errors out
        }
    }
}
