package com.vanta.app.data.fake

import com.vanta.core.Album
import com.vanta.core.Artist
import com.vanta.core.Folder
import com.vanta.core.Genre
import com.vanta.core.LibraryRepository
import com.vanta.core.ScanState
import com.vanta.core.SortOrder
import com.vanta.core.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf

class FakeLibraryRepository : LibraryRepository {
    private val data = FakeCatalog.tracks
    private val mutableScan = MutableStateFlow<ScanState>(ScanState.Idle)
    override val scanState = mutableScan.asStateFlow()

    override fun songs(sort: SortOrder): Flow<List<Track>> = flowOf(
        when (sort) {
            SortOrder.TITLE -> data.sortedBy { it.title.lowercase() }
            SortOrder.ARTIST -> data.sortedBy { it.artist.orEmpty().lowercase() }
            SortOrder.DATE_ADDED -> data.reversed()
            SortOrder.DURATION -> data.sortedByDescending { it.durationMs }
        }
    )

    override fun albums(): Flow<List<Album>> = flowOf(data.groupBy { it.albumId }.mapNotNull { (id, tracks) ->
        id?.let { Album(it, tracks.first().album ?: "Unknown album", tracks.first().artist, tracks.first().artUri, tracks.size) }
    }.sortedBy { it.title })

    override fun artists(): Flow<List<Artist>> = flowOf(data.groupBy { it.artist }.mapNotNull { (name, tracks) ->
        name?.let { Artist(tracks.first().id, it, tracks.size) }
    }.sortedBy { it.name })

    override fun folders(): Flow<List<Folder>> = flowOf(data.groupBy { it.folder }.mapNotNull { (path, tracks) ->
        path?.let { Folder(it, tracks.size) }
    }.sortedBy { it.path })

    override fun genres(): Flow<List<Genre>> = flowOf(data.groupBy { it.genre }.mapNotNull { (name, tracks) ->
        name?.let { Genre(it, tracks.size) }
    }.sortedBy { it.name })

    override fun tracksOfAlbum(id: Long): Flow<List<Track>> = flowOf(data.filter { it.albumId == id })
    override fun tracksOfArtist(id: Long): Flow<List<Track>> = flowOf(data.filter { it.id == id || it.artist == data.firstOrNull { t -> t.id == id }?.artist })
    override fun tracksOfFolder(path: String): Flow<List<Track>> = flowOf(data.filter { it.folder == path })
    override fun tracksOfGenre(name: String): Flow<List<Track>> = flowOf(data.filter { it.genre == name })
    override fun recentlyPlayed(): Flow<List<Track>> = flowOf(data.takeLast(10).reversed())
    override fun recentlyAdded(): Flow<List<Track>> = flowOf(data.takeLast(16).reversed())
    override fun mostPlayed(): Flow<List<Track>> = flowOf(data.sortedBy { it.id % 7 }.take(12))
    override fun search(query: String): Flow<List<Track>> {
        val q = query.trim().lowercase()
        return flowOf(if (q.isEmpty()) emptyList() else data.filter {
            it.title.lowercase().contains(q) || it.artist.orEmpty().lowercase().contains(q) ||
                it.album.orEmpty().lowercase().contains(q) || it.genre.orEmpty().lowercase().contains(q)
        })
    }
    override fun rescan() {
        mutableScan.value = ScanState.Scanning(data.size)
        mutableScan.value = ScanState.Idle
    }
}
