package com.vanta.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/** Имитация «жидкого стекла»: полупрозрачный градиент, светлая кромка и блик сверху. */
fun Modifier.glass(shape: Shape, strength: Float = 1f): Modifier = this
    .clip(shape)
    .background(
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.24f * strength),
                Color.White.copy(alpha = 0.07f * strength)
            )
        )
    )
    .border(
        1.dp,
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.60f * strength),
                Color.White.copy(alpha = 0.08f * strength)
            )
        ),
        shape
    )
