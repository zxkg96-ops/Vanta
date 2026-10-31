package com.vanta.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import com.vanta.app.ui.motion.VantaMotion

/** Subtle press feedback: quick response, restrained scale, no distracting bounce. */
fun Modifier.pressScale(
    source: InteractionSource,
    pressedScale: Float = 0.97f
): Modifier = composed {
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = VantaMotion.TouchSpring,
        label = "vantaPressScale"
    )
    graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}
