package com.vanta.app.ui.components

import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vanta.app.ui.motion.VantaMotion

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

        var lastSel by remember { mutableIntStateOf(selected) }
        val goingRight = selected >= lastSel
        SideEffect { lastSel = selected }

        // Передний край бежит быстро, задний отстаёт: таблетка растягивается при переходе
        val fast = spring<Dp>(dampingRatio = 0.86f, stiffness = 460f)
        val slow = spring<Dp>(dampingRatio = 0.90f, stiffness = 340f)
        val left by animateDpAsState(tabW * selected, if (goingRight) slow else fast, label = "pillLeft")
        val right by animateDpAsState(tabW * (selected + 1), if (goingRight) fast else slow, label = "pillRight")

        Box(
            Modifier
                .offset(x = left)
                .width((right - left).coerceAtLeast(0.dp))
                .height(40.dp)
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
                    Text(
                        text = title,
                        color = if (selected == i) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = if (selected == i) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}
