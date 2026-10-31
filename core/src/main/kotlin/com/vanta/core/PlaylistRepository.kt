package com.vanta.core

import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    fun playlists(): Flow<List<Playlist>>
    fun tracks(id: Long): Flow<List<Track>>
    suspend fun create(name: String): Long
    suspend fun rename(id: Long, name: String)
    suspend fun add(id: Long, track: Track)
    suspend fun remove(id: Long, track: Track)
    suspend fun delete(id: Long)
}
