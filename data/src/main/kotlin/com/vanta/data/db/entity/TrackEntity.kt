package com.vanta.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Трек из MediaStore. id = MediaStore _ID. scanId нужен, чтобы удалять файлы, пропавшие с устройства. */
@Entity(
    tableName = "tracks",
    indices = [
        Index("albumId"), Index("artistId"), Index("folder"),
        Index("genre"), Index("dateAdded"), Index("scanId")
    ]
)
data class TrackEntity(
    @PrimaryKey val id: Long,
    val uri: String,
    val title: String,
    val artist: String?,
    val artistId: Long?,
    val album: String?,
    val albumId: Long?,
    val genre: String?,
    val artUri: String?,
    val durationMs: Long,
    val folder: String?,
    val trackNo: Int,
    val dateAdded: Long,
    val sizeBytes: Long,
    val mimeType: String?,
    /** title + artist + album в нижнем регистре (Kotlin lowercase), чтобы поиск работал с кириллицей */
    val searchKey: String,
    val scanId: Long
)
