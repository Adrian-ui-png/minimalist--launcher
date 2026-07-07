package com.example.productive_launcher.ui.delay

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun AppIconLarge(
    drawable: Drawable?,
    contentDescription: String? = null,
    modifier: Modifier = Modifier
) {
    val imageBitmap = remember(drawable) {
        when (drawable) {
            is BitmapDrawable -> drawable.bitmap.asImageBitmap()
            else -> {
                val bitmap = Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888)
                drawable?.setBounds(0, 0, 96, 96)
                drawable?.draw(android.graphics.Canvas(bitmap))
                bitmap.asImageBitmap()
            }
        }
    }

    Image(
        bitmap = imageBitmap,
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
}
