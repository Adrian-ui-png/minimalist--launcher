package com.example.productive_launcher.launcher

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.productive_launcher.data.local.FavoritesDataStore
import com.example.productive_launcher.data.model.AppInfo
import com.example.productive_launcher.data.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository(application.packageManager)
    private val favoritesDataStore = FavoritesDataStore(application)

    private val _apps = MutableStateFlow<List<AppInfo>>(emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val favoritePackageNames: StateFlow<Set<String>> = favoritesDataStore.favoritesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    val favoriteApps: StateFlow<List<AppInfo>> = combine(
        _apps, favoritePackageNames
    ) { apps, favs ->
        apps.filter { it.packageName in favs }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val otherApps: StateFlow<List<AppInfo>> = combine(
        _apps, favoritePackageNames
    ) { apps, favs ->
        apps.filter { it.packageName !in favs }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val filteredApps: StateFlow<List<AppInfo>> = combine(
        _apps, _searchQuery
    ) { apps, query ->
        if (query.isBlank()) {
            apps
        } else {
            val lowerQuery = query.lowercase()
            apps.filter { it.appName.lowercase().contains(lowerQuery) }
        }
    }.distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadApps()
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavorite(packageName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            favoritesDataStore.toggleFavorite(packageName)
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
