package com.vanta.app.ui.components

import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Островок с вкладками и «стеклянной» таблеткой, которая перетекает к выбранной вкладке. */
@Composable
fun GlassTabBar(
    titles: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val plate = RoundedCornerShape(28.dp)
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .clip(plate)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp)
    ) {
        val tabW = maxWidth / titles.size

        // Keep the indicator's measured size constant. Only its render-layer position
        // changes, avoiding repeated width/layout work on every animation frame.
        val indicatorOffset by animateFloatAsState(
            targetValue = tabW.value * selected,
            animationSpec = spring(dampingRatio = 0.76f, stiffness = 380f),
            label = "tabIndicatorOffset"
        )

        Box(
            Modifier
                .width(tabW)
                .height(40.dp)
                .graphicsLayer { translationX = indicatorOffset * density }
                .flatFill(RoundedCornerShape(24.dp), Color.White.copy(alpha = 0.16f))
        )

        Row(Modifier.fillMaxWidth().height(40.dp)) {
            titles.forEachIndexed { i, title ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onSelect(i) },
                    contentAlignment = Alignment.Center
                ) {
                    val tint by animateColorAsState(
                        targetValue = if (selected == i) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        animationSpec = tween(200),
                        label = "tabTint"
                    )
                    Text(
                        text = title,
                        color = tint,
                        fontSize = 12.sp,
                        // Constant weight: switching Bold/Normal re-measures the text and makes the label jump.
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}
