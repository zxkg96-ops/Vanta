package com.vanta.app.ui.nav

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
                    targetContentEnter = slideInVertically(tween(380)) { it } + fadeIn(tween(250)),
                    initialContentExit = fadeOut(tween(300)),
                    targetContentZIndex = 1f
                )
            } else {
                // Закрытие: плеер уезжает вниз, главная проявляется
                ContentTransform(
                    targetContentEnter = fadeIn(tween(300)),
                    initialContentExit = slideOutVertically(tween(320)) { it } + fadeOut(tween(250)),
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
