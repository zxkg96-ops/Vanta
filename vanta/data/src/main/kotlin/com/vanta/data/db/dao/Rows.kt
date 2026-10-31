package com.vanta.data.db.dao

data class AlbumRow(val id: Long, val title: String?, val artist: String?, val artUri: String?, val trackCount: Int)
data class ArtistRow(val id: Long, val name: String?, val trackCount: Int)
data class FolderRow(val path: String, val trackCount: Int)
data class GenreRow(val name: String, val trackCount: Int)
data class PlaylistRow(val id: Long, val name: String, val trackCount: Int)
