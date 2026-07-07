package com.example.productive_launcher.data.repository

import com.example.productive_launcher.data.local.DelaySettingsDataStore
import kotlinx.coroutines.flow.Flow

class DelaySettingsRepository(private val dataStore: DelaySettingsDataStore) {

    val protectedAppsFlow: Flow<List<String>> = dataStore.protectedAppsFlow

    suspend fun setProtected(packageName: String, protected: Boolean) {
        dataStore.setProtected(packageName, protected)
    }
}
