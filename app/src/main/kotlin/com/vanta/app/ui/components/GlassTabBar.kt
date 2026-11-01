package com.vanta.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Островок с вкладками и «стеклянной» таблеткой.
 * Тап по вкладке — таблетка перетекает к ней. Зажми саму таблетку и веди пальцем:
 * она приподнимается, едет за пальцем, а вкладка переключается сразу, как только
 * центр таблетки пересекает границу соседней вкладки.
 */
@Composable
fun GlassTabBar(
    titles: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val plate = RoundedCornerShape(28.dp)
    val currentSelected by rememberUpdatedState(selected)
    val currentOnSelect by rememberUpdatedState(onSelect)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .clip(plate)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp)
    ) {
        val count = titles.size
        val totalPx = constraints.maxWidth.toFloat()
        val tabPx = totalPx / count
        val maxX = totalPx - tabPx
        val hystPx = with(LocalDensity.current) { 6.dp.toPx() }
        val scope = rememberCoroutineScope()

        // Position of the pill in pixels. Only its render-layer offset changes while animating.
        val indicatorX = remember { Animatable(selected * tabPx) }
        var pressed by remember { mutableStateOf(false) }
        var dragging by remember { mutableStateOf(false) }
        var dragX by remember { mutableFloatStateOf(0f) }
        val lifted = pressed || dragging

        val lift by animateFloatAsState(
            targetValue = if (lifted) 1.06f else 1f,
            animationSpec = spring(dampingRatio = 0.7f, stiffness = 520f),
            label = "pillLift"
        )
        val glow by animateFloatAsState(
            targetValue = if (lifted) 0.26f else 0.16f,
            animationSpec = tween(160),
            label = "pillGlow"
        )

        LaunchedEffect(selected, tabPx) {
            // While the finger holds the pill, the finger owns its position.
            if (!dragging) {
                indicatorX.animateTo(selected * tabPx, spring(dampingRatio = 0.76f, stiffness = 380f))
            }
        }

        Box(
            Modifier
                .fillMaxWidth()
                .height(40.dp)
                .pointerInput(count, tabPx) {
                    detectTapGestures(
                        onPress = { off ->
                            pressed = off.x >= indicatorX.value && off.x <= indicatorX.value + tabPx
                            tryAwaitRelease()
                            pressed = false
                        },
                        onTap = { off ->
                            currentOnSelect((off.x / tabPx).toInt().coerceIn(0, count - 1))
                        }
                    )
                }
                .pointerInput(count, tabPx) {
                    var grabbed = false
                    val settle = {
                        dragging = false
                        grabbed = false
                        scope.launch {
                            indicatorX.animateTo(
                                currentSelected * tabPx,
                                spring(dampingRatio = 0.76f, stiffness = 380f)
                            )
                        }
                        Unit
                    }
                    detectHorizontalDragGestures(
                        onDragStart = { off ->
                            // Only the pill itself can be dragged; a swipe elsewhere does nothing.
                            grabbed = off.x >= indicatorX.value - hystPx &&
                                off.x <= indicatorX.value + tabPx + hystPx
                            if (grabbed) {
                                dragging = true
                                dragX = indicatorX.value
                            }
                        },
                        onDragEnd = { settle() },
                        onDragCancel = { settle() },
                        onHorizontalDrag = { change, dx ->
                            if (!grabbed) return@detectHorizontalDragGestures
                            change.consume()
                            // A touch of give past the edges, then the pill stops.
                            dragX = (dragX + dx).coerceIn(-10f, maxX + 10f)
                            scope.launch { indicatorX.snapTo(dragX) }

                            // Switch the tab as soon as the pill centre crosses into another one (with hysteresis).
                            val centre = dragX + tabPx / 2f
                            val cur = currentSelected
                            val curCentre = (cur + 0.5f) * tabPx
                            if (abs(centre - curCentre) > tabPx / 2f + hystPx) {
                                val target = (centre / tabPx).toInt().coerceIn(0, count - 1)
                                if (target != cur) {
                                    currentOnSelect(target)
                                }
                            }
                        }
                    )
                }
        ) {
            Box(
                Modifier
                    .width(maxWidth / count)
                    .height(40.dp)
                    .graphicsLayer {
                        translationX = indicatorX.value
                        scaleX = lift
                        scaleY = lift
                    }
                    .flatFill(RoundedCornerShape(24.dp), Color.White.copy(alpha = glow))
            )

            Row(Modifier.fillMaxSize()) {
                titles.forEachIndexed { i, title ->
                    val tint by animateColorAsState(
                        targetValue = if (selected == i) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        animationSpec = tween(200),
                        label = "tabTint"
                    )
                    Box(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            color = tint,
                            fontSize = 12.sp,
                            // Constant weight: switching Bold/Normal re-measures the text and makes the label jump.
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}
