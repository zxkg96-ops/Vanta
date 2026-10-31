package com.vanta.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.vanta.data.db.entity.TrackEntity
import kotlinx.coroutines.flow.Flow

/** minMs — фильтр коротких треков из настроек (рингтоны, звуки мессенджеров). */
@Dao
interface TrackDao {

    @Upsert
    suspend fun upsertAll(tracks: List<TrackEntity>)

    @Query("DELETE FROM tracks WHERE scanId != :scanId")
    suspend fun deleteStale(scanId: Long): Int

    @Query("SELECT COUNT(*) FROM tracks")
    suspend fun count(): Int

    @Query("SELECT * FROM tracks WHERE uri = :uri LIMIT 1")
    suspend fun byUri(uri: String): TrackEntity?

    @Query("SELECT * FROM tracks WHERE durationMs >= :minMs ORDER BY title COLLATE NOCASE ASC")
    fun songsByTitle(minMs: Long): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE durationMs >= :minMs ORDER BY artist COLLATE NOCASE ASC, title COLLATE NOCASE ASC")
    fun songsByArtist(minMs: Long): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE durationMs >= :minMs ORDER BY dateAdded DESC")
    fun songsByDateAdded(minMs: Long): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE durationMs >= :minMs ORDER BY durationMs DESC")
    fun songsByDuration(minMs: Long): Flow<List<TrackEntity>>

    @Query(
        """SELECT albumId AS id, MIN(album) AS title, MIN(artist) AS artist, MIN(artUri) AS artUri, COUNT(*) AS trackCount
           FROM tracks WHERE albumId IS NOT NULL AND durationMs >= :minMs
           GROUP BY albumId ORDER BY MIN(album) COLLATE NOCASE"""
    )
    fun albums(minMs: Long): Flow<List<AlbumRow>>

    @Query(
        """SELECT artistId AS id, MIN(artist) AS name, COUNT(*) AS trackCount
           FROM tracks WHERE artistId IS NOT NULL AND durationMs >= :minMs
           GROUP BY artistId ORDER BY MIN(artist) COLLATE NOCASE"""
    )
    fun artists(minMs: Long): Flow<List<ArtistRow>>

    @Query(
        """SELECT folder AS path, COUNT(*) AS trackCount
           FROM tracks WHERE folder IS NOT NULL AND durationMs >= :minMs
           GROUP BY folder ORDER BY folder COLLATE NOCASE"""
    )
    fun folders(minMs: Long): Flow<List<FolderRow>>

    @Query(
        """SELECT genre AS name, COUNT(*) AS trackCount
           FROM tracks WHERE genre IS NOT NULL AND durationMs >= :minMs
           GROUP BY genre ORDER BY genre COLLATE NOCASE"""
    )
    fun genres(minMs: Long): Flow<List<GenreRow>>

    @Query("SELECT * FROM tracks WHERE albumId = :id AND durationMs >= :minMs ORDER BY trackNo ASC, title COLLATE NOCASE ASC")
    fun tracksOfAlbum(id: Long, minMs: Long): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE artistId = :id AND durationMs >= :minMs ORDER BY album COLLATE NOCASE ASC, trackNo ASC")
    fun tracksOfArtist(id: Long, minMs: Long): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE folder = :path AND durationMs >= :minMs ORDER BY title COLLATE NOCASE ASC")
    fun tracksOfFolder(path: String, minMs: Long): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE genre = :name AND durationMs >= :minMs ORDER BY title COLLATE NOCASE ASC")
    fun tracksOfGenre(name: String, minMs: Long): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE durationMs >= :minMs ORDER BY dateAdded DESC LIMIT 30")
    fun recentlyAdded(minMs: Long): Flow<List<TrackEntity>>

    @Query(
        """SELECT t.* FROM tracks t INNER JOIN play_stats p ON p.trackId = t.id
           WHERE t.durationMs >= :minMs ORDER BY p.lastPlayedAt DESC LIMIT 30"""
    )
    fun recentlyPlayed(minMs: Long): Flow<List<TrackEntity>>

    @Query(
        """SELECT t.* FROM tracks t INNER JOIN play_stats p ON p.trackId = t.id
           WHERE t.durationMs >= :minMs ORDER BY p.playCount DESC, p.lastPlayedAt DESC LIMIT 30"""
    )
    fun mostPlayed(minMs: Long): Flow<List<TrackEntity>>

    /** pattern: уже в нижнем регистре и в виде %запрос% (экранирование % и _ делает репозиторий). */
    @Query(
        """SELECT * FROM tracks WHERE durationMs >= :minMs AND searchKey LIKE :pattern ESCAPE '\'
           ORDER BY title COLLATE NOCASE ASC LIMIT 200"""
    )
    fun search(pattern: String, minMs: Long): Flow<List<TrackEntity>>
}
