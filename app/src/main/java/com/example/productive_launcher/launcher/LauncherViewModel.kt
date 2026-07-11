package com.example.productive_launcher.launcher

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.productive_launcher.data.local.DelaySettingsDataStore
import com.example.productive_launcher.data.local.FavoritesDataStore
import com.example.productive_launcher.data.local.RecentAppsDataStore
import com.example.productive_launcher.data.local.SettingsDataStore
import com.example.productive_launcher.data.model.AppInfo
import com.example.productive_launcher.data.repository.AppRepository
import com.example.productive_launcher.data.repository.DelaySettingsRepository
import com.example.productive_launcher.data.repository.FavoritesRepository
import com.example.productive_launcher.data.repository.MindfulDelayRepository
import com.example.productive_launcher.data.repository.RecentAppsRepository
import com.example.productive_launcher.data.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository(application.packageManager)
    private val favoritesRepository = FavoritesRepository(FavoritesDataStore(application))
    private val recentAppsRepository = RecentAppsRepository(RecentAppsDataStore(application))
    private val mindfulDelayRepository = MindfulDelayRepository(
        DelaySettingsRepository(DelaySettingsDataStore(application))
    )
    private val settingsRepository = SettingsRepository(SettingsDataStore(application))

    private val _isFocusActive = MutableStateFlow(false)
    val isFocusActive = _isFocusActive.asStateFlow()

    private val _wallpaperUpdateTrigger = MutableStateFlow(System.currentTimeMillis())
    val wallpaperUpdateTrigger = _wallpaperUpdateTrigger.asStateFlow()

    fun notifyWallpaperChanged() {
        _wallpaperUpdateTrigger.value = System.currentTimeMillis()
    }

    private val _apps = MutableStateFlow<List<AppInfo>>(emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val isSearching: StateFlow<Boolean> = _searchQuery
        .map { it.isNotBlank() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private val favoritePackageNames: StateFlow<List<String>> = favoritesRepository.favoritesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val favoritePackageSet: StateFlow<Set<String>> = favoritesRepository.favoritesFlow
        .map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    val protectedApps: StateFlow<Set<String>> = mindfulDelayRepository.protectedAppsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    val favoriteApps: StateFlow<List<AppInfo>> = combine(
        _apps, favoritePackageNames, isFocusActive, protectedApps
    ) { apps, favPkgs, focusActive, protPkgs ->
        val appMap = apps.associateBy { it.packageName }
        val finalFavPkgs = if (focusActive) favPkgs.filter { it !in protPkgs } else favPkgs
        finalFavPkgs.mapNotNull { appMap[it] }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val otherApps: StateFlow<List<AppInfo>> = combine(
        _apps, favoritePackageNames, isFocusActive, protectedApps
    ) { apps, favPkgs, focusActive, protPkgs ->
        val favSet = favPkgs.toHashSet()
        val baseList = apps.filter { it.packageName !in favSet }
        if (focusActive) {
            baseList.filter { it.packageName !in protPkgs }
        } else {
            baseList
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val recentPackageNames: StateFlow<List<String>> = recentAppsRepository.recentAppsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val recentApps: StateFlow<List<AppInfo>> = combine(
        _apps, recentPackageNames, isFocusActive, protectedApps
    ) { apps, recentPkgs, focusActive, protPkgs ->
        val appMap = apps.associateBy { it.packageName }
        val finalRecentPkgs = if (focusActive) recentPkgs.filter { it !in protPkgs } else recentPkgs
        finalRecentPkgs.mapNotNull { appMap[it] }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val showFavorites: StateFlow<Boolean> = settingsRepository.showFavoritesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val showRecentApps: StateFlow<Boolean> = settingsRepository.showRecentAppsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val filteredApps: StateFlow<List<AppInfo>> = combine(
        _apps, _searchQuery, isFocusActive, protectedApps
    ) { apps, query, focusActive, protPkgs ->
        val baseApps = if (focusActive) apps.filter { it.packageName !in protPkgs } else apps
        if (query.isBlank()) baseApps
        else {
            val lowerQuery = query.lowercase()
            baseApps.filter { it.appName.lowercase().contains(lowerQuery) }
        }
    }.distinctUntilChanged()
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadApps()
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun addFavorite(packageName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            favoritesRepository.addFavorite(packageName)
        }
    }

    fun removeFavorite(packageName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            favoritesRepository.removeFavorite(packageName)
        }
    }

    fun recordAppLaunch(packageName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            recentAppsRepository.recordLaunch(packageName)
        }
    }

    fun setFocusActive(active: Boolean) {
        _isFocusActive.value = active
    }

    val apps: StateFlow<List<AppInfo>> = _apps.asStateFlow()

    private val _focusDurationMinutes = MutableStateFlow(25)
    val focusDurationMinutes = _focusDurationMinutes.asStateFlow()

    private val _breakDurationMinutes = MutableStateFlow(5)
    val breakDurationMinutes = _breakDurationMinutes.asStateFlow()

    fun setFocusDuration(minutes: Int) {
        _focusDurationMinutes.value = minutes
    }

    fun setBreakDuration(minutes: Int) {
        _breakDurationMinutes.value = minutes
    }

    fun setAppProtected(packageName: String, protected: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            mindfulDelayRepository.setProtected(packageName, protected)
        }
    }

    private fun loadApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val apps = repository.getInstalledApps()
            _apps.value = apps
            _isLoading.value = false
        }
    }
}
