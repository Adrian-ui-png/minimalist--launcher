package com.example.productive_launcher.data.repository

import com.example.productive_launcher.data.local.RecentAppsDataStore
import kotlinx.coroutines.flow.Flow

class RecentAppsRepository(private val dataStore: RecentAppsDataStore) {

    val recentAppsFlow: Flow<List<String>> = dataStore.recentAppsFlow

    suspend fun recordLaunch(packageName: String) {
        dataStore.recordLaunch(packageName)
    }
}
