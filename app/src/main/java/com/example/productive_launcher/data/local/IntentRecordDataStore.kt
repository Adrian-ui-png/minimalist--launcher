package com.example.productive_launcher.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.productive_launcher.data.model.IntentRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

private val Context.intentRecordDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "intent_records_prefs"
)

class IntentRecordDataStore(private val context: Context) {

    private val recordsKey = stringPreferencesKey("intent_records")

    val recordsFlow: Flow<List<IntentRecord>> = context.intentRecordDataStore.data
        .map { prefs ->
            val raw = prefs[recordsKey] ?: ""
            if (raw.isBlank()) emptyList()
            else raw.split("\n").mapNotNull { line ->
                val parts = line.split("|")
                if (parts.size == 3) {
                    try {
                        IntentRecord(
                            packageName = parts[0],
                            intent = parts[1],
                            timestamp = parts[2].toLong()
                        )
                    } catch (_: NumberFormatException) {
                        null
                    }
                } else null
            }
        }
        .distinctUntilChanged()
        .flowOn(Dispatchers.IO)

    suspend fun addRecord(record: IntentRecord) {
        context.intentRecordDataStore.edit { prefs ->
            val raw = prefs[recordsKey] ?: ""
            val newLine = "${record.packageName}|${record.intent}|${record.timestamp}"
            val updated = if (raw.isBlank()) newLine
            else "$raw\n$newLine"
            prefs[recordsKey] = updated
        }
    }
}
