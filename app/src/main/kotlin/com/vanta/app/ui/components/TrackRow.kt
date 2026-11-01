package com.vanta.app.ui.components

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.vanta.core.Track

/**
 * Строка трека. [onClick] срабатывает на касании (см. [instantTap]), а не на отпускании:
 * палец лежит [INSTANT_TAP_WINDOW_MS] без движения — трек стартует; начался скролл — не стартует.
 * [onPressStart] вызывается сразу при касании (прогрев файла).
 */
@Composable
fun TrackRow(
    track: Track,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    onPressStart: (() -> Unit)? = null
) {
    val source = remember { MutableInteractionSource() }
    val subtitle = remember(track.artist, track.album) {
        listOfNotNull(track.artist, track.album).joinToString(" • ").ifBlank { "Unknown artist" }
    }
    Row(
        modifier = modifier.fillMaxWidth().pressScale(source).clip(RoundedCornerShape(14.dp))
            .indication(source, LocalIndication.current)
            .instantTap(
                interactionSource = source,
                onPressStart = onPressStart,
                onLongClick = onLongClick,
                onTap = onClick
            )
            .padding(horizontal = 4.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            if (track.artUri != null) {
                AsyncImage(model = track.artUri, contentDescription = null, contentScale = ContentScale.Crop)
            } else {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(track.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        trailing?.invoke()
    }
}
