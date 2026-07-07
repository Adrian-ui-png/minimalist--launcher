package com.example.productive_launcher.data.repository

import com.example.productive_launcher.data.local.FavoritesDataStore
import kotlinx.coroutines.flow.Flow

class FavoritesRepository(private val dataStore: FavoritesDataStore) {

    val favoritesFlow: Flow<List<String>> = dataStore.favoritesFlow

    suspend fun addFavorite(packageName: String) {
        dataStore.addFavorite(packageName)
    }

    suspend fun removeFavorite(packageName: String) {
        dataStore.removeFavorite(packageName)
    }

}
