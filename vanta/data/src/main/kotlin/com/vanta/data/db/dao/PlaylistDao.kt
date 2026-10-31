package com.vanta.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vanta.data.db.entity.PlaylistEntity
import com.vanta.data.db.entity.PlaylistTrackEntity
import com.vanta.data.db.entity.TrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Query(
        """SELECT p.id AS id, p.name AS name, COUNT(pt.trackId) AS trackCount
           FROM playlists p LEFT JOIN playlist_tracks pt ON pt.playlistId = p.id
           GROUP BY p.id ORDER BY p.name COLLATE NOCASE"""
    )
    fun playlists(): Flow<List<PlaylistRow>>

    @Query(
        """SELECT t.* FROM tracks t INNER JOIN playlist_tracks pt ON pt.trackId = t.id
           WHERE pt.playlistId = :id ORDER BY pt.addedAt ASC, pt.trackId ASC"""
    )
    fun tracks(id: Long): Flow<List<TrackEntity>>

    @Insert
    suspend fun insert(playlist: PlaylistEntity): Long

    @Query("UPDATE playlists SET name = :name WHERE id = :id")
    suspend fun rename(id: Long, name: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTrack(entry: PlaylistTrackEntity)

    @Query("DELETE FROM playlist_tracks WHERE playlistId = :id AND trackId = :trackId")
    suspend fun removeTrack(id: Long, trackId: Long)

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun delete(id: Long)
}
