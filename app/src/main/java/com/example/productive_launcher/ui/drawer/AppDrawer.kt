package com.example.productive_launcher.ui.drawer

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.productive_launcher.data.model.AppInfo
import com.example.productive_launcher.launcher.LauncherViewModel
import com.example.productive_launcher.ui.components.AppListItem
import com.example.productive_launcher.ui.components.SearchBar
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.collectLatest

private data class AlphabetGroup(
    val letter: Char,
    val apps: List<AppInfo>
)

private data class PillData(
    val letter: Char,
    val appName: String?
)

private data class ScrollTarget(
    val index: Int,
    val animated: Boolean
)

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
    val hasRecent by remember { derivedStateOf { recentApps.isNotEmpty() } }

    val alphabetGroups by remember(otherApps) {
        derivedStateOf { groupAppsByLetter(otherApps) }
    }
    val railLetters by remember(alphabetGroups) {
        derivedStateOf { alphabetGroups.map { it.letter } }
    }

    val sectionIndexCache by remember(
        alphabetGroups, showFavoritesSection, hasFavorites,
        showRecentAppsSection, hasRecent, favoriteApps.size, recentApps.size
    ) {
        derivedStateOf {
            val offset = computeAlphabetSectionOffset(
                showFavorites = showFavoritesSection && hasFavorites,
                showRecent = showRecentAppsSection && hasRecent,
                favoriteCount = favoriteApps.size,
                recentCount = recentApps.size
            )
            val map = mutableMapOf<Char, Int>()
            var current = offset
            alphabetGroups.forEach { group ->
                map[group.letter] = current
                current += 1 + group.apps.size
            }
            map
        }
    }

    var selectedLetter by remember { mutableStateOf<Char?>(null) }
    var draggedLetter by remember { mutableStateOf<Char?>(null) }
    var isDragging by remember { mutableStateOf(false) }
    var dragPosition by remember { mutableStateOf(0f) }

    val listState = rememberLazyListState()

    var scrollTarget by remember { mutableStateOf<ScrollTarget?>(null) }

    LaunchedEffect(Unit) {
        snapshotFlow { scrollTarget }
            .filterNotNull()
            .collectLatest { target ->
                if (target.animated) {
                    listState.animateScrollToItem(target.index)
                } else {
                    listState.scrollToItem(target.index)
                }
            }
    }

    val pillState = if (isDragging && draggedLetter != null) {
        val group = alphabetGroups.find { it.letter == draggedLetter }
        if (group != null) PillData(draggedLetter!!, group.apps.firstOrNull()?.appName) else null
    } else null

    val showRail = !isSearching && !isLoading && alphabetGroups.isNotEmpty()

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
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
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                if (showRecentAppsSection && hasRecent) {
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
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                if (alphabetGroups.isNotEmpty()) {
                    item(key = "alpha_header") {
                        SectionTitle(text = "All Apps")
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    alphabetGroups.forEach { group ->
                        stickyHeader(key = "sticky_${group.letter}") {
                            AlphabetHeader(letter = group.letter)
                        }

                        itemsIndexed(
                            items = group.apps,
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

        if (showRail) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = 2.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                AlphabetNavigationRail(
                    letters = railLetters,
                    selectedLetter = selectedLetter,
                    onLetterSelected = { letter ->
                        selectedLetter = letter
                        draggedLetter = letter
                        isDragging = true
                        val index = sectionIndexCache[letter]
                        if (index != null) {
                            scrollTarget = ScrollTarget(index, animated = false)
                        }
                    },
                    onDragEnd = {
                        val lastLetter = draggedLetter
                        selectedLetter = null
                        draggedLetter = null
                        isDragging = false
                        if (lastLetter != null) {
                            val index = sectionIndexCache[lastLetter]
                            if (index != null) {
                                scrollTarget = ScrollTarget(index, animated = true)
                            }
                        }
                    },
                    onDragPositionChanged = { y ->
                        if (y >= 0f) {
                            dragPosition = y
                        }
                    }
                )
            }
        }

        AnimatedContent(
            targetState = pillState,
            transitionSpec = {
                fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)) togetherWith
                    fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium))
            },
            label = "pill",
            modifier = Modifier.fillMaxSize()
        ) { state ->
            if (state != null) {
                DragIndicatorPill(
                    letter = state.letter,
                    appName = state.appName,
                    dragY = dragPosition
                )
            }
        }
    }
}

@Composable
private fun DragIndicatorPill(
    letter: Char,
    appName: String?,
    dragY: Float
) {
    val density = LocalDensity.current

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopEnd
    ) {
        Row(
            modifier = Modifier
                .padding(end = 48.dp)
                .offset(y = with(density) { dragY.toDp() - 20.dp })
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = letter.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (appName != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = appName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun AlphabetHeader(letter: Char, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(0.5.dp)
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = letter.toString(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.50f)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(0.5.dp)
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))
        )
    }
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

private fun groupAppsByLetter(apps: List<AppInfo>): List<AlphabetGroup> {
    if (apps.isEmpty()) return emptyList()

    val groups = mutableListOf<AlphabetGroup>()
    var currentLetter: Char? = null
    val currentApps = mutableListOf<AppInfo>()

    for (app in apps) {
        val firstChar = app.appName.firstOrNull()?.uppercaseChar() ?: '#'
        if (firstChar != currentLetter) {
            if (currentLetter != null && currentApps.isNotEmpty()) {
                groups.add(AlphabetGroup(currentLetter, currentApps.toList()))
            }
            currentLetter = firstChar
            currentApps.clear()
        }
        currentApps.add(app)
    }
    if (currentLetter != null && currentApps.isNotEmpty()) {
        groups.add(AlphabetGroup(currentLetter, currentApps.toList()))
    }
    return groups
}

private fun computeAlphabetSectionOffset(
    showFavorites: Boolean,
    showRecent: Boolean,
    favoriteCount: Int,
    recentCount: Int
): Int {
    var offset = 1
    if (showFavorites) {
        offset += 1 + favoriteCount + 1
    }
    if (showRecent) {
        offset += 1 + recentCount + 1
    }
    offset += 1
    return offset
}
