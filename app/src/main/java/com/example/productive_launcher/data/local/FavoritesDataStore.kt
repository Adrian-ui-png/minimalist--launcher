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

private val Context.favoritesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "favorites_prefs"
)

class FavoritesDataStore(private val context: Context) {

    private val favoritesKey = stringPreferencesKey("favorite_packages")

    val favoritesFlow: Flow<Set<String>> = context.favoritesDataStore.data
        .map { prefs ->
            val raw = prefs[favoritesKey] ?: ""
            if (raw.isBlank()) emptySet() else raw.split(",").toSet()
        }
        .distinctUntilChanged()
        .flowOn(Dispatchers.IO)

    suspend fun toggleFavorite(packageName: String) {
        context.favoritesDataStore.edit { prefs ->
            val current = prefs[favoritesKey] ?: ""
            val currentSet = if (current.isBlank()) {
                emptySet()
            } else {
                current.split(",").toSet()
            }
            val updated = if (packageName in currentSet) {
                currentSet - packageName
            } else {
                currentSet + packageName
            }
            prefs[favoritesKey] = if (updated.isEmpty()) "" else updated.joinToString(",")
        }
    }
}
