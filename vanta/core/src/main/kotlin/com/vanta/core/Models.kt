package com.vanta.core

data class Track(
    val id: Long,
    val uri: String,
    val title: String,
    val artist: String?,
    val album: String?,
    val albumId: Long?,
    val genre: String?,
    val artUri: String?,
    val durationMs: Long,
    val folder: String?
)

data class Album(val id: Long, val title: String, val artist: String?, val artUri: String?, val trackCount: Int)
data class Artist(val id: Long, val name: String, val trackCount: Int)
data class Folder(val path: String, val trackCount: Int)
data class Genre(val name: String, val trackCount: Int)
data class Playlist(val id: Long, val name: String, val trackCount: Int)

enum class SortOrder { TITLE, ARTIST, DATE_ADDED, DURATION }

sealed interface ScanState {
    object Idle : ScanState
    data class Scanning(val found: Int) : ScanState
}
