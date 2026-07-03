package com.example.productive_launcher.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
    modifier: Modifier = Modifier
) {
    val favoriteApps by viewModel.favoriteApps.collectAsState()
    val otherApps by viewModel.otherApps.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredApps by viewModel.filteredApps.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSearching = searchQuery.isNotBlank()
    val favoriteNames = remember(favoriteApps) {
        favoriteApps.mapTo(HashSet()) { it.packageName }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        contentPadding = PaddingValues(vertical = 24.dp)
    ) {
        item(key = "header") {
            Spacer(modifier = Modifier.height(8.dp))
            GreetingHeader()
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

            items(
                items = filteredApps,
                key = { it.packageName }
            ) { app ->
                AppListItem(
                    appName = app.appName,
                    icon = app.icon,
                    onClick = { onAppClick(app) },
                    isFavorite = app.packageName in favoriteNames,
                    onFavoriteToggle = { viewModel.toggleFavorite(app.packageName) }
                )
            }
        } else {
            if (favoriteApps.isNotEmpty()) {
                item(key = "fav_header") {
                    SectionTitle(text = "Favorites")
                }

                items(
                    items = favoriteApps,
                    key = { "fav_${it.packageName}" }
                ) { app ->
                    AppListItem(
                        appName = app.appName,
                        icon = app.icon,
                        onClick = { onAppClick(app) },
                        isFavorite = true,
                        onFavoriteToggle = { viewModel.toggleFavorite(app.packageName) }
                    )
                }
            }

            if (otherApps.isNotEmpty()) {
                item(key = "apps_header") {
                    SectionTitle(text = "Apps")
                }

                items(
                    items = otherApps,
                    key = { it.packageName }
                ) { app ->
                    AppListItem(
                        appName = app.appName,
                        icon = app.icon,
                        onClick = { onAppClick(app) },
                        isFavorite = false,
                        onFavoriteToggle = { viewModel.toggleFavorite(app.packageName) }
                    )
                }
            }

            if (favoriteApps.isEmpty() && otherApps.isEmpty()) {
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
