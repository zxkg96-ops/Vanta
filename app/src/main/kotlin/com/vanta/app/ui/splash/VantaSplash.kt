package com.vanta.app.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.vanta.app.ui.motion.VantaMotion
import com.vanta.app.ui.theme.VantaBlack
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

private const val DANCE_MS = 850
private const val SETTLE_MS = 600
private const val HOLD_MS = 120L

// Geometry of the launcher icon (ic_launcher_foreground.xml, 108x108 viewport).
private const val STROKE = 5.04f
private const val CENTER_X = 54.05f
private val BAR_X = floatArrayOf(34.38f, 44.07f, 54.05f, 63.93f, 73.62f)
private val BAR_CENTER_Y = floatArrayOf(53.715f, 54.045f, 54.045f, 55.185f, 54.38f)
// Total visible height of every bar (line length + round caps).
private val BAR_HEIGHT = floatArrayOf(14.63f, 27.65f, 44.37f, 22.33f, 9.70f)

// Per-bar "equalizer" motion while the app is starting.
private val FREQ = floatArrayOf(2.1f, 2.9f, 2.4f, 3.3f, 2.6f)
private val PHASE = floatArrayOf(0.0f, 1.7f, 3.1f, 4.4f, 5.6f)

/**
 * Launch animation: the five bars of the app icon dance like an equalizer,
 * then spring into the exact shape of the icon and the screen fades into the app.
 */
@Composable
fun VantaSplash(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clockMs = DANCE_MS + SETTLE_MS + 500
    val clock = remember { Animatable(0f) }
    val settle = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }
    val done by rememberUpdatedState(onFinished)

    LaunchedEffect(Unit) {
        coroutineScope {
            launch { clock.animateTo(1f, tween(clockMs, easing = LinearEasing)) }
            launch {
                delay(DANCE_MS.toLong())
                settle.animateTo(1f, spring(dampingRatio = 0.62f, stiffness = 170f))
            }
        }
        delay(HOLD_MS)
        alpha.animateTo(0f, tween(220, easing = VantaMotion.StandardEasing))
        done()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { this.alpha = alpha.value }
            .background(VantaBlack)
            // Swallow touches so nothing underneath reacts during the animation.
            .pointerInput(Unit) { detectTapGestures { } },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(220.dp)) {
            val unit = 3.4.dp.toPx()
            val cx = size.width / 2f
            val cy = size.height / 2f
            val seconds = clockMs / 1000f
            val s = settle.value
            val t = clock.value * seconds

            for (i in BAR_X.indices) {
                val a = 2f * PI.toFloat() * FREQ[i] * t + PHASE[i]
                val wave = 0.65f * sin(a) + 0.35f * sin(a * 1.7f + PHASE[i])
                val dancing = 8f + 36f * ((wave + 1f) / 2f)          // 8..44 units
                val height = (dancing * (1f - s) + BAR_HEIGHT[i] * s).coerceAtLeast(STROKE)
                val centerY = 54.05f * (1f - s) + BAR_CENTER_Y[i] * s
                val half = (height - STROKE) / 2f
                val x = cx + (BAR_X[i] - CENTER_X) * unit
                val y = cy + (centerY - 54.05f) * unit
                drawLine(
                    color = Color.White,
                    start = Offset(x, y - half * unit),
                    end = Offset(x, y + half * unit),
                    strokeWidth = STROKE * unit,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
