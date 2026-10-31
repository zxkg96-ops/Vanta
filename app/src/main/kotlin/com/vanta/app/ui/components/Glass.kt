package com.vanta.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Имитация «жидкого стекла» в духе iOS:
 * полупрозрачная заливка, блик сверху, мягкое свечение снизу
 * и тонкая кромка, подсвеченная по диагонали (светлее в углах, темнее по бокам).
 */
fun Modifier.glass(shape: Shape, strength: Float = 1f, highlight: Float = 0.20f): Modifier = this
    .clip(shape)
    .background(
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.20f * strength),
                Color.White.copy(alpha = 0.06f * strength)
            )
        )
    )
    .drawBehind {
        // Верхний блик
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.White.copy(alpha = highlight * strength), Color.Transparent),
                startY = 0f,
                endY = size.height * 0.5f
            ),
            size = Size(size.width, size.height * 0.5f)
        )
        // Мягкое свечение у нижней кромки
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.09f * strength)),
                startY = size.height * 0.55f,
                endY = size.height
            ),
            topLeft = androidx.compose.ui.geometry.Offset(0f, size.height * 0.55f),
            size = Size(size.width, size.height * 0.45f)
        )
    }
    .border(
        1.dp,
        Brush.linearGradient(
            listOf(
                Color.White.copy(alpha = 0.75f * strength),
                Color.White.copy(alpha = 0.08f * strength),
                Color.White.copy(alpha = 0.40f * strength)
            )
        ),
        shape
    )
