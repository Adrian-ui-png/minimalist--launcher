package com.example.productive_launcher.ui.components

import android.os.Build
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.productive_launcher.R
import com.example.productive_launcher.launcher.LauncherViewModel
import com.example.productive_launcher.ui.theme.MotionSystem
import java.io.File

@Composable
fun WallpaperBackground(
    isFocusActive: Boolean = false,
    viewModel: LauncherViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val wallpaperTrigger by viewModel.wallpaperUpdateTrigger.collectAsState()

    val wallpaperFile = remember(wallpaperTrigger) {
        File(context.filesDir, "current_wallpaper.png")
    }

    val imageBitmap = remember(wallpaperFile, wallpaperTrigger) {
        if (wallpaperFile.exists()) {
            try {
                android.graphics.BitmapFactory.decodeFile(wallpaperFile.absolutePath)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    // Zoom/scale animation (1.0f -> 1.15f when focus is active)
    val scale by animateFloatAsState(
        targetValue = if (isFocusActive) 1.15f else 1.0f,
        animationSpec = tween(
            durationMillis = 800,
            easing = MotionSystem.StandardEasing
        ),
        label = "wallpaperScale"
    )

    // Blur radius animation (0dp -> 16dp when focus is active)
    val blurRadius by animateDpAsState(
        targetValue = if (isFocusActive) 16.dp else 0.dp,
        animationSpec = tween(
            durationMillis = 800,
            easing = MotionSystem.StandardEasing
        ),
        label = "wallpaperBlur"
    )

    Box(modifier = modifier.fillMaxSize()) {
        if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(scaleX = scale, scaleY = scale)
                    .run {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurRadius > 0.dp) {
                            blur(blurRadius)
                        } else this
                    }
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.default_wallpaper),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(scaleX = scale, scaleY = scale)
                    .run {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurRadius > 0.dp) {
                            blur(blurRadius)
                        } else this
                    }
            )
        }
    }
}
