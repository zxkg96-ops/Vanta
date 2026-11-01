package com.vanta.app.ui.motion

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

/** Shared motion tokens tuned for responsive, restrained, iOS-like motion. */
object VantaMotion {
    const val INSTANT_MS = 90
    const val FAST_MS = 180
    const val NORMAL_MS = 300
    const val EMPHASIZED_MS = 460
    const val EXIT_MS = 190

    // Smooth acceleration/deceleration without the abrupt stop of a linear curve.
    val StandardEasing = CubicBezierEasing(0.22f, 0.0f, 0.0f, 1.0f)
    val DecelerateEasing = CubicBezierEasing(0.16f, 1.0f, 0.3f, 1.0f)
    val EnterEasing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)

    // High damping keeps controls precise. Lower stiffness gives larger surfaces a softer settle.
    val TouchSpring = spring<Float>(
        dampingRatio = 0.86f,
        stiffness = 480f
    )

    val ContentSpring = spring<Float>(
        dampingRatio = 0.88f,
        stiffness = 300f
    )

    val ArtworkSpring = spring<Float>(
        dampingRatio = 0.84f,
        stiffness = 220f
    )

    val GentleSpring = spring<Float>(
        dampingRatio = 0.92f,
        stiffness = 260f
    )

    fun fastTween() = tween<Float>(FAST_MS, easing = StandardEasing)
    fun normalTween() = tween<Float>(NORMAL_MS, easing = StandardEasing)
    fun emphasizedTween() = tween<Float>(EMPHASIZED_MS, easing = DecelerateEasing)
}
