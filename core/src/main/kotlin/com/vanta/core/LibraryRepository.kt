package com.vanta.core

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface LibraryRepository {
    val scanState: StateFlow<ScanState>

    fun songs(sort: SortOrder): Flow<List<Track>>
    fun albums(): Flow<List<Album>>
    fun artists(): Flow<List<Artist>>
    fun folders(): Flow<List<Folder>>
    fun genres(): Flow<List<Genre>>

    fun tracksOfAlbum(id: Long): Flow<List<Track>>
    fun tracksOfArtist(id: Long): Flow<List<Track>>
    fun tracksOfFolder(path: String): Flow<List<Track>>
    fun tracksOfGenre(name: String): Flow<List<Track>>

    fun recentlyPlayed(): Flow<List<Track>>
    fun recentlyAdded(): Flow<List<Track>>
    fun mostPlayed(): Flow<List<Track>>
    fun search(query: String): Flow<List<Track>>

    fun rescan()
}
