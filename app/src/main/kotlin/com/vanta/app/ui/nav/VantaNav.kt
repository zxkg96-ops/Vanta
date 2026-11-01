package com.vanta.app.ui.nav

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vanta.app.ui.home.HomeScreen
import com.vanta.app.ui.home.HomeViewModel
import com.vanta.app.ui.motion.VantaMotion
import com.vanta.app.ui.player.NowPlayingScreen
import com.vanta.core.PlayerController
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/**
 * Now Playing is a finger-driven sheet: [progress] 0 = closed, 1 = fully open.
 * The same value drives the sheet position, the scale/dim of Home behind it,
 * drag-to-dismiss, predictive back and the release spring (which keeps the
 * velocity of the gesture), so every motion can be interrupted mid-flight.
 */
@Composable
fun VantaRoot(
    player: PlayerController,
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val progress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var open by remember { mutableStateOf(false) }
    var heightPx by remember { mutableFloatStateOf(1f) }
    val visible by remember { derivedStateOf { progress.value > 0.001f } }

    fun settle(target: Boolean, velocity: Float = 0f) {
        open = target
        scope.launch {
            progress.animateTo(
                targetValue = if (target) 1f else 0f,
                animationSpec = if (target) VantaMotion.SheetOpenSpring else VantaMotion.SheetCloseSpring,
                initialVelocity = velocity
            )
        }
    }

    val dragState = androidx.compose.foundation.gestures.rememberDraggableState { dy ->
        scope.launch { progress.snapTo((progress.value - dy / heightPx).coerceIn(0f, 1f)) }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .onSizeChanged { heightPx = it.height.toFloat().coerceAtLeast(1f) }
    ) {
        // Home stays composed underneath: it keeps scroll position and shrinks like an iOS card.
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val p = progress.value.coerceIn(0f, 1f)
                    val s = 1f - 0.07f * p
                    scaleX = s
                    scaleY = s
                    shape = RoundedCornerShape(14.dp)
                    clip = p > 0f
                }
        ) {
            HomeScreen(
                viewModel = homeViewModel,
                onOpenPlayer = { settle(true) }
            )
        }

        if (visible) {
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = 0.4f * progress.value.coerceIn(0f, 1f) }
                    .background(Color.Black)
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val p = progress.value.coerceIn(0f, 1f)
                        translationY = (1f - p) * size.height
                        shape = RoundedCornerShape(topStart = (28f * (1f - p)).dp, topEnd = (28f * (1f - p)).dp)
                        clip = true
                    }
                    .background(MaterialTheme.colorScheme.background)
                    .draggable(
                        state = dragState,
                        orientation = Orientation.Vertical,
                        onDragStopped = { v ->
                            // v is px/s downward; progress velocity is the opposite sign, normalised by height.
                            val closing = v > 900f || (progress.value < 0.55f && v > -900f)
                            settle(!closing, -v / heightPx)
                        }
                    )
            ) {
                NowPlayingScreen(
                    player = player,
                    onClose = { settle(false) }
                )
            }
        }

        // Registered after Home so it wins over Home's own back handlers while the player is open.
        PredictiveBackHandler(enabled = open) { events ->
            try {
                events.collect { e -> progress.snapTo(1f - 0.3f * e.progress) }
                settle(false)
            } catch (c: CancellationException) {
                settle(true)
            }
        }
    }
}
