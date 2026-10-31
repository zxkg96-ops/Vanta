package com.vanta.app.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun VantaSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Slider(
        value = value.coerceIn(valueRange),
        onValueChange = onValueChange,
        valueRange = valueRange,
        enabled = enabled,
        modifier = modifier,
        colors = androidx.compose.material3.SliderDefaults.colors(
            thumbColor = MaterialTheme.colorScheme.primary,
            activeTrackColor = MaterialTheme.colorScheme.primary,
            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )
}
