package com.vanta.app.ui.player

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.vanta.app.ui.components.instantTap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vanta.core.Track

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueSheet(
    queue: List<Track>,
    current: Int,
    onDismiss: () -> Unit,
    onPlay: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    onMove: (Int, Int) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            "Очередь · ${queue.size}",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )
        LazyColumn(Modifier.fillMaxWidth().heightIn(max = 480.dp)) {
            itemsIndexed(queue) { i, t ->
                val rowSource = remember { MutableInteractionSource() }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Старт на касании только по названию: кнопки справа остаются обычными, иначе
                    // удержание кнопки дольше окна запустило бы и сам трек. Повторное касание
                    // уже играющего трека ничего не делает.
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .indication(rowSource, LocalIndication.current)
                            .instantTap(rowSource) { if (i != current) onPlay(i) },
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            t.title,
                            color = if (i == current) MaterialTheme.colorScheme.primary else Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = { onMove(i, i - 1) }, enabled = i > 0) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Выше")
                    }
                    IconButton(onClick = { onMove(i, i + 1) }, enabled = i < queue.lastIndex) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Ниже")
                    }
                    IconButton(onClick = { onRemove(i) }) {
                        Icon(Icons.Default.Close, contentDescription = "Убрать")
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}
