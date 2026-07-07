package com.example.productive_launcher.ui.components

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppListItem(
    appName: String,
    icon: Drawable?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false,
    onFavoriteToggle: () -> Unit = {},
    dragHandle: @Composable (() -> Unit)? = null
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showMenu = true }
            )
            .padding(horizontal = 4.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIcon(drawable = icon)
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = appName,
            style = MaterialTheme.typography.bodyLarge,
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
    drawable: Drawable?,
    modifier: Modifier = Modifier
) {
    val imageBitmap = remember(drawable) {
        when (drawable) {
            is BitmapDrawable -> drawable.bitmap.asImageBitmap()
            else -> {
                val bitmap = Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888)
                drawable?.setBounds(0, 0, 48, 48)
                drawable?.draw(android.graphics.Canvas(bitmap))
                bitmap.asImageBitmap()
            }
        }
    }

    Image(
        bitmap = imageBitmap,
        contentDescription = null,
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
    )
}
