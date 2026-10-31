package com.vanta.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vanta.app.data.UserDataStore
import com.vanta.app.data.UserPlaylist
import com.vanta.core.Album
import com.vanta.core.Artist
import com.vanta.core.LibraryRepository
import com.vanta.core.PlayerController
import com.vanta.core.PlayerState
import com.vanta.core.ScanState
import com.vanta.core.SortOrder
import com.vanta.core.Track
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val tracks: List<Track> = emptyList(),
    val player: PlayerState = PlayerState()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val library: LibraryRepository,
    private val player: PlayerController,
    private val userData: UserDataStore
) : ViewModel() {

    val scanState: StateFlow<ScanState> = library.scanState

    private val allSongs: StateFlow<List<Track>> =
        library.songs(SortOrder.TITLE).stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val allTracks: StateFlow<List<Track>> = allSongs

    val query = MutableStateFlow("")

    val uiState: StateFlow<HomeUiState> = combine(allSongs, player.state, query) { tracks, playerState, q ->
        val f = q.trim()
        val shown = if (f.isEmpty()) tracks else tracks.filter { t ->
            t.title.contains(f, ignoreCase = true) ||
                (t.artist?.contains(f, ignoreCase = true) == true) ||
                (t.album?.contains(f, ignoreCase = true) == true)
        }
        HomeUiState(tracks = shown, player = playerState)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    val albums: StateFlow<List<Album>> =
        library.albums().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val artists: StateFlow<List<Artist>> =
        library.artists().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val favorites: StateFlow<Set<Long>> = userData.favorites
    val playlists: StateFlow<List<UserPlaylist>> = userData.playlists

    init {
        // Восстановить последнюю очередь и позицию, когда библиотека загрузилась
        viewModelScope.launch {
            val songs = allSongs.first { it.isNotEmpty() }
            player.restoreSession(songs)
        }
    }

    fun setQuery(q: String) {
        query.value = q
    }

    fun play(track: Track) {
        val tracks = uiState.value.tracks
        val index = tracks.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        player.playTracks(tracks, index)
    }

    fun playAll(tracks: List<Track>) {
        if (tracks.isNotEmpty()) player.playTracks(tracks, 0)
    }

    fun playAlbum(id: Long) {
        viewModelScope.launch { playAll(library.tracksOfAlbum(id).first()) }
    }

    fun playArtist(id: Long) {
        viewModelScope.launch { playAll(library.tracksOfArtist(id).first()) }
    }

    fun playNext(track: Track) = player.playNext(track)
    fun addToQueue(track: Track) = player.addToQueue(track)

    fun toggleFavorite(trackId: Long) = userData.toggleFavorite(trackId)
    fun createPlaylist(name: String, firstTrackId: Long?) = userData.createPlaylist(name, firstTrackId)
    fun deletePlaylist(id: Long) = userData.deletePlaylist(id)
    fun addToPlaylist(playlistId: Long, trackId: Long) = userData.addToPlaylist(playlistId, trackId)

    fun closePlayer() {
        player.pause()
        player.playTracks(emptyList(), 0)
    }

    fun setEqEnabled(on: Boolean) = player.setEqEnabled(on)
    fun setEqBand(index: Int, db: Float) = player.setEqBand(index, db)
    fun setBassBoost(level: Int) = player.setBassBoost(level)
    fun applyPreset(name: String) = player.applyPreset(name)
    fun setAutoHeadphones(on: Boolean) = player.setAutoHeadphones(on)

    fun togglePlayback() = player.toggle()
    fun next() = player.next()
    fun rescan() = library.rescan()
}
