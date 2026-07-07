package com.example.productive_launcher.data.repository

import com.example.productive_launcher.data.local.SettingsDataStore
import com.example.productive_launcher.data.model.ThemeMode
import kotlinx.coroutines.flow.Flow

class SettingsRepository(private val dataStore: SettingsDataStore) {

    val themeModeFlow: Flow<ThemeMode> = dataStore.themeModeFlow
    val delayDurationFlow: Flow<Int> = dataStore.delayDurationFlow
    val showFavoritesFlow: Flow<Boolean> = dataStore.showFavoritesFlow
    val showRecentAppsFlow: Flow<Boolean> = dataStore.showRecentAppsFlow

    suspend fun setThemeMode(mode: ThemeMode) = dataStore.setThemeMode(mode)
    suspend fun setDelayDuration(seconds: Int) = dataStore.setDelayDuration(seconds)
    suspend fun setShowFavorites(show: Boolean) = dataStore.setShowFavorites(show)
    suspend fun setShowRecentApps(show: Boolean) = dataStore.setShowRecentApps(show)
}
