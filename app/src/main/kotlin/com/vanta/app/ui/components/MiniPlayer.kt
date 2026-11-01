package com.vanta.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vanta.core.PlayerState

@Composable
fun MiniPlayer(
    state: PlayerState,
    onOpen: () -> Unit,
    onToggle: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null
) {
    val track = state.queue.getOrNull(state.currentIndex) ?: return
    val source = remember { MutableInteractionSource() }
    val offsetY = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    Surface(
        modifier = modifier.fillMaxWidth()
            .graphicsLayer {
                translationY = offsetY.value
                alpha = (1f - offsetY.value.coerceAtLeast(0f) / 400f).coerceIn(0.4f, 1f)
            }
            .draggable(
                state = rememberDraggableState { dy ->
                    scope.launch {
                        val cur = offsetY.value
                        // Rubber band when pulled upwards, free when pulled down to dismiss.
                        offsetY.snapTo(cur + if (cur + dy < 0f) dy * 0.35f else dy)
                    }
                },
                orientation = Orientation.Vertical,
                onDragStopped = { v ->
                    val y = offsetY.value
                    when {
                        onDismiss != null && (y > 90f || v > 1400f) -> {
                            offsetY.animateTo(160f, tween(110))
                            onDismiss()
                            offsetY.snapTo(0f)
                        }
                        y < -36f || v < -1100f -> {
                            onOpen()
                            offsetY.animateTo(0f, spring(dampingRatio = 0.8f, stiffness = 400f), v)
                        }
                        else -> offsetY.animateTo(0f, spring(dampingRatio = 0.78f, stiffness = 380f), v)
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
        Row(Modifier.padding(start = 16.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically) {
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
                    targetState = state.isPlaying,
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
