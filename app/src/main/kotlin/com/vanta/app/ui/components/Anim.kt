package com.vanta.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import com.vanta.app.ui.motion.VantaMotion

/** Tactile press response with a small scale change and a spring return. */
fun Modifier.pressScale(
    source: InteractionSource,
    pressedScale: Float = 0.975f
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

/** IconButton with the same tactile press response as the rest of the app. */
@Composable
fun PressIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    pressedScale: Float = 0.86f,
    content: @Composable () -> Unit
) {
    val source = remember { MutableInteractionSource() }
    IconButton(
        onClick = onClick,
        modifier = modifier.pressScale(source, pressedScale),
        interactionSource = source,
        content = content
    )
}
