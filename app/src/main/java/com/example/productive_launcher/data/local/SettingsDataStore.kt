package com.example.productive_launcher.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.productive_launcher.data.model.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "settings_prefs"
)

class SettingsDataStore(private val context: Context) {

    private val themeModeKey = stringPreferencesKey("theme_mode")
    private val delayDurationKey = intPreferencesKey("delay_duration")
    private val showFavoritesKey = booleanPreferencesKey("show_favorites")
    private val showRecentAppsKey = booleanPreferencesKey("show_recent_apps")

    val themeModeFlow: Flow<ThemeMode> = context.settingsDataStore.data
        .map { prefs ->
            try {
                ThemeMode.valueOf(prefs[themeModeKey] ?: ThemeMode.SYSTEM.name)
            } catch (_: IllegalArgumentException) {
                ThemeMode.SYSTEM
            }
        }
        .distinctUntilChanged()
        .flowOn(Dispatchers.IO)

    val delayDurationFlow: Flow<Int> = context.settingsDataStore.data
        .map { prefs -> prefs[delayDurationKey] ?: 5 }
        .distinctUntilChanged()
        .flowOn(Dispatchers.IO)

    val showFavoritesFlow: Flow<Boolean> = context.settingsDataStore.data
        .map { prefs -> prefs[showFavoritesKey] ?: true }
        .distinctUntilChanged()
        .flowOn(Dispatchers.IO)

    val showRecentAppsFlow: Flow<Boolean> = context.settingsDataStore.data
        .map { prefs -> prefs[showRecentAppsKey] ?: true }
        .distinctUntilChanged()
        .flowOn(Dispatchers.IO)

    suspend fun setThemeMode(mode: ThemeMode) {
        context.settingsDataStore.edit { prefs ->
            prefs[themeModeKey] = mode.name
        }
    }

    suspend fun setDelayDuration(seconds: Int) {
        context.settingsDataStore.edit { prefs ->
            prefs[delayDurationKey] = seconds
        }
    }

    suspend fun setShowFavorites(show: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[showFavoritesKey] = show
        }
    }

    suspend fun setShowRecentApps(show: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[showRecentAppsKey] = show
        }
    }
}
