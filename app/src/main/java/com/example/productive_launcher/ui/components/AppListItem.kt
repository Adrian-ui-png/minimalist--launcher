package com.example.productive_launcher.ui.components

import android.content.Context
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.productive_launcher.data.local.IconCache
import com.example.productive_launcher.data.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppListItem(
    appName: String,
    packageName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false,
    onFavoriteToggle: () -> Unit = {},
    dragHandle: @Composable (() -> Unit)? = null
) {
    var showMenu by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    val iconScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "iconPressScale"
    )

    LaunchedEffect(isPressed) {
        if (isPressed) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = { showMenu = true }
            )
            .padding(horizontal = 4.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = iconScale
                    scaleY = iconScale
                    alpha = 0.88f
                }
                .clip(RoundedCornerShape(4.dp))
        ) {
            AppIcon(
                packageName = packageName,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(20.dp))
        Text(
            text = appName,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.02.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (dragHandle != null) {
            dragHandle()
        }
    }

    DropdownMenu(
        expanded = showMenu,
        onDismissRequest = { showMenu = false }
    ) {
        DropdownMenuItem(
            text = {
                Text(
                    text = if (isFavorite) "Remove from Favorites"
                    else "Add to Favorites"
                )
            },
            onClick = {
                onFavoriteToggle()
                showMenu = false
            }
        )
    }
}

@Composable
fun AppIcon(
    packageName: String,
    modifier: Modifier = Modifier
) {
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    val context = LocalContext.current

    LaunchedEffect(packageName) {
        val cached = IconCache.get(packageName)
        if (cached != null) {
            imageBitmap = cached
        } else {
            val bitmap = withContext(Dispatchers.IO) {
                loadAndCacheIcon(context, packageName)
            }
            imageBitmap = bitmap
        }
    }

    val bitmap = imageBitmap
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = modifier.size(44.dp)
        )
    } else {
        Box(
            modifier = modifier
                .size(44.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        )
    }
}

private suspend fun loadAndCacheIcon(context: Context, packageName: String): ImageBitmap? {
    val appRepo = AppRepository(context.packageManager)
    val drawable = appRepo.getIcon(packageName) ?: return null
    IconCache.put(packageName, drawable)
    return IconCache.get(packageName)
}
