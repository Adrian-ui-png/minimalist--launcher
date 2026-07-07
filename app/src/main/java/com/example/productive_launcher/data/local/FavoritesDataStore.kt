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

    val favoritesFlow: Flow<List<String>> = context.favoritesDataStore.data
        .map { prefs ->
            val raw = prefs[favoritesKey] ?: ""
            if (raw.isBlank()) emptyList() else raw.split(",")
        }
        .distinctUntilChanged()
        .flowOn(Dispatchers.IO)

    suspend fun addFavorite(packageName: String) {
        context.favoritesDataStore.edit { prefs ->
            val raw = prefs[favoritesKey] ?: ""
            val current = if (raw.isBlank()) mutableListOf()
            else raw.split(",").toMutableList()
            if (packageName in current) return@edit
            current.add(packageName)
            prefs[favoritesKey] = current.joinToString(",")
        }
    }

    suspend fun removeFavorite(packageName: String) {
        context.favoritesDataStore.edit { prefs ->
            val raw = prefs[favoritesKey] ?: ""
            if (raw.isBlank()) return@edit
            val current = raw.split(",").toMutableList()
            if (!current.remove(packageName)) return@edit
            prefs[favoritesKey] = if (current.isEmpty()) "" else current.joinToString(",")
        }
    }

}
