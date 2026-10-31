package com.vanta.app.ui.motion

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

/** Shared motion tokens for consistent, low-jank UI transitions. */
object VantaMotion {
    const val FAST_MS = 120
    const val NORMAL_MS = 280
    const val EMPHASIZED_MS = 420

    val StandardEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    val DecelerateEasing = CubicBezierEasing(0.16f, 1.0f, 0.3f, 1.0f)

    val TouchSpring = spring<Float>(
        dampingRatio = 0.88f,
        stiffness = 520f
    )

    val ContentSpring = spring<Float>(
        dampingRatio = 0.90f,
        stiffness = 340f
    )

    val GentleSpring = spring<Float>(
        dampingRatio = 0.92f,
        stiffness = 260f
    )

    fun fastTween() = tween<Float>(FAST_MS, easing = StandardEasing)
    fun normalTween() = tween<Float>(NORMAL_MS, easing = StandardEasing)
    fun emphasizedTween() = tween<Float>(EMPHASIZED_MS, easing = DecelerateEasing)
}
