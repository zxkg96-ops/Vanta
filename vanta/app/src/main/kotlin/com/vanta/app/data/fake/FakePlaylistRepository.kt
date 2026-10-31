package com.vanta.app.data.fake

import com.vanta.core.Playlist
import com.vanta.core.PlaylistRepository
import com.vanta.core.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf

class FakePlaylistRepository : PlaylistRepository {
    private val playlistState = MutableStateFlow(
        listOf(Playlist(1L, "Late night", 4), Playlist(2L, "Focus", 6))
    )
    private val trackState = mutableMapOf(
        1L to MutableStateFlow(FakeCatalog.tracks.take(4)),
        2L to MutableStateFlow(FakeCatalog.tracks.drop(4).take(6))
    )
    private var nextId = 3L

    override fun playlists(): Flow<List<Playlist>> = playlistState.asStateFlow()
    override fun tracks(id: Long): Flow<List<Track>> = trackState[id]?.asStateFlow() ?: flowOf(emptyList())

    override suspend fun create(name: String): Long {
        val id = nextId++
        trackState[id] = MutableStateFlow(emptyList())
        playlistState.value = playlistState.value + Playlist(id, name, 0)
        return id
    }

    override suspend fun rename(id: Long, name: String) {
        playlistState.value = playlistState.value.map { if (it.id == id) it.copy(name = name) else it }
    }

    override suspend fun add(id: Long, track: Track) {
        val flow = trackState.getOrPut(id) { MutableStateFlow(emptyList()) }
        if (flow.value.none { it.id == track.id }) flow.value = flow.value + track
        refreshCount(id)
    }

    override suspend fun remove(id: Long, track: Track) {
        trackState[id]?.let { it.value = it.value.filterNot { item -> item.id == track.id } }
        refreshCount(id)
    }

    override suspend fun delete(id: Long) {
        trackState.remove(id)
        playlistState.value = playlistState.value.filterNot { it.id == id }
    }

    private fun refreshCount(id: Long) {
        val count = trackState[id]?.value?.size ?: 0
        playlistState.value = playlistState.value.map { if (it.id == id) it.copy(trackCount = count) else it }
    }
}
