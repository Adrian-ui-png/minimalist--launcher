package com.example.productive_launcher.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import com.example.productive_launcher.data.model.AppInfo
import com.example.productive_launcher.launcher.LauncherViewModel
import com.example.productive_launcher.ui.drawer.AppDrawer
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@Composable
fun AppPagerScreen(
    viewModel: LauncherViewModel,
    onAppClick: (AppInfo) -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()

    HorizontalPager(
        state = pagerState,
        modifier = modifier,
        beyondViewportPageCount = 1
    ) { page ->
        val targetAlpha = 1f - (pagerState.getOffsetFraction(page).absoluteValue * 0.15f)
        val targetScale = 1f - (pagerState.getOffsetFraction(page).absoluteValue * 0.03f)
        val alpha by animateFloatAsState(
            targetValue = targetAlpha.coerceIn(0.7f, 1f),
            animationSpec = tween(durationMillis = 300),
            label = "pageAlpha"
        )
        val scale by animateFloatAsState(
            targetValue = targetScale.coerceIn(0.92f, 1f),
            animationSpec = tween(durationMillis = 300),
            label = "pageScale"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(alpha)
                .scale(scale)
        ) {
            when (page) {
                0 -> HomeScreen(
                    viewModel = viewModel,
                    onNavigateToSettings = onNavigateToSettings,
                    onNavigateToApps = {
                        scope.launch {
                            pagerState.animateScrollToPage(1)
                        }
                    },
                    onAppClick = onAppClick,
                    modifier = Modifier.fillMaxSize()
                )
                1 -> AppDrawer(
                    viewModel = viewModel,
                    onAppClick = onAppClick,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

private fun PagerState.getOffsetFraction(page: Int): Float {
    val currentPage = this.currentPage
    val offset = this.currentPageOffsetFraction
    return when {
        page < currentPage -> (page - currentPage + offset)
        page == currentPage -> offset
        else -> (page - currentPage + offset)
    }
}
