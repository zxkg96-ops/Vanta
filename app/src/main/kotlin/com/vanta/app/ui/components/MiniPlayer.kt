package com.vanta.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.vanta.core.Track
import kotlinx.coroutines.launch

/** Высота самого мини-плеера. */
val MiniPlayerHeight = 64.dp

/** Сколько места мини-плеер занимает снизу вместе с внешними отступами (по 10 dp сверху и снизу). */
val MiniPlayerSlotHeight = MiniPlayerHeight + 20.dp

/** Размер и скругление обложки внутри мини-плеера (от них стартует полёт обложки). */
val MiniCoverSize = 48.dp
val MiniCoverCorner = 10.dp

/**
 * Внешние отступы мини-плеера и отступ обложки от его левого края. По ним VantaRoot точно знает,
 * где обложка лежит в покое, и считает от этой точки полёт (обложка всегда на расстоянии
 * (MiniPlayerHeight - MiniCoverSize) / 2 от верха мини-плеера).
 */
val MiniOuterHorizontal = 16.dp
val MiniOuterVertical = 10.dp
val MiniInnerStart = 8.dp

private const val MODE_NONE = 0
private const val MODE_SHEET = 1
private const val MODE_DISMISS = 2

/**
 * Мини-плеер. Живёт в VantaRoot рядом со шторкой. [coverHidden] прячет его обложку на время полёта.
 *
 * Жест по вертикали решается на первом движении:
 * - вверх: мини-плеер отдаёт движение шторке ([onSheetDrag]), она открывается за пальцем,
 *   при отпускании шторка сама подхватывает скорость ([onSheetDragStopped]);
 * - вниз: мини-плеер едет за пальцем и закрывает плеер ([onDismiss]) или пружиной возвращается.
 * Любую анимацию можно перехватить пальцем на лету.
 */
@Composable
fun MiniPlayer(
    track: Track,
    isPlaying: Boolean,
    onOpen: () -> Unit,
    onToggle: () -> Unit,
    onNext: () -> Unit,
    onSheetDrag: (Float) -> Unit,
    onSheetDragStopped: (Float) -> Unit,
    coverHidden: () -> Boolean = { false },
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null
) {
    val source = remember { MutableInteractionSource() }
    val offsetY = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var mode by remember { mutableIntStateOf(MODE_NONE) }

    Surface(
        modifier = modifier.fillMaxWidth()
            .graphicsLayer {
                translationY = offsetY.value
                alpha = (1f - offsetY.value.coerceAtLeast(0f) / 400f).coerceIn(0.4f, 1f)
            }
            .draggable(
                state = rememberDraggableState { dy ->
                    if (mode == MODE_NONE) {
                        // Пойманный на лету «уезжающий вниз» мини-плеер остаётся под пальцем.
                        mode = if (offsetY.value > 0.5f || dy >= 0f) MODE_DISMISS else MODE_SHEET
                    }
                    if (mode == MODE_SHEET) {
                        onSheetDrag(dy)
                    } else {
                        scope.launch { offsetY.snapTo((offsetY.value + dy).coerceAtLeast(0f)) }
                    }
                },
                orientation = Orientation.Vertical,
                onDragStarted = {
                    mode = MODE_NONE
                    offsetY.stop()
                },
                onDragStopped = { v ->
                    val wasSheet = mode == MODE_SHEET
                    mode = MODE_NONE
                    if (wasSheet) {
                        onSheetDragStopped(v)
                    } else {
                        val y = offsetY.value
                        if (onDismiss != null && (y > 90f || v > 1400f)) {
                            offsetY.animateTo(160f, tween(110))
                            onDismiss()
                            offsetY.snapTo(0f)
                        } else {
                            offsetY.animateTo(0f, spring(dampingRatio = 0.78f, stiffness = 380f), v)
                        }
                    }
                }
            )
            .pressScale(source)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(interactionSource = source, indication = LocalIndication.current, onClick = onOpen),
        color = Color.Transparent,
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().height(MiniPlayerHeight).padding(start = MiniInnerStart, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Обложка: отсюда стартует и сюда возвращается полёт обложки.
            Box(
                modifier = Modifier
                    .size(MiniCoverSize)
                    .graphicsLayer {
                        // Пока обложка летит между мини-плеером и шторкой, её рисует VantaRoot.
                        alpha = if (coverHidden()) 0f else 1f
                    }
                    .clip(RoundedCornerShape(MiniCoverCorner))
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                if (track.artUri != null) {
                    AsyncImage(
                        model = track.artUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = track.title,
                    transitionSpec = {
                        (fadeIn(tween(220)) + slideInVertically(tween(220)) { it / 3 })
                            .togetherWith(fadeOut(tween(140)) + slideOutVertically(tween(140)) { -it / 4 })
                    },
                    label = "miniTrackTitle"
                ) { title ->
                    Text(title, style = MaterialTheme.typography.titleMedium, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                AnimatedContent(
                    targetState = track.artist ?: "Unknown artist",
                    transitionSpec = {
                        (fadeIn(tween(240, delayMillis = 25)) + slideInVertically(tween(240, delayMillis = 25)) { it / 3 })
                            .togetherWith(fadeOut(tween(130)) + slideOutVertically(tween(130)) { -it / 4 })
                    },
                    label = "miniTrackArtist"
                ) { artist ->
                    Text(artist, style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
            }
            IconButton(
                onClick = onToggle,
                modifier = Modifier.size(44.dp).background(MaterialTheme.colorScheme.primary, CircleShape)
            ) {
                AnimatedContent(
                    targetState = isPlaying,
                    transitionSpec = {
                        (fadeIn(tween(180)) + scaleIn(tween(180), initialScale = 0.82f))
                            .togetherWith(fadeOut(tween(110)) + scaleOut(tween(110), targetScale = 0.88f))
                    },
                    label = "miniPlayPause"
                ) { playing ->
                    Icon(
                        if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play or pause",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = onNext,
                modifier = Modifier.size(44.dp).background(Color.White.copy(alpha = 0.12f), CircleShape)
            ) {
                Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White)
            }
        }
    }
}
