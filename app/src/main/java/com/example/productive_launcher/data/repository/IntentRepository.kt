package com.example.productive_launcher.data.repository

import com.example.productive_launcher.data.local.IntentRecordDataStore
import com.example.productive_launcher.data.model.IntentRecord
import kotlinx.coroutines.flow.Flow

class IntentRepository(private val dataStore: IntentRecordDataStore) {

    val recordsFlow: Flow<List<IntentRecord>> = dataStore.recordsFlow

    suspend fun addRecord(record: IntentRecord) {
        dataStore.addRecord(record)
    }
}
