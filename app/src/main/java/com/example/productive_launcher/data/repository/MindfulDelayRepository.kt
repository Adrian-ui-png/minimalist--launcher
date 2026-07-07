package com.example.productive_launcher.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MindfulDelayRepository(
    private val delaySettingsRepository: DelaySettingsRepository
) {

    val protectedAppsFlow: Flow<Set<String>> = delaySettingsRepository.protectedAppsFlow
        .map { it.toSet() }

    suspend fun setProtected(packageName: String, protected: Boolean) {
        delaySettingsRepository.setProtected(packageName, protected)
    }
}
