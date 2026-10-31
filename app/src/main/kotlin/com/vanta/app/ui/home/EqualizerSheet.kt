package com.vanta.app.ui.home

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vanta.core.EqualizerState
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerSheet(
    eq: EqualizerState,
    onDismiss: () -> Unit,
    onEnabled: (Boolean) -> Unit,
    onBand: (Int, Float) -> Unit,
    onBass: (Int) -> Unit,
    onPreset: (String) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Эквалайзер", style = MaterialTheme.typography.titleLarge, color = Color.White)
                if (eq.supported) Switch(checked = eq.enabled, onCheckedChange = onEnabled)
            }

            if (!eq.supported) {
                Text(
                    "Эквалайзер недоступен на этом устройстве",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 16.dp)
                )
                return@Column
            }

            if (eq.presets.isNotEmpty()) {
                Row(
                    modifier = Modifier.padding(top = 16.dp).horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    eq.presets.forEach { name ->
                        FilterChip(
                            selected = eq.currentPreset == name,
                            onClick = { onPreset(name) },
                            enabled = eq.enabled,
                            label = { Text(name) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            eq.bandsDb.forEachIndexed { i, db ->
                val hz = eq.bandFreqsHz.getOrNull(i) ?: 0
                val label = if (hz >= 1000) String.format(Locale.US, "%.1f кГц", hz / 1000f) else "$hz Гц"
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(label, modifier = Modifier.width(72.dp), style = MaterialTheme.typography.labelSmall)
                    Slider(
                        value = db.coerceIn(eq.minDb, eq.maxDb),
                        onValueChange = { onBand(i, it) },
                        valueRange = eq.minDb..eq.maxDb,
                        enabled = eq.enabled,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        String.format(Locale.US, "%+.0f", db),
                        modifier = Modifier.width(32.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            if (eq.bassBoostSupported) {
                Spacer(Modifier.height(8.dp))
                Text("Усиление баса", color = Color.White)
                Slider(
                    value = eq.bassBoost.toFloat(),
                    onValueChange = { onBass(it.toInt()) },
                    valueRange = 0f..100f,
                    enabled = eq.enabled
                )
            }
        }
    }
}
