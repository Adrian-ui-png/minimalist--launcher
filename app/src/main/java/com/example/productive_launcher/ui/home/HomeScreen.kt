package com.example.productive_launcher.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.ViewList
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import android.os.Build
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.example.productive_launcher.data.model.AppInfo
import com.example.productive_launcher.launcher.LauncherViewModel
import com.example.productive_launcher.ui.components.TaskItem
import com.example.productive_launcher.ui.components.TaskList
import com.example.productive_launcher.ui.components.HeroClock
import com.example.productive_launcher.ui.components.QuoteCard
import com.example.productive_launcher.ui.components.FocusButton
import com.example.productive_launcher.ui.theme.DesignTokens
import com.example.productive_launcher.ui.theme.SharedTypography
import com.example.productive_launcher.ui.theme.MotionSystem
import com.example.productive_launcher.ui.theme.FocusThemeProvider
import com.example.productive_launcher.ui.theme.LocalFocusThemeConfig
import com.example.productive_launcher.ui.animation.animateWallpaperProperties
import com.example.productive_launcher.ui.haptics.FocusHaptics
import androidx.compose.foundation.layout.offset
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.draw.blur
import androidx.compose.ui.text.font.FontStyle

private fun Drawable.toBitmap(): Bitmap {
    if (this is BitmapDrawable) {
        return this.bitmap
    }
    val bitmap = Bitmap.createBitmap(
        intrinsicWidth.coerceAtLeast(1),
        intrinsicHeight.coerceAtLeast(1),
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bitmap
}

private fun getFormattedTimeOnly(): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(Date())
}

private fun getMotivationalText(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> "🌅 What's the one thing that matters today?"
        in 12..16 -> "☀️ Keep going."
        in 17..21 -> "🌇 Finish strong."
        else -> "🌙 Wrap up with intention."
    }
}

private fun formatMinutesSeconds(totalSeconds: Int): String {
    val mins = totalSeconds / 60
    val secs = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
}

sealed class DashboardState {
    object Normal : DashboardState()
    object Commitment : DashboardState()
    object FocusRunning : DashboardState()
    object Completion : DashboardState()
}

@Composable
fun HomeScreen(
    viewModel: LauncherViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToApps: () -> Unit,
    onAppClick: (AppInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    var focusText by remember { mutableStateOf("") }
    var currentTime by remember { mutableStateOf(getFormattedTimeOnly()) }
    val favoriteApps by viewModel.favoriteApps.collectAsState()
    var showTasks by remember { mutableStateOf(false) }
    var tasks by remember { mutableStateOf(listOf<TaskItem>()) }
    var showLinksDropdown by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                Thread {
                    try {
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            val file = java.io.File(context.filesDir, "current_wallpaper.png")
                            val tempFile = java.io.File(context.filesDir, "current_wallpaper_temp.png")
                            java.io.FileOutputStream(tempFile).use { output ->
                                input.copyTo(output)
                            }
                            if (tempFile.exists()) {
                                if (file.exists()) {
                                    file.delete()
                                }
                                tempFile.renameTo(file)
                            }
                            android.util.Log.d("Wallpaper", "Saved selected wallpaper to app data successfully")
                            viewModel.notifyWallpaperChanged()
                        }
                    } catch (e: java.lang.Exception) {
                        android.util.Log.e("Wallpaper", "Failed to save selected wallpaper: ${e.message}")
                    }
                }.start()
            }
        }
    )

    // Focus state architecture
    var dashboardState by remember { mutableStateOf<DashboardState>(DashboardState.Normal) }
    val isFocusActive = dashboardState !is DashboardState.Normal
    var motivationMessage by remember { mutableStateOf(getMotivationalText()) }
    val scope = rememberCoroutineScope()
    var isTransitioning by remember { mutableStateOf(false) }

    // Configured durations from settings
    val configuredFocusMinutes by viewModel.focusDurationMinutes.collectAsState()
    val configuredBreakMinutes by viewModel.breakDurationMinutes.collectAsState()
    var isBreakMode by remember { mutableStateOf(false) }
    var showFocusSettingsDialog by remember { mutableStateOf(false) }

    // Total session seconds based on current mode
    val totalSessionSeconds = (if (isBreakMode) configuredBreakMinutes else configuredFocusMinutes) * 60

    // Timer States
    var focusSecondsRemaining by remember { mutableStateOf(configuredFocusMinutes * 60) }
    var isTimerRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isTimerRunning, isBreakMode, configuredFocusMinutes, configuredBreakMinutes) {
        if (isTimerRunning) {
            while (focusSecondsRemaining > 0) {
                delay(1000L)
                focusSecondsRemaining -= 1
            }
            if (focusSecondsRemaining == 0) {
                FocusHaptics.performSuccess(context)
                if (!isBreakMode) {
                    // Transition to break mode automatically!
                    isBreakMode = true
                    focusSecondsRemaining = configuredBreakMinutes * 60
                } else {
                    // Transition to completion mode
                    isBreakMode = false
                    dashboardState = DashboardState.Completion
                    scope.launch {
                        delay(3000L)
                        dashboardState = DashboardState.Normal
                    }
                }
            }
        }
    }

    LaunchedEffect(dashboardState) {
        val focusActive = dashboardState !is DashboardState.Normal
        viewModel.setFocusActive(focusActive)
        
        if (dashboardState is DashboardState.FocusRunning) {
            isBreakMode = false
            focusSecondsRemaining = configuredFocusMinutes * 60
            isTimerRunning = true
        } else {
            isTimerRunning = false
        }
    }

    val onToggleFocusState: () -> Unit = {
        if (!isTransitioning) {
            isTransitioning = true
            if (dashboardState is DashboardState.Normal) {
                FocusHaptics.performFocusStart(context)
                dashboardState = DashboardState.Commitment
                scope.launch {
                    delay(3000L) // Show commitment quote for 3 seconds
                    dashboardState = DashboardState.FocusRunning
                    isTransitioning = false
                }
            } else {
                FocusHaptics.performSessionComplete(context)
                dashboardState = DashboardState.Completion
                scope.launch {
                    delay(3000L) // Show completion quote for 3 seconds
                    dashboardState = DashboardState.Normal
                    isTransitioning = false
                }
            }
        }
    }

    FocusThemeProvider(isFocusActive = isFocusActive) {
        val focusTheme = LocalFocusThemeConfig.current
        val animationProps by animateWallpaperProperties(focusTheme)

        LaunchedEffect(Unit) {
            while (true) {
                currentTime = getFormattedTimeOnly()
                motivationMessage = getMotivationalText()
                delay(1000L)
            }
        }

        val focusRequester = remember { FocusRequester() }
        var isGoalFocused by remember { mutableStateOf(false) }

        // Animation sequence values: Refined staggered cinematic timelines matching the Apple/Nothing OS reference pacing

        // 1. Wallpaper overlay Alpha (starts at 180ms, takes 600ms, dimming from 0.25f to 0.45f very slowly)
        val overlayAlpha by animateFloatAsState(
            targetValue = if (isFocusActive) 0.45f else 0.25f,
            animationSpec = tween(
                durationMillis = 600,
                delayMillis = if (isFocusActive) 180 else 0,
                easing = MotionSystem.StandardEasing
            ),
            label = "overlayAlpha"
        )

        // 2. Greeting (fades out at 100ms, takes 400ms)
        val greetingAlpha by animateFloatAsState(
            targetValue = if (isFocusActive) 0f else 1f,
            animationSpec = tween(
                durationMillis = 400,
                delayMillis = if (isFocusActive) 100 else 300,
                easing = MotionSystem.StandardEasing
            ),
            label = "greetingAlpha"
        )

        // 3. UI Clutter / Tasks (starts at 120ms, translates down, fades out over 400ms)
        val uiClutterAlpha by animateFloatAsState(
            targetValue = if (isFocusActive) 0f else 1f,
            animationSpec = tween(
                durationMillis = 400,
                delayMillis = if (isFocusActive) 120 else 250,
                easing = MotionSystem.StandardEasing
            ),
            label = "uiClutterAlpha"
        )

        val uiClutterTranslationY by animateDpAsState(
            targetValue = if (isFocusActive) 24.dp else 0.dp,
            animationSpec = tween(
                durationMillis = 400,
                delayMillis = if (isFocusActive) 120 else 250,
                easing = MotionSystem.StandardEasing
            ),
            label = "uiClutterTranslationY"
        )

        // 4. Quote (starts at 100ms, moves down 24dp, fades out over 400ms)
        val quoteAlpha by animateFloatAsState(
            targetValue = if (isFocusActive) 0f else 1f,
            animationSpec = tween(
                durationMillis = 400,
                delayMillis = if (isFocusActive) 100 else 300,
                easing = MotionSystem.StandardEasing
            ),
            label = "quoteAlpha"
        )

        val quoteTranslationY by animateDpAsState(
            targetValue = if (isFocusActive) 24.dp else 0.dp,
            animationSpec = tween(
                durationMillis = 400,
                delayMillis = if (isFocusActive) 100 else 300,
                easing = MotionSystem.StandardEasing
            ),
            label = "quoteTranslationY"
        )

        // 5. Today's Goal (starts at 220ms, translates upward, takes 500ms)
        val goalTranslationY by animateDpAsState(
            targetValue = if (isFocusActive) (-60).dp else 0.dp,
            animationSpec = tween(
                durationMillis = 500,
                delayMillis = if (isFocusActive) 220 else 200,
                easing = MotionSystem.StandardEasing
            ),
            label = "goalTranslationY"
        )

        val goalFontSize by animateFloatAsState(
            targetValue = if (isFocusActive) 24f else 18f,
            animationSpec = tween(
                durationMillis = 500,
                delayMillis = if (isFocusActive) 220 else 200,
                easing = MotionSystem.StandardEasing
            ),
            label = "goalFontSize"
        )

        val goalFontWeight = if (isFocusActive) FontWeight.SemiBold else FontWeight.Normal

        // 6. Circular Progress Ring Sweep (starts at 450ms, takes 700ms)
        val ringProgress by animateFloatAsState(
            targetValue = if (isFocusActive) 1f else 0f,
            animationSpec = tween(
                durationMillis = 700,
                delayMillis = if (isFocusActive) 450 else 0,
                easing = MotionSystem.StandardEasing
            ),
            label = "ringProgress"
        )

        val ringAlpha by animateFloatAsState(
            targetValue = if (isFocusActive) 0.6f else 0f,
            animationSpec = tween(
                durationMillis = 300,
                delayMillis = if (isFocusActive) 450 else 0,
                easing = MotionSystem.StandardEasing
            ),
            label = "ringAlpha"
        )

        // 7. Focus controls (starts at 650ms, translates upward 8dp, fades in over 250ms)
        val focusControlsAlpha by animateFloatAsState(
            targetValue = if (isFocusActive) 1f else 0f,
            animationSpec = tween(
                durationMillis = 250,
                delayMillis = if (isFocusActive) 650 else 0,
                easing = MotionSystem.StandardEasing
            ),
            label = "focusControlsAlpha"
        )

        val focusControlsTranslationY by animateDpAsState(
            targetValue = if (isFocusActive) 0.dp else 8.dp,
            animationSpec = tween(
                durationMillis = 250,
                delayMillis = if (isFocusActive) 650 else 0,
                easing = MotionSystem.StandardEasing
            ),
            label = "focusControlsTranslationY"
        )

        val commitmentAlpha by animateFloatAsState(
            targetValue = if (dashboardState is DashboardState.Commitment) 1f else 0f,
            animationSpec = tween(durationMillis = 600),
            label = "commitmentAlpha"
        )

        val focusRunningAlpha by animateFloatAsState(
            targetValue = if (dashboardState is DashboardState.FocusRunning) 1f else 0f,
            animationSpec = tween(durationMillis = 600),
            label = "focusRunningAlpha"
        )

        val completionAlpha by animateFloatAsState(
            targetValue = if (dashboardState is DashboardState.Completion) 1f else 0f,
            animationSpec = tween(durationMillis = 600),
            label = "completionAlpha"
        )

        val ringProgressFraction = if (totalSessionSeconds > 0) focusSecondsRemaining.toFloat() / totalSessionSeconds.toFloat() else 1f

        Box(modifier = modifier.fillMaxSize()) {
            // Focus mode overlay for dimming using Wallpaper Transition utilities
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = overlayAlpha))
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = DesignTokens.ScreenPaddingVerticalTop,
                        bottom = DesignTokens.ScreenPaddingVerticalBottom,
                        start = DesignTokens.ScreenPaddingHorizontal,
                        end = DesignTokens.ScreenPaddingHorizontal
                    )
            ) {

            if (showLinksDropdown) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { showLinksDropdown = false }
                )
            }
            
            // Top Bar (Aligned Grid Layout) - Clutter reduction applied via contentOpacity animation
            if (uiClutterAlpha > 0f) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer(alpha = uiClutterAlpha)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Top
                ) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.TopCenter) {
                        TopIconTextItem(
                            icon = Icons.Outlined.OpenInNew, 
                            text = "Links",
                            onClick = { if (!isFocusActive) showLinksDropdown = !showLinksDropdown }
                        )
                    }
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.TopCenter) {
                        TopIconTextItem(
                            icon = Icons.Outlined.Psychology, 
                            text = "Focus",
                            subText = if (isFocusActive) "Active" else null,
                            onClick = onToggleFocusState
                        )
                    }
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.TopCenter) {
                        TopIconTextItem(
                            icon = Icons.Outlined.RadioButtonUnchecked, 
                            text = "0m", 
                            subText = "Focused Today"
                        )
                    }
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.TopCenter) {
                        TopIconTextItem(
                            icon = Icons.Outlined.Cloud, 
                            text = "28°", 
                            subText = "Kochi"
                        )
                    }
                }
            }

            // Links Dropdown
            LinksDropdown(
                isVisible = showLinksDropdown && !isFocusActive,
                onLinkClick = { url ->
                    showLinksDropdown = false
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // Ignore
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 64.dp, start = 12.dp)
            )
            // Center Section (Proper Middle & Spacing refinement)
            Box(
                modifier = Modifier.align(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {
                // State 1: Normal Layout
                val normalAlpha by animateFloatAsState(
                    targetValue = if (dashboardState is DashboardState.Normal) 1f else 0f,
                    animationSpec = tween(durationMillis = 400),
                    label = "normalAlpha"
                )
                if (normalAlpha > 0f) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.graphicsLayer(alpha = normalAlpha)
                    ) {
                        // Reusable Clock component
                        HeroClock(time = currentTime)
                        
                        Spacer(modifier = Modifier.height(DesignTokens.SpacingMedium))
                        
                        if (greetingAlpha > 0f) {
                            AnimatedContent(
                                targetState = motivationMessage,
                                transitionSpec = {
                                    fadeIn(
                                        animationSpec = tween(
                                            durationMillis = MotionSystem.DurationSlow,
                                            easing = MotionSystem.StandardEasing
                                        )
                                    ) togetherWith fadeOut(
                                        animationSpec = tween(
                                            durationMillis = MotionSystem.DurationSlow,
                                            easing = MotionSystem.StandardEasing
                                        )
                                    )
                                },
                                label = "motivationTransition",
                                modifier = Modifier.graphicsLayer(alpha = greetingAlpha)
                            ) { targetMessage ->
                                Text(
                                    text = targetMessage,
                                    style = SharedTypography.Greeting,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(DesignTokens.SpacingExtraLarge))
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .offset(y = goalTranslationY)
                                .animateContentSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            val pillAlpha = if (isFocusActive) 0f else (if (focusText.isEmpty() && !isGoalFocused) 1f else 0f)
                            if (pillAlpha > 0f) {
                                val pillInteractionSource = remember { MutableInteractionSource() }
                                val isPillPressed by pillInteractionSource.collectIsPressedAsState()
                                val pillScale by animateFloatAsState(
                                    targetValue = if (isPillPressed) 0.95f else 1f,
                                    animationSpec = tween(
                                        durationMillis = MotionSystem.DurationFast,
                                        easing = MotionSystem.StandardEasing
                                    ),
                                    label = "goalPillScale"
                                )
                                
                                Row(
                                    modifier = Modifier
                                        .graphicsLayer(alpha = pillAlpha, scaleX = pillScale, scaleY = pillScale)
                                        .clip(RoundedCornerShape(DesignTokens.CornerRadiusPill))
                                        .background(Color.White.copy(alpha = 0.1f))
                                        .clickable(
                                            interactionSource = pillInteractionSource,
                                            indication = null,
                                            enabled = pillAlpha > 0.5f
                                        ) {
                                            isGoalFocused = true
                                            focusRequester.requestFocus()
                                        }
                                        .padding(horizontal = DesignTokens.SpacingMedium, vertical = DesignTokens.SpacingSmall),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = DesignTokens.OpacityHigh),
                                        modifier = Modifier.size(DesignTokens.IconSizeSmall)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Add today's goal",
                                        color = Color.White.copy(alpha = DesignTokens.OpacityHigh),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            val textFieldAlpha = if (isFocusActive) 1f else (if (focusText.isNotEmpty() || isGoalFocused) 1f else 0f)
                            if (textFieldAlpha > 0f) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .graphicsLayer(alpha = textFieldAlpha)
                                        .fillMaxWidth()
                                ) {
                                    val headerAlpha = if (isFocusActive) 0f else 1f
                                    val animatedHeaderAlpha by animateFloatAsState(
                                        targetValue = headerAlpha,
                                        animationSpec = tween(durationMillis = 300),
                                        label = "headerAlpha"
                                    )
                                    if (animatedHeaderAlpha > 0f) {
                                        Text(
                                            text = "today's goal",
                                            style = SharedTypography.SectionLabel,
                                            modifier = Modifier.graphicsLayer(alpha = animatedHeaderAlpha)
                                        )
                                        Spacer(modifier = Modifier.height(DesignTokens.SpacingSmall))
                                    }
                                    
                                    BasicTextField(
                                        value = if (isFocusActive) (if (focusText.isEmpty()) "Focus Session" else focusText) else focusText,
                                        onValueChange = { if (!isFocusActive) focusText = it },
                                        readOnly = isFocusActive,
                                        textStyle = TextStyle(
                                            color = Color.White,
                                            fontSize = goalFontSize.sp,
                                            fontWeight = goalFontWeight,
                                            textAlign = TextAlign.Center
                                        ),
                                        cursorBrush = SolidColor(Color.White),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .focusRequester(focusRequester)
                                            .onFocusChanged { isGoalFocused = it.isFocused },
                                        decorationBox = { innerTextField ->
                                            Box(contentAlignment = Alignment.Center) {
                                                if (focusText.isEmpty() && !isFocusActive) {
                                                    Text(
                                                        text = "What is important today?",
                                                        color = Color.White.copy(alpha = DesignTokens.OpacityDisabled),
                                                        fontSize = goalFontSize.sp,
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                                innerTextField()
                                            }
                                        },
                                        singleLine = true
                                    )
                                    
                                    Spacer(modifier = Modifier.height(6.dp))
                                    
                                    val lineAlphaTarget = if (isFocusActive) 0f else (if (isGoalFocused) DesignTokens.OpacityHigh else DesignTokens.OpacityDisabled)
                                    val lineAlpha by animateFloatAsState(
                                        targetValue = lineAlphaTarget,
                                        animationSpec = tween(
                                            durationMillis = MotionSystem.DurationNormal,
                                            easing = MotionSystem.StandardEasing
                                        )
                                    )
                                    val lineHeight by animateDpAsState(
                                        targetValue = if (isGoalFocused && !isFocusActive) 1.5.dp else 1.dp,
                                        animationSpec = tween(
                                            durationMillis = MotionSystem.DurationNormal,
                                            easing = MotionSystem.StandardEasing
                                        )
                                    )
                                    if (lineAlpha > 0f) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(0.9f)
                                                .height(lineHeight)
                                                .background(Color.White.copy(alpha = lineAlpha))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // State 2: Commitment Layout (Photo 1)
                if (commitmentAlpha > 0f) {
                    Text(
                        text = "I commit to mindful productivity.",
                        style = SharedTypography.FocusTitle.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Light,
                            letterSpacing = 0.5.sp
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .graphicsLayer(alpha = commitmentAlpha)
                            .padding(horizontal = 32.dp)
                    )
                }

                // State 4: Completion Layout
                if (completionAlpha > 0f) {
                    Text(
                        text = "Good job focusing! Take a mindful moment.",
                        style = SharedTypography.FocusTitle.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Light,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .graphicsLayer(alpha = completionAlpha)
                            .padding(horizontal = 32.dp)
                    )
                }

                // State 3: FocusRunning Layout (Photo 2)
                if (focusRunningAlpha > 0f) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .graphicsLayer(alpha = focusRunningAlpha)
                            .size(320.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Background open arc
                            drawArc(
                                color = Color.White.copy(alpha = 0.15f),
                                startAngle = 140f,
                                sweepAngle = 260f,
                                useCenter = false,
                                style = Stroke(
                                    width = 4.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            )
                            // Active progress open arc
                            drawArc(
                                color = Color.White.copy(alpha = 0.8f),
                                startAngle = 140f,
                                sweepAngle = 260f * ringProgressFraction,
                                useCenter = false,
                                style = Stroke(
                                    width = 4.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(top = 28.dp, bottom = 12.dp, start = 24.dp, end = 24.dp)
                        ) {
                            Row(
                                 horizontalArrangement = Arrangement.Center,
                                 verticalAlignment = Alignment.CenterVertically
                             ) {
                                 Text(
                                     text = "FOCUS",
                                     color = if (!isBreakMode) Color.White else Color.White.copy(alpha = 0.35f),
                                     fontSize = 12.sp,
                                     fontWeight = FontWeight.Bold,
                                     letterSpacing = 1.sp,
                                     modifier = Modifier.clickable {
                                         if (isBreakMode) {
                                             FocusHaptics.performSelection(context)
                                             isBreakMode = false
                                             focusSecondsRemaining = configuredFocusMinutes * 60
                                             isTimerRunning = true
                                         }
                                     }
                                 )
                                 Spacer(modifier = Modifier.width(16.dp))
                                 Text(
                                     text = "BREAK",
                                     color = if (isBreakMode) Color.White else Color.White.copy(alpha = 0.35f),
                                     fontSize = 12.sp,
                                     fontWeight = FontWeight.Bold,
                                     letterSpacing = 1.sp,
                                     modifier = Modifier.clickable {
                                         if (!isBreakMode) {
                                             FocusHaptics.performSelection(context)
                                             isBreakMode = true
                                             focusSecondsRemaining = configuredBreakMinutes * 60
                                             isTimerRunning = true
                                         }
                                     }
                                 )
                             }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = formatMinutesSeconds(focusSecondsRemaining),
                                style = TextStyle(
                                    color = Color.White,
                                    fontSize = 72.sp,
                                    fontWeight = FontWeight.Light,
                                    letterSpacing = (-2).sp
                                ),
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text(
                                text = if (focusText.isEmpty()) "I will focus on..." else "I will focus on $focusText",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(28.dp))
                            
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.15f))
                                    .clickable {
                                        FocusHaptics.performSelection(context)
                                        isTimerRunning = !isTimerRunning
                                    }
                            ) {
                                Icon(
                                    imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Focusing Indicator Pill (Top Center Overlay)
            if (focusRunningAlpha > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer(alpha = focusRunningAlpha)
                        .align(Alignment.TopCenter),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(DesignTokens.CornerRadiusPill))
                            .background(Color.White.copy(alpha = 0.15f))
                            .clickable {
                                onToggleFocusState()
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Psychology,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Focusing",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // Stop square button
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color.White, shape = RoundedCornerShape(1.dp))
                        )
                    }
                }
            }

            // Focus Mode Bottom Action Bar Overlay (Photo 2)
            if (focusRunningAlpha > 0f) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer(alpha = focusRunningAlpha)
                        .align(Alignment.BottomCenter)
                        .padding(bottom = DesignTokens.SpacingSmall, start = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Settings",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier
                            .size(24.dp)
                            .clickable {
                                FocusHaptics.performSelection(context)
                                showFocusSettingsDialog = true
                            }
                    )

                    Text(
                        text = "Block out everything but your next task.",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        fontStyle = FontStyle.Italic,
                        textAlign = TextAlign.Center
                    )

                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = "Complete Session",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier
                            .size(24.dp)
                            .clickable {
                                onToggleFocusState()
                            }
                    )
                }
            }

            // Bottom Content Area (Intentional spacing quote -> navigation bar) - Clutter reduction completely fades it out in Focus Mode
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = DesignTokens.SpacingSmall),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(DesignTokens.SpacingLarge)
            ) {
                // Reusable QuoteCard
                QuoteCard(
                    quote = "\"Do not overestimate the competition and underestimate yourself. You are better than you think.\"",
                    modifier = Modifier
                        .graphicsLayer(alpha = quoteAlpha)
                        .offset(y = quoteTranslationY)
                )

                if (uiClutterAlpha > 0f) {
                    // Bottom Navigation Card (Frosted glass pill) - Fades out in Focus Mode
                    Row(
                        modifier = Modifier
                            .graphicsLayer(alpha = uiClutterAlpha)
                            .offset(y = uiClutterTranslationY)
                            .clip(RoundedCornerShape(DesignTokens.CornerRadiusPill))
                            .background(Color.White.copy(alpha = 0.08f))
                            .run {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    blur(12.dp)
                                } else this
                            }
                            .padding(horizontal = DesignTokens.SpacingSmall, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.SpacingSmall),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BottomNavItem(
                            icon = Icons.Outlined.CheckBox,
                            text = "Tasks",
                            onClick = { if (!isFocusActive) showTasks = true }
                        )
                        
                        // Vertical divider line
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(18.dp)
                                .background(Color.White.copy(alpha = DesignTokens.OpacityClutter))
                        )
                        
                        BottomNavItem(
                            icon = Icons.Outlined.ViewList,
                            text = "Apps",
                            onClick = onNavigateToApps
                        )
                    }
                }
            }
        }
    }

        if (showTasks && !isFocusActive) {
            Dialog(onDismissRequest = { showTasks = false }) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(DesignTokens.CornerRadiusLarge))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(DesignTokens.SpacingMedium)
                ) {
                    TaskList(
                        tasks = tasks,
                        onToggle = { id ->
                            tasks = tasks.map {
                                if (it.id == id) it.copy(isChecked = !it.isChecked) else it
                            }
                        },
                        onDelete = { id ->
                            tasks = tasks.filter { it.id != id }
                        },
                        onAdd = { text ->
                            val newId = (tasks.maxOfOrNull { it.id } ?: 0) + 1
                            tasks = tasks + TaskItem(id = newId, text = text, isChecked = false)
                        }
                    )
                }
            }
        }

        if (showFocusSettingsDialog) {
            Dialog(onDismissRequest = { showFocusSettingsDialog = false }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .fillMaxHeight(0.85f)
                        .clip(RoundedCornerShape(DesignTokens.CornerRadiusLarge))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(DesignTokens.SpacingMedium)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "Focus Mode Settings",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(DesignTokens.SpacingMedium))
                        
                        // Focus Duration configuration
                        Text(
                            text = "Focus Duration: $configuredFocusMinutes mins",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            TextButton(onClick = { if (configuredFocusMinutes > 5) viewModel.setFocusDuration(configuredFocusMinutes - 5) }) {
                                Text("-5m", color = MaterialTheme.colorScheme.primary)
                            }
                            TextButton(onClick = { if (configuredFocusMinutes < 120) viewModel.setFocusDuration(configuredFocusMinutes + 5) }) {
                                Text("+5m", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(DesignTokens.SpacingSmall))

                        // Break Duration configuration
                        Text(
                            text = "Break Duration: $configuredBreakMinutes mins",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            TextButton(onClick = { if (configuredBreakMinutes > 1) viewModel.setBreakDuration(configuredBreakMinutes - 1) }) {
                                Text("-1m", color = MaterialTheme.colorScheme.primary)
                            }
                            TextButton(onClick = { if (configuredBreakMinutes < 60) viewModel.setBreakDuration(configuredBreakMinutes + 1) }) {
                                Text("+1m", color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Spacer(modifier = Modifier.height(DesignTokens.SpacingSmall))

                        Text(
                            text = "Wallpaper Settings:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        TextButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        ) {
                            Text("Select Home Screen Wallpaper File", color = MaterialTheme.colorScheme.primary)
                        }

                        Spacer(modifier = Modifier.height(DesignTokens.SpacingMedium))
                        
                        Text(
                            text = "Hide Apps during Focus Period:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val allApps by viewModel.apps.collectAsState()
                        val protectedAppsSet by viewModel.protectedApps.collectAsState()
                        
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(allApps) { app ->
                                val isProtected = protectedAppsSet.contains(app.packageName)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.setAppProtected(app.packageName, !isProtected)
                                        }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val iconDrawable = app.icon
                                    if (iconDrawable != null) {
                                        val iconPainter = remember(iconDrawable) {
                                            try {
                                                androidx.compose.ui.graphics.painter.BitmapPainter(iconDrawable.toBitmap().asImageBitmap())
                                            } catch (e: Exception) {
                                                null
                                            }
                                        }
                                        if (iconPainter != null) {
                                            Image(
                                                painter = iconPainter,
                                                contentDescription = null,
                                                modifier = Modifier.size(32.dp)
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .background(Color.White.copy(alpha = 0.1f), shape = CircleShape)
                                            )
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .background(Color.White.copy(alpha = 0.1f), shape = CircleShape)
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.width(12.dp))
                                    
                                    Text(
                                        text = app.appName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    
                                    Checkbox(
                                        checked = isProtected,
                                        onCheckedChange = { checked ->
                                            viewModel.setAppProtected(app.packageName, checked)
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = MaterialTheme.colorScheme.primary,
                                            uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(DesignTokens.SpacingMedium))
                        
                        TextButton(
                            onClick = { showFocusSettingsDialog = false },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Done", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopIconTextItem(
    icon: ImageVector, 
    text: String, 
    subText: String? = null,
    onClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = tween(100)
    )
    val alpha by animateFloatAsState(
        targetValue = if (isPressed) 0.6f else 1f,
        animationSpec = tween(100)
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = if (onClick != null) {
            Modifier
                .graphicsLayer(scaleX = scale, scaleY = scale, alpha = alpha)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
        } else {
            Modifier
        }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = text,
            color = Color.White,
            fontSize = 14.sp
        )
        if (subText != null) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subText,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun BottomNavItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = tween(100)
    )
    val alpha by animateFloatAsState(
        targetValue = if (isPressed) 0.6f else 0.85f,
        animationSpec = tween(100)
    )

    Row(
        modifier = Modifier
            .graphicsLayer(scaleX = scale, scaleY = scale, alpha = alpha)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun FavoriteAppItem(app: AppInfo, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable(onClick = onClick).padding(vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = app.appName.firstOrNull()?.uppercase() ?: "",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Light
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = app.appName.lowercase(),
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Light,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
    }
}

@Composable
fun LinksDropdown(
    isVisible: Boolean,
    onLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(150)) + expandVertically(animationSpec = tween(200)),
        exit = fadeOut(animationSpec = tween(150)) + shrinkVertically(animationSpec = tween(200)),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.width(280.dp)
        ) {
            // Triangle pointer
            Box(
                modifier = Modifier
                    .padding(start = 20.dp)
                    .size(10.dp)
                    .graphicsLayer(rotationZ = 45f)
                    .background(Color(0xFF121212))
            )
            Spacer(modifier = Modifier.height(-5.dp))
            
            // Content Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF121212))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.OpenInNew,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Links",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Link",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                        MoreHorizIcon()
                    }
                }
                
                // Link Items
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    DropdownLinkItem(
                        icon = { YoutubeIcon() },
                        name = "youtube",
                        onClick = { onLinkClick("https://youtube.com") }
                    )
                    DropdownLinkItem(
                        icon = { ClassroomIcon() },
                        name = "classroom",
                        onClick = { onLinkClick("https://classroom.google.com") }
                    )
                }
            }
        }
    }
}

@Composable
fun DropdownLinkItem(
    icon: @Composable () -> Unit,
    name: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        icon()
        Text(
            text = name,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
fun MoreHorizIcon(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.width(20.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.7f))
            )
        }
    }
}

@Composable
fun YoutubeIcon() {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color.Red),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(10.dp)) {
            val path = Path().apply {
                moveTo(size.width * 0.35f, size.height * 0.25f)
                lineTo(size.width * 0.75f, size.height * 0.5f)
                lineTo(size.width * 0.35f, size.height * 0.75f)
                close()
            }
            drawPath(path, Color.White)
        }
    }
}

@Composable
fun ClassroomIcon() {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF137333))
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF137333)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(12.dp)) {
                drawRoundRect(
                    color = Color(0xFFF1B300),
                    size = size,
                    cornerRadius = CornerRadius(2f, 2f),
                    style = Stroke(width = 2f)
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.5f,
                    center = Offset(size.width / 2, size.height / 2 - 1.5f)
                )
                drawArc(
                    color = Color.White,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(size.width / 2 - 3.5f, size.height / 2 + 1f),
                    size = Size(7f, 6f)
                )
            }
        }
    }
}
