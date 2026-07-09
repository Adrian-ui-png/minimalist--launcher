package com.example.productive_launcher.ui.delay

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.productive_launcher.data.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val LARGE_ICON_SIZE = 96

@Composable
fun AppIconLarge(
    packageName: String,
    contentDescription: String? = null,
    modifier: Modifier = Modifier
) {
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    val context = LocalContext.current

    LaunchedEffect(packageName) {
        val bitmap = withContext(Dispatchers.IO) {
            loadLargeIcon(context, packageName)
        }
        imageBitmap = bitmap
    }

    val bitmap = imageBitmap
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = contentDescription,
            modifier = modifier
                .semantics {
                    if (contentDescription != null) {
                        this.contentDescription = contentDescription
                    }
                }
                .clip(RoundedCornerShape(20.dp)),
            contentScale = ContentScale.Fit
        )
    } else {
        Box(
            modifier = modifier
                .size(96.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        )
    }
}

private suspend fun loadLargeIcon(context: Context, packageName: String): ImageBitmap? {
    val repo = AppRepository(context.packageManager)
    val drawable = repo.getIcon(packageName) ?: return null
    return when (drawable) {
        is BitmapDrawable -> {
            val bmp = drawable.bitmap
            if (bmp.width == LARGE_ICON_SIZE && bmp.height == LARGE_ICON_SIZE) {
                bmp.asImageBitmap()
            } else {
                val scaled = Bitmap.createScaledBitmap(bmp, LARGE_ICON_SIZE, LARGE_ICON_SIZE, true)
                scaled.asImageBitmap()
            }
        }
        else -> {
            val bitmap = Bitmap.createBitmap(LARGE_ICON_SIZE, LARGE_ICON_SIZE, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)
            drawable.setBounds(0, 0, LARGE_ICON_SIZE, LARGE_ICON_SIZE)
            drawable.draw(canvas)
            bitmap.asImageBitmap()
        }
    }
}
