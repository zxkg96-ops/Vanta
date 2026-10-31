package com.vanta.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape

/** Простая плоская подложка заданной формы. */
fun Modifier.flatFill(shape: Shape, color: Color): Modifier = this.clip(shape).background(color)
