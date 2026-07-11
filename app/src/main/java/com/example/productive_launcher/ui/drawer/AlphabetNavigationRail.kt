package com.example.productive_launcher.ui.drawer

import android.util.Log
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

private const val TAG = "AlphabetRail"

@Composable
fun AlphabetNavigationRail(
    letters: List<Char>,
    selectedLetter: Char?,
    onLetterSelected: (Char) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier,
    onDragPositionChanged: (Float) -> Unit = {}
) {
    if (letters.isEmpty()) return

    val haptic = LocalHapticFeedback.current
    var lastHapticLetter by remember { mutableStateOf<Char?>(null) }

    val railPadding = 4.dp
    val itemHeightDp = 12.dp
    val railWidth = 14.dp
    val touchTargetWidth = 36.dp

    var boxTopRoot by remember { mutableStateOf(0f) }
    var railTopPx by remember { mutableStateOf(0f) }
    var railHeightPx by remember { mutableStateOf(1f) }
    val railTopState = rememberUpdatedState(railTopPx)
    val railHeightState = rememberUpdatedState(railHeightPx)

    Box(
        modifier = modifier
            .width(touchTargetWidth)
            .fillMaxHeight()
            .onGloballyPositioned { coords ->
                boxTopRoot = coords.localToRoot(Offset.Zero).y
            }
            .pointerInput(letters, railTopState, railHeightState) {
                val numLetters = letters.size

                detectDragGestures(
                    onDragStart = { offset ->
                        val touchY = offset.y
                        val localY = touchY - railTopState.value
                        val progress = (localY / railHeightState.value).coerceIn(0f, 1f)
                        val idx = (progress * numLetters).toInt().coerceIn(0, numLetters - 1)
                        val letter = letters[idx]

                        val letterCenterY = railTopState.value +
                                (idx.toFloat() + 0.5f) * (railHeightState.value / numLetters)

                        Log.d(TAG, "DOWN touchY=$touchY railTop=${railTopState.value} " +
                                "localY=$localY railH=${railHeightState.value} " +
                                "progress=$progress idx=$idx letter=$letter " +
                                "letterCenterY=$letterCenterY offset=${touchY - letterCenterY}")

                        lastHapticLetter = letter
                        onLetterSelected(letter)
                        onDragPositionChanged(offset.y)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val touchY = change.position.y
                        val localY = touchY - railTopState.value
                        val progress = (localY / railHeightState.value).coerceIn(0f, 1f)
                        val idx = (progress * numLetters).toInt().coerceIn(0, numLetters - 1)
                        val letter = letters[idx]

                        val letterCenterY = railTopState.value +
                                (idx.toFloat() + 0.5f) * (railHeightState.value / numLetters)

                        if (letter != lastHapticLetter) {
                            Log.d(TAG, "MOVE touchY=$touchY railTop=${railTopState.value} " +
                                    "localY=$localY railH=${railHeightState.value} " +
                                    "progress=$progress idx=$idx letter=$letter " +
                                    "letterCenterY=$letterCenterY offset=${touchY - letterCenterY}")
                            lastHapticLetter = letter
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        onLetterSelected(letter)
                        onDragPositionChanged(touchY)
                    },
                    onDragEnd = {
                        lastHapticLetter = null
                        onDragEnd()
                        onDragPositionChanged(-1f)
                    },
                    onDragCancel = {
                        lastHapticLetter = null
                        onDragEnd()
                        onDragPositionChanged(-1f)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.08f))
                .padding(vertical = railPadding)
                .width(railWidth)
                .onGloballyPositioned { coordinates ->
                    val colInRoot = coordinates.localToRoot(Offset.Zero)
                    railTopPx = colInRoot.y - boxTopRoot
                    railHeightPx = coordinates.size.height.toFloat()

                    Log.d(TAG, "MEASURE railTop=$railTopPx railHeight=$railHeightPx " +
                            "colRoot=${colInRoot.y} boxRoot=$boxTopRoot")
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            letters.forEach { letter ->
                val selIdx = if (selectedLetter != null) {
                    letters.indexOf(selectedLetter)
                } else -1
                val curIdx = letters.indexOf(letter)
                val distance = if (selIdx >= 0 && curIdx >= 0) abs(selIdx - curIdx) else -1

                val targetScale = when {
                    distance < 0 -> 1f
                    distance == 0 -> 1.9f
                    distance == 1 -> 1.35f
                    distance == 2 -> 1.15f
                    else -> 1f
                }

                val targetAlpha = when {
                    distance < 0 -> 0.18f
                    distance == 0 -> 1f
                    distance == 1 -> 0.70f
                    distance == 2 -> 0.45f
                    else -> 0.18f
                }

                val direction = if (curIdx >= 0 && selIdx >= 0 && curIdx != selIdx) {
                    if (curIdx < selIdx) -1 else 1
                } else 0

                val targetOffsetY: Dp = when {
                    distance < 0 -> 0.dp
                    distance == 0 -> 0.dp
                    distance == 1 -> (direction * 7).dp
                    distance == 2 -> (direction * 3).dp
                    else -> 0.dp
                }

                val scale by animateFloatAsState(
                    targetValue = targetScale,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "letterScale"
                )

                val alpha by animateFloatAsState(
                    targetValue = targetAlpha,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "letterAlpha"
                )

                val offsetY by animateDpAsState(
                    targetValue = targetOffsetY,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "letterOffset"
                )

                Text(
                    text = letter.toString(),
                    fontSize = 10.sp,
                    fontWeight = if (distance == 0) FontWeight.Bold else FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(vertical = 0.5.dp)
                        .offset(y = offsetY)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                )
            }
        }
    }
}
