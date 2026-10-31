package com.vanta.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vanta.core.LibraryRepository
import com.vanta.core.PlayerController
import com.vanta.core.PlayerState
import com.vanta.core.Track
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val recentlyAdded: List<Track> = emptyList(),
    val player: PlayerState = PlayerState()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val library: LibraryRepository,
    private val player: PlayerController
) : ViewModel() {
    val uiState: StateFlow<HomeUiState> = combine(
        library.recentlyAdded(),
        player.state
    ) { tracks, playerState ->
        HomeUiState(recentlyAdded = tracks, player = playerState)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun play(track: Track) {
        val tracks = uiState.value.recentlyAdded
        player.playTracks(tracks, tracks.indexOf(track).coerceAtLeast(0))
    }

    fun togglePlayback() = player.toggle()
    fun next() = player.next()
}
