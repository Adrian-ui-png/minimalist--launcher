package com.example.productive_launcher.ui.drawer

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.productive_launcher.data.model.AppInfo
import com.example.productive_launcher.launcher.LauncherViewModel
import com.example.productive_launcher.ui.components.AppListItem
import com.example.productive_launcher.ui.components.SearchBar

@Composable
fun AppDrawer(
    viewModel: LauncherViewModel,
    onAppClick: (AppInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val favoriteApps by viewModel.favoriteApps.collectAsState()
    val recentApps by viewModel.recentApps.collectAsState()
    val otherApps by viewModel.otherApps.collectAsState()
    val filteredApps by viewModel.filteredApps.collectAsState()
    val showFavoritesSection by viewModel.showFavorites.collectAsState()
    val showRecentAppsSection by viewModel.showRecentApps.collectAsState()
    val favSet by viewModel.favoritePackageSet.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val hasFavorites by remember { derivedStateOf { favoriteApps.isNotEmpty() } }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        contentPadding = PaddingValues(vertical = 20.dp)
    ) {
        item(key = "header") {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Apps",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(24.dp))
            SearchBar(
                query = searchQuery,
                onQueryChange = viewModel::onSearchQueryChanged
            )
            Spacer(modifier = Modifier.height(28.dp))
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        } else if (isSearching) {
            if (filteredApps.isEmpty()) {
                item(key = "empty_search") {
                    EmptyDrawerSearchState()
                }
            }
            itemsIndexed(
                items = filteredApps,
                key = { _, app -> app.packageName }
            ) { _, app ->
                val isFav = app.packageName in favSet
                AppListItem(
                    appName = app.appName,
                    packageName = app.packageName,
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
                    SectionTitle(text = "Favorites")
                }

                itemsIndexed(
                    items = favoriteApps,
                    key = { _, app -> "fav_${app.packageName}" }
                ) { _, app ->
                    AppListItem(
                        appName = app.appName,
                        packageName = app.packageName,
                        onClick = { onAppClick(app) },
                        isFavorite = true,
                        onFavoriteToggle = { viewModel.removeFavorite(app.packageName) }
                    )
                }

                item(key = "fav_spacer") {
                    Spacer(modifier = Modifier.height(20.dp))
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
                    val isFav = app.packageName in favSet
                    AppListItem(
                        appName = app.appName,
                        packageName = app.packageName,
                        onClick = { onAppClick(app) },
                        isFavorite = isFav,
                        onFavoriteToggle = {
                            if (isFav) viewModel.removeFavorite(app.packageName)
                            else viewModel.addFavorite(app.packageName)
                        }
                    )
                }

                item(key = "recent_spacer") {
                    Spacer(modifier = Modifier.height(20.dp))
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
                        packageName = app.packageName,
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
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun EmptyDrawerSearchState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Outlined.SearchOff,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No results found",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Try a different search term",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
