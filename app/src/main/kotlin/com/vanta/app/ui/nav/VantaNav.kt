package com.vanta.app.ui.nav

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
import javax.inject.Inject

@Composable
fun VantaRoot(
    player: PlayerController,
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    var showNowPlaying by remember { mutableStateOf(false) }

    if (showNowPlaying) {
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
