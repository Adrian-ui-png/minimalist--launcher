package com.example.productive_launcher.data.local

import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable

object IconCache {
    private const val MAX_ICON_COUNT = 60
    private const val ICON_SIZE = 48

    private val cache = LruCache<String, ImageBitmap>(MAX_ICON_COUNT)

    fun get(packageName: String): ImageBitmap? {
        return cache.get(packageName)
    }

    fun put(packageName: String, drawable: Drawable?) {
        if (drawable == null) return
        val bitmap = drawableToBitmap(drawable)
        cache.put(packageName, bitmap)
    }

    private fun drawableToBitmap(drawable: Drawable): ImageBitmap {
        return when (drawable) {
            is BitmapDrawable -> {
                val bmp = drawable.bitmap
                if (bmp.width == ICON_SIZE && bmp.height == ICON_SIZE) {
                    bmp.asImageBitmap()
                } else {
                    val scaled = Bitmap.createScaledBitmap(bmp, ICON_SIZE, ICON_SIZE, true)
                    scaled.asImageBitmap()
                }
            }
            else -> {
                val bitmap = Bitmap.createBitmap(ICON_SIZE, ICON_SIZE, Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(bitmap)
                drawable.setBounds(0, 0, ICON_SIZE, ICON_SIZE)
                drawable.draw(canvas)
                bitmap.asImageBitmap()
            }
        }
    }
}
