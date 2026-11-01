package com.vanta.app.ui.nav

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.vanta.app.ui.components.MiniCoverCorner
import com.vanta.app.ui.components.MiniCoverSize
import com.vanta.app.ui.components.MiniInnerStart
import com.vanta.app.ui.components.MiniOuterHorizontal
import com.vanta.app.ui.components.MiniOuterVertical
import com.vanta.app.ui.components.MiniPlayer
import com.vanta.app.ui.components.MiniPlayerHeight
import com.vanta.app.ui.home.HomeScreen
import com.vanta.app.ui.home.HomeViewModel
import com.vanta.app.ui.motion.VantaMotion
import com.vanta.app.ui.player.NowPlayingScreen
import com.vanta.core.PlayerController
import com.vanta.core.Track
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/** Во сколько раз палец на мини-плеере «быстрее» тянет шторку: мини-плеер стоит у низа, шторке лететь через весь экран. */
private const val MINI_DRAG_GAIN = 1.15f

/** Прогресс, начиная с которого шторка считается полностью открытой и своя обложка плеера заменяет летящую. */
private const val FLIGHT_END = 0.999f

/** Доля ширины карусели, которую занимает обложка в NowPlayingScreen (fillMaxWidth(0.88f)). */
private const val COVER_WIDTH_FRACTION = 0.88f

/**
 * Now Playing is a finger-driven sheet: [progress] 0 = closed, 1 = fully open.
 * The same value drives the sheet position, the scale/dim of Home behind it,
 * drag-to-dismiss, predictive back, the release spring (which keeps the
 * velocity of the gesture) and the flight of the cover art from the mini player
 * to the full screen, so every motion follows the finger and can be interrupted mid-flight.
 *
 * Мини-плеер живёт здесь, в одном слое со шторкой: так обложка может лететь между ними по общему progress.
 * Свайп вверх по мини-плееру ведёт ту же шторку за пальцем.
 */
@Composable
fun VantaRoot(
    player: PlayerController,
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val progress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var open by remember { mutableStateOf(false) }
    var heightPx by remember { mutableFloatStateOf(1f) }
    var widthPx by remember { mutableFloatStateOf(1f) }
    val visible by remember { derivedStateOf { progress.value > 0.001f } }
    val flying by remember { derivedStateOf { progress.value > 0.001f && progress.value < FLIGHT_END } }
    val density = LocalDensity.current

    // Нам нужен только текущий трек и признак воспроизведения, а не позиция, которая меняется 4 раза в секунду.
    val track by remember(player) {
        player.state.map { it.queue.getOrNull(it.currentIndex) }.distinctUntilChanged()
    }.collectAsStateWithLifecycle(initialValue = null)
    val isPlaying by remember(player) {
        player.state.map { it.isPlaying }.distinctUntilChanged()
    }.collectAsStateWithLifecycle(initialValue = false)

    // Пока мини-плеер уезжает после закрытия плеера, показываем последний трек, а не пустоту.
    var lastTrack by remember { mutableStateOf<Track?>(null) }
    SideEffect {
        val t = track
        if (t != null && lastTrack != t) lastTrack = t
    }
    val miniTrack = track ?: lastTrack

    // Границы карусели Now Playing в координатах экрана плеера (не зависят от положения шторки).
    val carouselBounds = remember { mutableStateOf<Rect?>(null) }

    fun settle(target: Boolean, velocity: Float = 0f) {
        open = target
        scope.launch {
            progress.animateTo(
                targetValue = if (target) 1f else 0f,
                animationSpec = if (target) VantaMotion.SheetOpenSpring else VantaMotion.SheetCloseSpring,
                initialVelocity = velocity
            )
        }
    }

    val dragState = rememberDraggableState { dy ->
        scope.launch { progress.snapTo((progress.value - dy / heightPx).coerceIn(0f, 1f)) }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .onSizeChanged {
                heightPx = it.height.toFloat().coerceAtLeast(1f)
                widthPx = it.width.toFloat().coerceAtLeast(1f)
            }
    ) {
        // Home stays composed underneath: it keeps scroll position and shrinks like an iOS card.
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val p = progress.value.coerceIn(0f, 1f)
                    val s = 1f - 0.07f * p
                    scaleX = s
                    scaleY = s
                    shape = RoundedCornerShape(14.dp)
                    clip = p > 0f
                }
        ) {
            HomeScreen(viewModel = homeViewModel)
        }

        // Мини-плеер: над Home, под шторкой. Не масштабируется вместе с Home.
        AnimatedVisibility(
            visible = track != null,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(spring(dampingRatio = 0.85f, stiffness = 400f)) { it } +
                fadeIn(tween(180, easing = VantaMotion.EnterEasing)),
            exit = slideOutVertically(spring(dampingRatio = 1f, stiffness = 500f)) { it } +
                fadeOut(tween(140, easing = VantaMotion.StandardEasing))
        ) {
            val t = miniTrack
            if (t != null) {
                MiniPlayer(
                    track = t,
                    isPlaying = isPlaying,
                    onOpen = { settle(true) },
                    onToggle = homeViewModel::togglePlayback,
                    onNext = homeViewModel::next,
                    onSheetDrag = { dy ->
                        scope.launch {
                            progress.snapTo((progress.value - dy / heightPx * MINI_DRAG_GAIN).coerceIn(0f, 1f))
                        }
                    },
                    onSheetDragStopped = { v ->
                        // v: px/s вниз. Шторка открыта меньше чем на треть и палец не бросил вверх: возвращаем.
                        val closing = v > 900f || (progress.value < 0.3f && v > -900f)
                        settle(!closing, -v / heightPx * MINI_DRAG_GAIN)
                    },
                    coverHidden = { progress.value > 0.001f },
                    onDismiss = homeViewModel::closePlayer,
                    modifier = Modifier.padding(horizontal = MiniOuterHorizontal, vertical = MiniOuterVertical)
                )
            }
        }

        if (visible) {
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = 0.4f * progress.value.coerceIn(0f, 1f) }
                    .background(Color.Black)
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val p = progress.value.coerceIn(0f, 1f)
                        translationY = (1f - p) * size.height
                        shape = RoundedCornerShape(topStart = (28f * (1f - p)).dp, topEnd = (28f * (1f - p)).dp)
                        clip = true
                    }
                    .background(MaterialTheme.colorScheme.background)
                    .draggable(
                        state = dragState,
                        orientation = Orientation.Vertical,
                        onDragStopped = { v ->
                            // v is px/s downward; progress velocity is the opposite sign, normalised by height.
                            val closing = v > 900f || (progress.value < 0.55f && v > -900f)
                            settle(!closing, -v / heightPx)
                        }
                    )
            ) {
                NowPlayingScreen(
                    player = player,
                    onClose = { settle(false) },
                    // Пока обложка в полёте, настоящая обложка плеера скрыта: вместо неё рисуется летящая копия.
                    coverHidden = { progress.value < FLIGHT_END },
                    onCarouselBounds = { carouselBounds.value = it }
                )
            }
        }

        // Летящая обложка: рисуется поверх всего, пока шторка между «закрыта» и «открыта».
        if (flying) {
            val t = miniTrack
            if (t != null) {
                val miniLeft = with(density) { (MiniOuterHorizontal + MiniInnerStart).toPx() }
                val miniSide = with(density) { MiniCoverSize.toPx() }
                val miniTop = heightPx - with(density) {
                    (MiniOuterVertical + MiniPlayerHeight - (MiniPlayerHeight - MiniCoverSize) / 2).toPx()
                }
                val mini = Rect(miniLeft, miniTop, miniLeft + miniSide, miniTop + miniSide)

                val bounds = carouselBounds.value
                val base = if (bounds != null) {
                    // Обложка стоит по центру карусели, у её верхнего края.
                    val side = bounds.width * COVER_WIDTH_FRACTION
                    val left = bounds.left + (bounds.width - side) / 2f
                    Rect(left, bounds.top, left + side, bounds.top + side)
                } else {
                    // Плеер ещё ни разу не открывался: оцениваем по вёрстке (отступы 28 dp, шапка 64 dp, зазор 12 dp).
                    val side = (widthPx - with(density) { 56.dp.toPx() }) * COVER_WIDTH_FRACTION
                    val left = (widthPx - side) / 2f
                    val top = WindowInsets.statusBars.getTop(density) + with(density) { 76.dp.toPx() }
                    Rect(left, top, left + side, top + side)
                }
                // На паузе обложка плеера чуть уменьшена (artScale): летящая приходит в тот же размер, без скачка.
                val restScale = if (isPlaying) 1f else 0.975f
                val half = base.width / 2f * restScale
                val full = Rect(
                    base.center.x - half, base.center.y - half,
                    base.center.x + half, base.center.y + half
                )
                FlyingCover(track = t, progress = { progress.value }, mini = mini, full = full)
            }
        }

        // Registered after Home so it wins over Home's own back handlers while the player is open.
        PredictiveBackHandler(enabled = open) { events ->
            try {
                events.collect { e -> progress.snapTo(1f - 0.3f * e.progress) }
                settle(false)
            } catch (c: CancellationException) {
                settle(true)
            }
        }
    }
}

/**
 * Обложка между мини-плеером и экраном плеера. Рисуется один раз в размере полной обложки,
 * а положение, размер и скругление считаются из прогресса шторки в слое рисования: без перерасчёта вёрстки
 * и без перезагрузки картинки на каждом кадре.
 */
@Composable
private fun FlyingCover(
    track: Track,
    progress: () -> Float,
    mini: Rect,
    full: Rect
) {
    val density = LocalDensity.current
    val miniCorner = with(density) { MiniCoverCorner.toPx() }
    val fullCorner = with(density) { 20.dp.toPx() }
    Box(
        modifier = Modifier
            .size(with(density) { full.width.toDp() }, with(density) { full.height.toDp() })
            .graphicsLayer {
                val p = progress().coerceIn(0f, 1f)
                val scale = lerp(mini.width, full.width, p) / full.width
                transformOrigin = TransformOrigin(0f, 0f)
                translationX = lerp(mini.left, full.left, p)
                translationY = lerp(mini.top, full.top, p)
                scaleX = scale
                scaleY = scale
                // Скругление задаём в локальных координатах так, чтобы после масштаба оно было нужным на экране.
                shape = RoundedCornerShape(lerp(miniCorner, fullCorner, p) / scale)
                clip = true
            }
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (track.artUri != null) {
            AsyncImage(
                model = track.artUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                Icons.Default.MusicNote,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(0.3f),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
