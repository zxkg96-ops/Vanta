package com.vanta.app.ui.nav

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import com.vanta.app.ui.motion.VantaMotion
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.vanta.app.ui.home.HomeScreen
import com.vanta.app.ui.home.HomeViewModel
import com.vanta.app.ui.player.NowPlayingScreen
import com.vanta.core.PlayerController

@Composable
fun VantaRoot(
    player: PlayerController,
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    var showNowPlaying by remember { mutableStateOf(false) }

    BackHandler(enabled = showNowPlaying) { showNowPlaying = false }

    AnimatedContent(
        targetState = showNowPlaying,
        transitionSpec = {
            if (targetState) {
                // Открытие плеера: выезжает снизу
                ContentTransform(
                    targetContentEnter = slideInVertically(tween(VantaMotion.EMPHASIZED_MS, easing = VantaMotion.DecelerateEasing)) { (it * 0.045f).toInt() } + scaleIn(tween(VantaMotion.EMPHASIZED_MS, easing = VantaMotion.DecelerateEasing), initialScale = 0.985f) + fadeIn(tween(VantaMotion.NORMAL_MS, easing = VantaMotion.EnterEasing)),
                    initialContentExit = scaleOut(tween(VantaMotion.EXIT_MS, easing = VantaMotion.StandardEasing), targetScale = 0.99f) + fadeOut(tween(VantaMotion.EXIT_MS, easing = VantaMotion.StandardEasing)),
                    targetContentZIndex = 1f
                )
            } else {
                // Закрытие: плеер уезжает вниз, главная проявляется
                ContentTransform(
                    targetContentEnter = scaleIn(tween(VantaMotion.NORMAL_MS, easing = VantaMotion.DecelerateEasing), initialScale = 0.99f) + fadeIn(tween(VantaMotion.NORMAL_MS, easing = VantaMotion.EnterEasing)),
                    initialContentExit = slideOutVertically(tween(VantaMotion.EXIT_MS, easing = VantaMotion.StandardEasing)) { (it * 0.035f).toInt() } + scaleOut(tween(VantaMotion.EXIT_MS, easing = VantaMotion.StandardEasing), targetScale = 0.985f) + fadeOut(tween(VantaMotion.EXIT_MS, easing = VantaMotion.StandardEasing)),
                    targetContentZIndex = -1f
                )
            }
        },
        label = "rootTransition"
    ) { nowPlaying ->
        if (nowPlaying) {
            NowPlayingScreen(
                player = player,
                onClose = { showNowPlaying = false }
            )
        } else {
            HomeScreen(
                viewModel = homeViewModel,
                onOpenPlayer = { showNowPlaying = true }
            )
        }
    }
}
