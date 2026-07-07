package com.example.productive_launcher.mindful

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.productive_launcher.data.local.DelaySettingsDataStore
import com.example.productive_launcher.data.local.IntentRecordDataStore
import com.example.productive_launcher.data.local.RecentAppsDataStore
import com.example.productive_launcher.data.local.SettingsDataStore
import com.example.productive_launcher.data.model.AppInfo
import com.example.productive_launcher.data.model.IntentRecord
import com.example.productive_launcher.data.repository.AppRepository
import com.example.productive_launcher.data.repository.DelaySettingsRepository
import com.example.productive_launcher.data.repository.IntentRepository
import com.example.productive_launcher.data.repository.MindfulDelayRepository
import com.example.productive_launcher.data.repository.RecentAppsRepository
import com.example.productive_launcher.data.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DelayViewModel(
    application: Application,
    private val packageName: String,
    private val appRepository: AppRepository,
    private val recentAppsRepository: RecentAppsRepository,
    private val intentRepository: IntentRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<MindfulDelayUiState>(MindfulDelayUiState.Loading)
    val uiState: StateFlow<MindfulDelayUiState> = _uiState.asStateFlow()

    private val settingsRepository = SettingsRepository(SettingsDataStore(application))
    private var countdownJob: Job? = null
    private var currentIntentKey: String? = null
    private var appInfo: AppInfo? = null

    init {
        loadAppInfo()
    }

    private fun loadAppInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            val allApps = appRepository.getInstalledApps()
            appInfo = allApps.find { it.packageName == packageName }
            if (appInfo != null) {
                _uiState.value = MindfulDelayUiState.IntentSelection(
                    appName = appInfo!!.appName,
                    appIcon = appInfo!!.icon,
                    selectedIntent = null,
                    isContinueEnabled = false
                )
            }
        }
    }

    fun onIntentSelected(intentKey: String) {
        currentIntentKey = intentKey
        val current = _uiState.value
        if (current is MindfulDelayUiState.IntentSelection) {
            _uiState.value = current.copy(
                selectedIntent = intentKey,
                isContinueEnabled = true
            )
        }
    }

    fun onContinue() {
        val intentKey = currentIntentKey ?: return
        val info = appInfo ?: return
        countdownJob = viewModelScope.launch {
            val duration = settingsRepository.delayDurationFlow.first()
            val label = intentOptions.find { it.key == intentKey }?.label ?: intentKey
            _uiState.value = MindfulDelayUiState.Countdown(
                appName = info.appName,
                label = label,
                currentNumber = duration
            )
            for (i in duration downTo 1) {
                delay(1000)
                if (i > 1) {
                    _uiState.value = MindfulDelayUiState.Countdown(
                        appName = info.appName,
                        label = label,
                        currentNumber = i - 1
                    )
                }
            }
            _uiState.value = MindfulDelayUiState.Launching
            recordIntent(intentKey)
            recentAppsRepository.recordLaunch(packageName)
            launchApp()
        }
    }

    fun cancelCountdown() {
        countdownJob?.cancel()
        countdownJob = null
        currentIntentKey = null
        val info = appInfo
        if (info != null) {
            _uiState.value = MindfulDelayUiState.IntentSelection(
                appName = info.appName,
                appIcon = info.icon,
                selectedIntent = null,
                isContinueEnabled = false
            )
        } else {
            _uiState.value = MindfulDelayUiState.Loading
        }
    }

    private suspend fun recordIntent(intentKey: String) {
        val record = IntentRecord(
            packageName = packageName,
            intent = intentKey,
            timestamp = System.currentTimeMillis()
        )
        intentRepository.addRecord(record)
    }

    private suspend fun launchApp() {
        val intent = appInfo?.launchIntent ?: return
        val context = getApplication<Application>()
        try {
            context.startActivity(intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: Exception) {
        }
    }

    companion object {
        fun factory(application: Application, packageName: String): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val appRepo = AppRepository(application.packageManager)
                    val recentRepo = RecentAppsRepository(
                        RecentAppsDataStore(application)
                    )
                    val intentRepo = IntentRepository(
                        IntentRecordDataStore(application)
                    )
                    return DelayViewModel(
                        application = application,
                        packageName = packageName,
                        appRepository = appRepo,
                        recentAppsRepository = recentRepo,
                        intentRepository = intentRepo
                    ) as T
                }
            }
        }
    }
}
