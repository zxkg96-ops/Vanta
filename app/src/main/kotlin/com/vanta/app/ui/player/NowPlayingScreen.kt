package com.vanta.app.ui.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp as lerpColor
import com.vanta.app.ui.components.flatFill
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode as AnimRepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures as detectHDrag
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.vanta.core.PlayerController
import com.vanta.core.RepeatMode
import java.util.Locale
import kotlin.math.PI
import kotlin.math.sin
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    player: PlayerController,
    onClose: () -> Unit
) {
    val state by player.state.collectAsStateWithLifecycle()
    val track = state.queue.getOrNull(state.currentIndex)
    var showQueue by remember { mutableStateOf(false) }
    var showTimer by remember { mutableStateOf(false) }
    val artColors = rememberArtColors(
        track?.artUri,
        MaterialTheme.colorScheme.surface,
        MaterialTheme.colorScheme.primary
    )
    val artColor = artColors.dark
    val accent = artColors.accent


    val artScale by animateFloatAsState(
        targetValue = if (state.isPlaying) 1f else 0.9f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "artScale"
    )
    val activeColor = MaterialTheme.colorScheme.primary
    val idleColor = MaterialTheme.colorScheme.onSurfaceVariant
    val shuffleTint by animateColorAsState(if (state.shuffle) activeColor else idleColor, tween(250), label = "shuffleTint")
    val repeatTint by animateColorAsState(if (state.repeat != RepeatMode.OFF) activeColor else idleColor, tween(250), label = "repeatTint")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        artColor,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        TopAppBar(
            title = {
                Text(
                    "Now Playing",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            navigationIcon = {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Close")
                }
            },
            actions = {
                IconButton(onClick = { showTimer = true }) {
                    Icon(
                        Icons.Default.Bedtime,
                        contentDescription = "Таймер сна",
                        tint = if (state.sleepTimerLeftMs != null) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { showQueue = true }) {
                    Icon(Icons.Default.QueueMusic, contentDescription = "Очередь")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(12.dp))

            AnimatedContent(
                targetState = track,
                modifier = Modifier.pointerInput(Unit) {
                    var dx = 0f
                    detectHDrag(
                        onDragStart = { dx = 0f },
                        onDragEnd = {
                            if (dx > 140f) player.prev() else if (dx < -140f) player.next()
                        },
                        onHorizontalDrag = { change, delta ->
                            change.consume()
                            dx += delta
                        }
                    )
                },
                contentKey = { it?.id },
                transitionSpec = {
                    ContentTransform(
                        targetContentEnter = fadeIn(tween(320)) + scaleIn(tween(320), initialScale = 0.94f),
                        initialContentExit = fadeOut(tween(200)),
                        sizeTransform = SizeTransform(clip = false)
                    )
                },
                label = "trackChange"
            ) { t ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Artwork
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.88f)
                            .aspectRatio(1f)
                            .graphicsLayer {
                                scaleX = artScale
                                scaleY = artScale
                            }
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        if (t?.artUri != null) {
                            AsyncImage(
                                model = t.artUri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                Icons.Default.MusicNote,
                                contentDescription = null,
                                modifier = Modifier.size(96.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.height(36.dp))

                    Text(
                        text = t?.title ?: "Nothing playing",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = t?.artist ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            var sliderPosition by remember(state.positionMs, state.durationMs) {
                mutableFloatStateOf(
                    if (state.durationMs > 0) state.positionMs.toFloat() / state.durationMs else 0f
                )
            }
            var isSeeking by remember { mutableFloatStateOf(-1f) }
            val shownProgress = if (isSeeking >= 0f) isSeeking else sliderPosition

            // Эквалайзер (имитация)
            EqualizerBars(
                isPlaying = state.isPlaying,
                progress = shownProgress,
                modifier = Modifier.fillMaxWidth().height(40.dp)
            )

            Spacer(Modifier.height(6.dp))

            // Seek bar
            SeekBar(
                progress = shownProgress,
                onDrag = { isSeeking = it },
                onSeek = { f ->
                    player.seekTo((f * state.durationMs).toLong())
                    isSeeking = -1f
                },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(formatMs(state.positionMs), style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(formatMs(state.durationMs), style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.height(20.dp))

            // Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .flatFill(RoundedCornerShape(50), MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { player.setShuffle(!state.shuffle) }) {
                    Icon(
                        Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = shuffleTint
                    )
                }
                IconButton(onClick = { player.prev() }, modifier = Modifier.size(56.dp)) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = Color.White, modifier = Modifier.size(36.dp))
                }
                IconButton(
                    onClick = { player.toggle() },
                    modifier = Modifier
                        .size(72.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                ) {
                    AnimatedContent(
                        targetState = state.isPlaying,
                        transitionSpec = {
                            (fadeIn(tween(180)) + scaleIn(tween(180), initialScale = 0.5f))
                                .togetherWith(fadeOut(tween(120)) + scaleOut(tween(120), targetScale = 0.5f))
                        },
                        label = "playPause"
                    ) { playing ->
                        Icon(
                            if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                IconButton(onClick = { player.next() }, modifier = Modifier.size(56.dp)) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(36.dp))
                }
                IconButton(onClick = {
                    val next = when (state.repeat) {
                        RepeatMode.OFF -> RepeatMode.ALL
                        RepeatMode.ALL -> RepeatMode.ONE
                        RepeatMode.ONE -> RepeatMode.OFF
                    }
                    player.setRepeat(next)
                }) {
                    Icon(
                        if (state.repeat == RepeatMode.ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                        contentDescription = "Repeat",
                        tint = repeatTint
                    )
                }
            }
        }
    }

    if (showQueue) {
        QueueSheet(
            queue = state.queue,
            current = state.currentIndex,
            onDismiss = { showQueue = false },
            onPlay = { player.playIndex(it) },
            onRemove = { player.removeFromQueue(it) },
            onMove = { from, to -> player.moveInQueue(from, to) }
        )
    }

    if (showTimer) {
        AlertDialog(
            onDismissRequest = { showTimer = false },
            confirmButton = { TextButton(onClick = { showTimer = false }) { Text("Закрыть") } },
            title = { Text("Таймер сна") },
            text = {
                Column {
                    state.sleepTimerLeftMs?.let {
                        Text("Осталось ${formatMs(it)}", color = MaterialTheme.colorScheme.primary)
                    }
                    listOf(15, 30, 45, 60, 90).forEach { m ->
                        TextButton(onClick = { player.setSleepTimer(m); showTimer = false }) { Text("$m мин") }
                    }
                    if (state.sleepTimerLeftMs != null) {
                        TextButton(onClick = { player.setSleepTimer(null); showTimer = false }) { Text("Выключить таймер") }
                    }
                }
            }
        )
    }
}

private fun formatMs(ms: Long): String {
    if (ms <= 0) return "0:00"
    val minutes = TimeUnit.MILLISECONDS.toMinutes(ms)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
    return String.format(Locale.US, "%d:%02d", minutes, seconds)
}


/** Тонкая цельная дорожка без ползунка: тап и перетаскивание. */
@Composable
private fun SeekBar(
    progress: Float,
    onDrag: (Float) -> Unit,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var widthPx by remember { mutableFloatStateOf(1f) }
    val currentOnDrag by rememberUpdatedState(onDrag)
    val currentOnSeek by rememberUpdatedState(onSeek)

    Box(
        modifier = modifier
            .height(28.dp)
            .onSizeChanged { widthPx = it.width.toFloat().coerceAtLeast(1f) }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    currentOnSeek((offset.x / widthPx).coerceIn(0f, 1f))
                }
            }
            .pointerInput(Unit) {
                var last = 0f
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        last = (offset.x / widthPx).coerceIn(0f, 1f)
                        currentOnDrag(last)
                    },
                    onDragEnd = { currentOnSeek(last) },
                    onDragCancel = { currentOnSeek(last) },
                    onHorizontalDrag = { change, _ ->
                        change.consume()
                        last = (change.position.x / widthPx).coerceIn(0f, 1f)
                        currentOnDrag(last)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

private const val EQ_BARS = 36

private class EqBar(val k1: Int, val o1: Float, val k2: Int, val o2: Float, val envelope: Float)

/** Имитация эквалайзера: столбики прыгают, пока играет музыка, и опускаются на паузе. */
@Composable
private fun EqualizerBars(
    isPlaying: Boolean,
    progress: Float,
    modifier: Modifier = Modifier
) {
    val bars = remember {
        val rnd = java.util.Random(7)
        List(EQ_BARS) { i ->
            EqBar(
                k1 = 1 + rnd.nextInt(3),
                o1 = rnd.nextFloat() * 2f * PI.toFloat(),
                k2 = 2 + rnd.nextInt(3),
                o2 = rnd.nextFloat() * 2f * PI.toFloat(),
                envelope = 0.6f + 0.4f * sin(PI.toFloat() * (i + 0.5f) / EQ_BARS)
            )
        }
    }
    val transition = rememberInfiniteTransition(label = "eq")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing), AnimRepeatMode.Restart),
        label = "eqPhase"
    )
    val level by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0f,
        animationSpec = tween(450),
        label = "eqLevel"
    )
    val active = MaterialTheme.colorScheme.primary
    val idle = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
    val twoPi = 2f * PI.toFloat()

    Canvas(modifier) {
        val barW = size.width / (EQ_BARS * 1.7f)
        val gap = (size.width - barW * EQ_BARS) / (EQ_BARS - 1)
        val minH = 4.dp.toPx()
        bars.forEachIndexed { i, bar ->
            val s1 = sin(twoPi * bar.k1 * phase + bar.o1)
            val s2 = sin(twoPi * bar.k2 * phase + bar.o2)
            val v = ((0.5f + 0.25f * s1 + 0.25f * s2) * bar.envelope).coerceIn(0f, 1f)
            val h = minH + (size.height - minH) * v * (0.18f + 0.82f * level)
            val x = i * (barW + gap)
            val played = (i + 0.5f) / EQ_BARS <= progress
            drawRoundRect(
                color = if (played) active else idle,
                topLeft = Offset(x, size.height - h),
                size = Size(barW, h),
                cornerRadius = CornerRadius(barW / 2f, barW / 2f)
            )
        }
    }
}
