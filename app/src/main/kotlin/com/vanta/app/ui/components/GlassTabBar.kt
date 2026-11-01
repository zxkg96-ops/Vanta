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
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Островок с вкладками и «стеклянной» таблеткой.
 *
 * Таблетка следует за пейджером: [pagePosition] отдаёт дробную позицию страницы
 * (currentPage + currentPageOffsetFraction), поэтому она едет синхронно с пальцем, листающим контент,
 * и с анимацией перехода после отпускания. Тап по вкладке вызывает [onSelect].
 *
 * Зажми саму таблетку и веди пальцем: она приподнимается и едет за пальцем, вкладка переключается,
 * как только центр таблетки пересекает границу соседней (с гистерезисом). При отпускании учитывается
 * скорость жеста (бросок переносит на соседнюю вкладку), а таблетка мягко переходит под управление
 * пейджера. Любую фазу можно перехватить пальцем.
 */
@Composable
fun GlassTabBar(
    titles: List<String>,
    selected: Int,
    pagePosition: () -> Float,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val plate = RoundedCornerShape(28.dp)
    val currentSelected by rememberUpdatedState(selected)
    val currentOnSelect by rememberUpdatedState(onSelect)
    val currentPagePosition by rememberUpdatedState(pagePosition)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .clip(plate)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp)
    ) {
        val count = titles.size
        // maxWidth is only reachable from the BoxWithConstraints scope, not from the nested Box below.
        val tabWidth = maxWidth / count
        val totalPx = constraints.maxWidth.toFloat()
        val tabPx = totalPx / count
        val maxX = totalPx - tabPx
        val hystPx = with(LocalDensity.current) { 6.dp.toPx() }
        val scope = rememberCoroutineScope()

        var pressed by remember { mutableStateOf(false) }
        var dragging by remember { mutableStateOf(false) }
        // Позиция таблетки, пока её держит палец, и точка, из которой она «перетекает» к пейджеру после отпускания.
        var fingerX by remember { mutableFloatStateOf(0f) }
        var releaseFromX by remember { mutableFloatStateOf(0f) }
        // 0 = таблетка там, где её отпустил палец, 1 = полностью под управлением пейджера.
        val blend = remember { Animatable(1f) }
        // Вкладка, которую таблетка уже запросила; не даёт вызывать onSelect на каждый кадр жеста.
        var requested by remember { mutableIntStateOf(selected) }
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

        LaunchedEffect(selected) {
            if (!dragging) requested = selected
        }

        // Единственный источник позиции таблетки; читается только в фазе рисования.
        fun pillX(): Float =
            if (dragging) fingerX
            else lerp(releaseFromX, currentPagePosition() * tabPx, blend.value)

        Box(
            Modifier
                .fillMaxWidth()
                .height(40.dp)
                .pointerInput(count, tabPx) {
                    detectTapGestures(
                        onPress = { off ->
                            val x = pillX()
                            pressed = off.x >= x && off.x <= x + tabPx
                            tryAwaitRelease()
                            pressed = false
                        },
                        onTap = { off ->
                            val target = (off.x / tabPx).toInt().coerceIn(0, count - 1)
                            requested = target
                            currentOnSelect(target)
                        }
                    )
                }
                .pointerInput(count, tabPx) {
                    var grabbed = false
                    var dragX = 0f
                    val tracker = VelocityTracker()
                    val release = {
                        if (grabbed) {
                            // Бросок: проецируем позицию таблетки по скорости и выбираем вкладку.
                            val vx = tracker.calculateVelocity().x
                            val projected = (fingerX + vx * 0.12f).coerceIn(0f, maxX)
                            val target = ((projected + tabPx / 2f) / tabPx).toInt().coerceIn(0, count - 1)
                            if (target != requested) {
                                requested = target
                                currentOnSelect(target)
                            }
                            releaseFromX = fingerX
                            grabbed = false
                            scope.launch {
                                blend.snapTo(0f)
                                dragging = false
                                blend.animateTo(1f, spring(dampingRatio = 0.82f, stiffness = 260f))
                            }
                        }
                        Unit
                    }
                    detectHorizontalDragGestures(
                        onDragStart = { off ->
                            // Only the pill itself can be dragged; a swipe elsewhere does nothing.
                            val x = pillX()
                            grabbed = off.x >= x - hystPx && off.x <= x + tabPx + hystPx
                            if (grabbed) {
                                tracker.resetTracking()
                                dragX = x
                                fingerX = x
                                requested = currentSelected
                                dragging = true
                            }
                        },
                        onDragEnd = { release() },
                        onDragCancel = { release() },
                        onHorizontalDrag = { change, dx ->
                            if (!grabbed) return@detectHorizontalDragGestures
                            change.consume()
                            tracker.addPosition(change.uptimeMillis, change.position)
                            // A touch of give past the edges, then the pill stops.
                            dragX = (dragX + dx).coerceIn(-10f, maxX + 10f)
                            fingerX = dragX

                            // Switch the tab as soon as the pill centre crosses into another one (with hysteresis).
                            val centre = dragX + tabPx / 2f
                            val cur = requested
                            val curCentre = (cur + 0.5f) * tabPx
                            if (abs(centre - curCentre) > tabPx / 2f + hystPx) {
                                val target = (centre / tabPx).toInt().coerceIn(0, count - 1)
                                if (target != cur) {
                                    requested = target
                                    currentOnSelect(target)
                                }
                            }
                        }
                    )
                }
        ) {
            Box(
                Modifier
                    .width(tabWidth)
                    .height(40.dp)
                    .graphicsLayer {
                        translationX = pillX()
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
