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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.filled.Add
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.productive_launcher.data.model.AppInfo
import com.example.productive_launcher.launcher.LauncherViewModel
import com.example.productive_launcher.ui.components.TaskItem
import com.example.productive_launcher.ui.components.TaskList
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

    // Focus mode & dynamics
    var isFocusModeActive by remember { mutableStateOf(false) }
    var motivationMessage by remember { mutableStateOf(getMotivationalText()) }
    
    val focusUiAlpha by animateFloatAsState(
        targetValue = if (isFocusModeActive) 0.4f else 1.0f,
        animationSpec = tween(500)
    )

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = getFormattedTimeOnly()
            motivationMessage = getMotivationalText()
            delay(1000L)
        }
    }

    val focusRequester = remember { FocusRequester() }
    var isGoalFocused by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 48.dp, bottom = 48.dp, start = 24.dp, end = 24.dp)
    ) {
        // Focus mode overlay for dimming and background blur
        val focusOverlayAlpha by animateFloatAsState(
            targetValue = if (isFocusModeActive) 0.15f else 0f,
            animationSpec = tween(500)
        )
        if (focusOverlayAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = (-24).dp, vertical = (-48).dp)
                    .background(Color.Black.copy(alpha = focusOverlayAlpha))
            )
        }

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
        
        // Top Bar (Aligned Grid Layout)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer(alpha = focusUiAlpha)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Top
        ) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.TopCenter) {
                TopIconTextItem(
                    icon = Icons.Outlined.OpenInNew, 
                    text = "Links",
                    onClick = { showLinksDropdown = !showLinksDropdown }
                )
            }
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.TopCenter) {
                TopIconTextItem(
                    icon = Icons.Outlined.Psychology, 
                    text = "Focus",
                    subText = if (isFocusModeActive) "Active" else null,
                    onClick = { isFocusModeActive = !isFocusModeActive }
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

        // Links Dropdown
        LinksDropdown(
            isVisible = showLinksDropdown,
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
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = currentTime,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 86.sp,
                    fontWeight = FontWeight.ExtraLight,
                    letterSpacing = (-2).sp,
                    lineHeight = 86.sp
                ),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(12.dp)) // Refined spacing
            
            AnimatedContent(
                targetState = motivationMessage,
                transitionSpec = {
                    fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
                },
                label = "motivationTransition"
            ) { targetMessage ->
                Text(
                    text = targetMessage,
                    fontSize = 22.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Light,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp)) // Reduced spacing gap (by ~30%)
            
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .animateContentSize(),
                contentAlignment = Alignment.Center
            ) {
                if (focusText.isEmpty() && !isGoalFocused) {
                    val pillInteractionSource = remember { MutableInteractionSource() }
                    val isPillPressed by pillInteractionSource.collectIsPressedAsState()
                    val pillScale by animateFloatAsState(
                        targetValue = if (isPillPressed) 0.95f else 1f,
                        animationSpec = tween(100)
                    )
                    
                    Row(
                        modifier = Modifier
                            .graphicsLayer(scaleX = pillScale, scaleY = pillScale)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                            .clickable(
                                interactionSource = pillInteractionSource,
                                indication = null
                            ) {
                                isGoalFocused = true
                                focusRequester.requestFocus()
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Add today's goal",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "today's goal",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        BasicTextField(
                            value = focusText,
                            onValueChange = { focusText = it },
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center
                            ),
                            cursorBrush = SolidColor(Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester)
                                .onFocusChanged { isGoalFocused = it.isFocused },
                            decorationBox = { innerTextField ->
                                Box(contentAlignment = Alignment.Center) {
                                    if (focusText.isEmpty()) {
                                        Text(
                                            text = "What is important today?",
                                            color = Color.White.copy(alpha = 0.3f),
                                            fontSize = 18.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                    innerTextField()
                                }
                            },
                            singleLine = true
                        )
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        val lineAlpha by animateFloatAsState(
                            targetValue = if (isGoalFocused) 0.8f else 0.3f,
                            animationSpec = tween(300)
                        )
                        val lineHeight by animateDpAsState(
                            targetValue = if (isGoalFocused) 1.5.dp else 1.dp,
                            animationSpec = tween(300)
                        )
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

        // Bottom Content Area (Intentional spacing quote -> navigation bar)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer(alpha = focusUiAlpha)
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            // Quote (Centered)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            ) {
                Text(
                    text = "\"Do not overestimate the competition and underestimate yourself.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    fontStyle = FontStyle.Italic
                )
                Text(
                    text = "You are better than you think.\"",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    fontStyle = FontStyle.Italic
                )
            }

            // Bottom Navigation Card (Frosted glass pill)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .run {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            blur(12.dp)
                        } else this
                    }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(
                    icon = Icons.Outlined.CheckBox,
                    text = "Tasks",
                    onClick = { showTasks = true }
                )
                
                // Vertical divider line
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(18.dp)
                        .background(Color.White.copy(alpha = 0.15f))
                )
                
                BottomNavItem(
                    icon = Icons.Outlined.ViewList,
                    text = "Apps",
                    onClick = onNavigateToApps
                )
            }
        }
    }

    if (showTasks) {
        Dialog(onDismissRequest = { showTasks = false }) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
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
