package com.vanta.data.library

import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import com.vanta.data.db.entity.TrackEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Сканирует MediaStore.Audio и возвращает список TrackEntity.
 * Работает на Android 8–15 (minSdk 26).
 */
class MediaStoreScanner(private val context: Context) {

    suspend fun scan(scanId: Long): List<TrackEntity> = withContext(Dispatchers.IO) {
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.DATA // for folder path (still available for query)
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

        val result = mutableListOf<TrackEntity>()

        context.contentResolver.query(
            collection,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val artistIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
            val trackCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val title = cursor.getString(titleCol)?.takeIf { it.isNotBlank() } ?: "Unknown"
                val artist = cursor.getString(artistCol)?.takeIf { it.isNotBlank() && it != "<unknown>" }
                val artistId = cursor.getLong(artistIdCol).takeIf { it > 0 }
                val album = cursor.getString(albumCol)?.takeIf { it.isNotBlank() }
                val albumId = cursor.getLong(albumIdCol).takeIf { it > 0 }
                val durationMs = cursor.getLong(durationCol).coerceAtLeast(0)
                val dateAdded = cursor.getLong(dateCol) * 1000L // seconds → ms
                val sizeBytes = cursor.getLong(sizeCol)
                val mime = cursor.getString(mimeCol)
                val trackNo = cursor.getInt(trackCol).let { if (it >= 1000) it % 1000 else it }
                val dataPath = cursor.getString(dataCol)
                val folder = dataPath?.let { File(it).parent }

                val contentUri = ContentUris.withAppendedId(collection, id).toString()
                val artUri = albumId?.let {
                    ContentUris.withAppendedId(
                        MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                        it
                    ).toString()
                }

                val searchKey = listOfNotNull(title, artist, album)
                    .joinToString(" ")
                    .lowercase()

                result += TrackEntity(
                    id = id,
                    uri = contentUri,
                    title = title,
                    artist = artist,
                    artistId = artistId,
                    album = album,
                    albumId = albumId,
                    genre = null,
                    artUri = artUri,
                    durationMs = durationMs,
                    folder = folder,
                    trackNo = trackNo,
                    dateAdded = dateAdded,
                    sizeBytes = sizeBytes,
                    mimeType = mime,
                    searchKey = searchKey,
                    scanId = scanId
                )
            }
        }

        result
    }
}
