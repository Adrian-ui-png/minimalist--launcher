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

    val favoriteApps: StateFlow<List<AppInfo>> = combine(
        _apps, favoritePackageNames
    ) { apps, favPkgs ->
        val appMap = apps.associateBy { it.packageName }
        favPkgs.mapNotNull { appMap[it] }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val otherApps: StateFlow<List<AppInfo>> = combine(
        _apps, favoritePackageNames
    ) { apps, favPkgs ->
        val favSet = favPkgs.toHashSet()
        apps.filter { it.packageName !in favSet }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val protectedApps: StateFlow<Set<String>> = mindfulDelayRepository.protectedAppsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    private val recentPackageNames: StateFlow<List<String>> = recentAppsRepository.recentAppsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val recentApps: StateFlow<List<AppInfo>> = combine(
        _apps, recentPackageNames
    ) { apps, recentPkgs ->
        val appMap = apps.associateBy { it.packageName }
        recentPkgs.mapNotNull { appMap[it] }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val showFavorites: StateFlow<Boolean> = settingsRepository.showFavoritesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val showRecentApps: StateFlow<Boolean> = settingsRepository.showRecentAppsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val filteredApps: StateFlow<List<AppInfo>> = combine(
        _apps, _searchQuery
    ) { apps, query ->
        if (query.isBlank()) apps
        else {
            val lowerQuery = query.lowercase()
            apps.filter { it.appName.lowercase().contains(lowerQuery) }
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

    private fun loadApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val apps = repository.getInstalledApps()
            _apps.value = apps
            _isLoading.value = false
        }
    }
}
