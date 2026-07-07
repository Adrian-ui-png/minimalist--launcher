package com.example.productive_launcher.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.productive_launcher.data.local.SettingsDataStore
import com.example.productive_launcher.data.model.ThemeMode
import com.example.productive_launcher.data.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository(SettingsDataStore(application))

    val themeMode: StateFlow<ThemeMode> = repository.themeModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeMode.SYSTEM)

    val delayDuration: StateFlow<Int> = repository.delayDurationFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 5)

    val showFavorites: StateFlow<Boolean> = repository.showFavoritesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val showRecentApps: StateFlow<Boolean> = repository.showRecentAppsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.setThemeMode(mode)
        }
    }

    fun setDelayDuration(seconds: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.setDelayDuration(seconds)
        }
    }

    fun setShowFavorites(show: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.setShowFavorites(show)
        }
    }

    fun setShowRecentApps(show: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.setShowRecentApps(show)
        }
    }
}
