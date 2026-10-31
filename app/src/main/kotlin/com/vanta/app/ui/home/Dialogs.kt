package com.vanta.app.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vanta.app.data.UserPlaylist
import com.vanta.core.Track

@Composable
private fun ActionRow(label: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(16.dp))
        Text(label, color = Color.White)
    }
}

@Composable
fun TrackActionsDialog(
    track: Track,
    isFavorite: Boolean,
    onDismiss: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToPlaylist: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Закрыть") } },
        title = { Text(track.title, maxLines = 2, overflow = TextOverflow.Ellipsis) },
        text = {
            Column {
                ActionRow("Играть следующим", Icons.Default.SkipNext, onPlayNext)
                ActionRow("Добавить в очередь", Icons.Default.Add, onAddToQueue)
                ActionRow(
                    if (isFavorite) "Убрать из избранного" else "В избранное",
                    if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    onToggleFavorite
                )
                ActionRow("Добавить в плейлист…", Icons.Default.Add, onAddToPlaylist)
            }
        }
    )
}

@Composable
fun PickPlaylistDialog(
    playlists: List<UserPlaylist>,
    onPick: (UserPlaylist) -> Unit,
    onNew: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onNew) { Text("Новый плейлист") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
        title = { Text("В плейлист") },
        text = {
            Column {
                if (playlists.isEmpty()) {
                    Text("Плейлистов пока нет", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                playlists.forEach { pl ->
                    TextButton(onClick = { onPick(pl) }) { Text(pl.name, color = Color.White) }
                }
            }
        }
    )
}

@Composable
fun NewPlaylistDialog(onCreate: (String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onCreate(name) }) { Text("Создать") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
        title = { Text("Новый плейлист") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                label = { Text("Название") }
            )
        }
    )
}
