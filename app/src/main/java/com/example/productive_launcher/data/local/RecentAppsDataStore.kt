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

private val Context.recentDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "recent_prefs"
)

class RecentAppsDataStore(private val context: Context) {

    private val recentKey = stringPreferencesKey("recent_packages")

    val recentAppsFlow: Flow<List<String>> = context.recentDataStore.data
        .map { prefs ->
            val raw = prefs[recentKey] ?: ""
            if (raw.isBlank()) emptyList() else raw.split(",")
        }
        .distinctUntilChanged()
        .flowOn(Dispatchers.IO)

    suspend fun recordLaunch(packageName: String) {
        context.recentDataStore.edit { prefs ->
            val raw = prefs[recentKey] ?: ""
            val current = if (raw.isBlank()) mutableListOf()
            else raw.split(",").toMutableList()
            current.remove(packageName)
            current.add(0, packageName)
            val trimmed = current.take(5)
            prefs[recentKey] = trimmed.joinToString(",")
        }
    }
}
