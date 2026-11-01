package com.vanta.app.ui.nav

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import com.vanta.app.ui.motion.VantaMotion
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
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
                // Full player enters with a soft lift and settles like a native sheet.
                (slideInVertically(
                    animationSpec = spring(dampingRatio = 0.88f, stiffness = 360f)
                ) { (it * 0.035f).toInt() } +
                    scaleIn(
                        animationSpec = spring(dampingRatio = 0.86f, stiffness = 320f),
                        initialScale = 0.965f
                    ) + fadeIn(tween(220, easing = VantaMotion.EnterEasing)))
                    .togetherWith(
                        scaleOut(tween(150, easing = VantaMotion.StandardEasing), targetScale = 0.985f) +
                            fadeOut(tween(150, easing = VantaMotion.StandardEasing))
                    )
            } else {
                // Reverse transition remains short, controlled and does not snap.
                (scaleIn(
                    animationSpec = spring(dampingRatio = 0.9f, stiffness = 380f),
                    initialScale = 0.985f
                ) + fadeIn(tween(180, easing = VantaMotion.EnterEasing)))
                    .togetherWith(
                        slideOutVertically(
                            animationSpec = spring(dampingRatio = 0.9f, stiffness = 420f)
                        ) { (it * 0.025f).toInt() } +
                            scaleOut(tween(170, easing = VantaMotion.StandardEasing), targetScale = 0.97f) +
                            fadeOut(tween(150, easing = VantaMotion.StandardEasing))
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
