package com.vanta.data.library

import com.vanta.core.Album
import com.vanta.core.Artist
import com.vanta.core.Folder
import com.vanta.core.Genre
import com.vanta.core.LibraryRepository
import com.vanta.core.ScanState
import com.vanta.core.SortOrder
import com.vanta.core.Track
import com.vanta.data.db.VantaDatabase
import com.vanta.data.db.toAlbum
import com.vanta.data.db.toArtist
import com.vanta.data.db.toFolder
import com.vanta.data.db.toGenre
import com.vanta.data.db.toTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class RealLibraryRepository(
    private val db: VantaDatabase,
    private val scanner: MediaStoreScanner,
    private val minDurationMs: Long = 30_000L
) : LibraryRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    override val scanState: StateFlow<ScanState> = _scanState.asStateFlow()

    private val tracks = db.tracks()

    init {
        // Auto-scan on first creation if library is empty
        scope.launch {
            if (tracks.count() == 0) {
                rescanInternal()
            }
        }
    }

    override fun songs(sort: SortOrder): Flow<List<Track>> = when (sort) {
        SortOrder.TITLE -> tracks.songsByTitle(minDurationMs)
        SortOrder.ARTIST -> tracks.songsByArtist(minDurationMs)
        SortOrder.DATE_ADDED -> tracks.songsByDateAdded(minDurationMs)
        SortOrder.DURATION -> tracks.songsByDuration(minDurationMs)
    }.map { list -> list.map { it.toTrack() } }

    override fun albums(): Flow<List<Album>> =
        tracks.albums(minDurationMs).map { rows -> rows.map { it.toAlbum() } }

    override fun artists(): Flow<List<Artist>> =
        tracks.artists(minDurationMs).map { rows -> rows.map { it.toArtist() } }

    override fun folders(): Flow<List<Folder>> =
        tracks.folders(minDurationMs).map { rows -> rows.map { it.toFolder() } }

    override fun genres(): Flow<List<Genre>> =
        tracks.genres(minDurationMs).map { rows -> rows.map { it.toGenre() } }

    override fun tracksOfAlbum(id: Long): Flow<List<Track>> =
        tracks.tracksOfAlbum(id, minDurationMs).map { list -> list.map { it.toTrack() } }

    override fun tracksOfArtist(id: Long): Flow<List<Track>> =
        tracks.tracksOfArtist(id, minDurationMs).map { list -> list.map { it.toTrack() } }

    override fun tracksOfFolder(path: String): Flow<List<Track>> =
        tracks.tracksOfFolder(path, minDurationMs).map { list -> list.map { it.toTrack() } }

    override fun tracksOfGenre(name: String): Flow<List<Track>> =
        tracks.tracksOfGenre(name, minDurationMs).map { list -> list.map { it.toTrack() } }

    override fun recentlyPlayed(): Flow<List<Track>> =
        tracks.recentlyPlayed(minDurationMs).map { list -> list.map { it.toTrack() } }

    override fun recentlyAdded(): Flow<List<Track>> =
        tracks.recentlyAdded(minDurationMs).map { list -> list.map { it.toTrack() } }

    override fun mostPlayed(): Flow<List<Track>> =
        tracks.mostPlayed(minDurationMs).map { list -> list.map { it.toTrack() } }

    override fun search(query: String): Flow<List<Track>> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return songs(SortOrder.TITLE)
        val escaped = q.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
        return tracks.search("%$escaped%", minDurationMs).map { list -> list.map { it.toTrack() } }
    }

    override fun rescan() {
        scope.launch { rescanInternal() }
    }

    private suspend fun rescanInternal() {
        _scanState.value = ScanState.Scanning(0)
        val scanId = System.currentTimeMillis()
        val found = scanner.scan(scanId)
        _scanState.value = ScanState.Scanning(found.size)
        tracks.upsertAll(found)
        tracks.deleteStale(scanId)
        _scanState.value = ScanState.Idle
    }
}
