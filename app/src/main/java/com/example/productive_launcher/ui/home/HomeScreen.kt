package com.example.productive_launcher.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.productive_launcher.data.model.AppInfo
import com.example.productive_launcher.launcher.LauncherViewModel
import com.example.productive_launcher.ui.components.AppListItem
import com.example.productive_launcher.ui.components.GreetingHeader
import com.example.productive_launcher.ui.components.SearchBar

@Composable
fun HomeScreen(
    viewModel: LauncherViewModel,
    onAppClick: (AppInfo) -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val favoriteApps by viewModel.favoriteApps.collectAsState()
    val recentApps by viewModel.recentApps.collectAsState()
    val otherApps by viewModel.otherApps.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredApps by viewModel.filteredApps.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val showFavoritesSection by viewModel.showFavorites.collectAsState()
    val showRecentAppsSection by viewModel.showRecentApps.collectAsState()
    val isSearching = searchQuery.isNotBlank()

    val hasFavorites = favoriteApps.isNotEmpty()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        contentPadding = PaddingValues(vertical = 24.dp)
    ) {
        item(key = "header") {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                GreetingHeader(modifier = Modifier.weight(1f))
                IconButton(onClick = onNavigateToSettings) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            SearchBar(
                query = searchQuery,
                onQueryChange = viewModel::onSearchQueryChanged
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (isLoading) {
            item(key = "loading") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        } else if (isSearching) {
            if (filteredApps.isEmpty()) {
                item(key = "empty_search") {
                    EmptySearchState()
                }
            }
            itemsIndexed(
                items = filteredApps,
                key = { _, app -> app.packageName }
            ) { _, app ->
                val isFav = app.packageName in favoriteApps.map { it.packageName }
                AppListItem(
                    appName = app.appName,
                    icon = app.icon,
                    onClick = { onAppClick(app) },
                    isFavorite = isFav,
                    onFavoriteToggle = {
                        if (isFav) viewModel.removeFavorite(app.packageName)
                        else viewModel.addFavorite(app.packageName)
                    }
                )
            }
        } else {
            if (showFavoritesSection && hasFavorites) {
                item(key = "fav_header") {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                        exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
                    ) {
                        SectionTitle(text = "Favorites")
                    }
                }

                itemsIndexed(
                    items = favoriteApps,
                    key = { _, app -> "fav_${app.packageName}" }
                ) { _, app ->
                    AppListItem(
                        appName = app.appName,
                        icon = app.icon,
                        onClick = { onAppClick(app) },
                        isFavorite = true,
                        onFavoriteToggle = { viewModel.removeFavorite(app.packageName) }
                    )
                }
            }

            if (showRecentAppsSection && recentApps.isNotEmpty()) {
                item(key = "recent_header") {
                    SectionTitle(text = "Recent")
                }

                itemsIndexed(
                    items = recentApps,
                    key = { _, app -> "recent_${app.packageName}" }
                ) { _, app ->
                    val isFav = app.packageName in favoriteApps.map { it.packageName }
                    AppListItem(
                        appName = app.appName,
                        icon = app.icon,
                        onClick = { onAppClick(app) },
                        isFavorite = isFav,
                        onFavoriteToggle = {
                            if (isFav) viewModel.removeFavorite(app.packageName)
                            else viewModel.addFavorite(app.packageName)
                        }
                    )
                }
            }

            if (otherApps.isNotEmpty()) {
                item(key = "apps_header") {
                    SectionTitle(text = "All Apps")
                }

                itemsIndexed(
                    items = otherApps,
                    key = { _, app -> app.packageName }
                ) { _, app ->
                    AppListItem(
                        appName = app.appName,
                        icon = app.icon,
                        onClick = { onAppClick(app) },
                        isFavorite = false,
                        onFavoriteToggle = { viewModel.addFavorite(app.packageName) }
                    )
                }
            }

            if (!hasFavorites && otherApps.isEmpty()) {
                item(key = "empty_all") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No apps available",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(top = 24.dp, bottom = 8.dp)
    )
}

@Composable
private fun EmptySearchState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No apps found",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Try searching with a different name",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
