package com.vanta.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vanta.core.LibraryRepository
import com.vanta.core.PlayerController
import com.vanta.core.PlayerState
import com.vanta.core.ScanState
import com.vanta.core.SortOrder
import com.vanta.core.Track
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val tracks: List<Track> = emptyList(),
    val player: PlayerState = PlayerState()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val library: LibraryRepository,
    private val player: PlayerController
) : ViewModel() {

    val scanState: StateFlow<ScanState> = library.scanState

    val uiState: StateFlow<HomeUiState> = combine(
        library.songs(SortOrder.TITLE),
        player.state
    ) { tracks, playerState ->
        HomeUiState(tracks = tracks, player = playerState)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun play(track: Track) {
        val tracks = uiState.value.tracks
        val index = tracks.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        player.playTracks(tracks, index)
    }

    fun togglePlayback() = player.toggle()
    fun next() = player.next()
    fun rescan() = library.rescan()
}
