package com.example.productive_launcher.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

private val Context.delaySettingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "delay_settings_prefs"
)

class DelaySettingsDataStore(private val context: Context) {

    private val protectedAppsKey = stringPreferencesKey("protected_apps")

    val protectedAppsFlow: Flow<List<String>> = context.delaySettingsDataStore.data
        .map { prefs ->
            val raw = prefs[protectedAppsKey] ?: ""
            if (raw.isBlank()) emptyList() else raw.split(",")
        }
        .distinctUntilChanged()
        .flowOn(Dispatchers.IO)

    suspend fun setProtected(packageName: String, isProtected: Boolean) {
        context.delaySettingsDataStore.edit { prefs ->
            val raw = prefs[protectedAppsKey] ?: ""
            val current = if (raw.isBlank()) mutableListOf()
            else raw.split(",").toMutableList()
            if (isProtected) {
                if (packageName !in current) current.add(packageName)
            } else {
                current.remove(packageName)
            }
            prefs[protectedAppsKey] = current.joinToString(",")
        }
    }
}
