package com.example.productive_launcher.audio

interface FocusAudioManager {
    fun play(soundId: String)
    fun pause()
    fun stop()
    fun changeSound(soundId: String)
}

/**
 * Stub implementation of the FocusAudioManager for building and compilation.
 * No-op execution.
 */
class NoOpFocusAudioManager : FocusAudioManager {
    override fun play(soundId: String) {
        // No-op
    }

    override fun pause() {
        // No-op
    }

    override fun stop() {
        // No-op
    }

    override fun changeSound(soundId: String) {
        // No-op
    }
}
