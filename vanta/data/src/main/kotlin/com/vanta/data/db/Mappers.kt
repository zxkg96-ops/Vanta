package com.vanta.data.db

import com.vanta.core.Album
import com.vanta.core.Artist
import com.vanta.core.Folder
import com.vanta.core.Genre
import com.vanta.core.Playlist
import com.vanta.core.Track
import com.vanta.data.db.dao.AlbumRow
import com.vanta.data.db.dao.ArtistRow
import com.vanta.data.db.dao.FolderRow
import com.vanta.data.db.dao.GenreRow
import com.vanta.data.db.dao.PlaylistRow
import com.vanta.data.db.entity.QueueItemEntity
import com.vanta.data.db.entity.TrackEntity

private const val UNKNOWN = "Unknown"

fun TrackEntity.toTrack() = Track(
    id = id, uri = uri, title = title, artist = artist, album = album, albumId = albumId,
    genre = genre, artUri = artUri, durationMs = durationMs, folder = folder
)

fun QueueItemEntity.toTrack() = Track(
    id = trackId, uri = uri, title = title, artist = artist, album = album, albumId = albumId,
    genre = genre, artUri = artUri, durationMs = durationMs, folder = folder
)

fun Track.toQueueItem(position: Int) = QueueItemEntity(
    position = position, trackId = id, uri = uri, title = title, artist = artist, album = album,
    albumId = albumId, genre = genre, artUri = artUri, durationMs = durationMs, folder = folder
)

fun AlbumRow.toAlbum() = Album(id, title ?: UNKNOWN, artist, artUri, trackCount)
fun ArtistRow.toArtist() = Artist(id, name ?: UNKNOWN, trackCount)
fun FolderRow.toFolder() = Folder(path, trackCount)
fun GenreRow.toGenre() = Genre(name, trackCount)
fun PlaylistRow.toPlaylist() = Playlist(id, name, trackCount)
