package com.vanta.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Снимок очереди целиком (а не ссылка на tracks): треки, открытые из проводника,
 * могут отсутствовать в MediaStore, но после убийства процесса очередь должна восстановиться.
 */
@Entity(tableName = "queue_items")
data class QueueItemEntity(
    @PrimaryKey val position: Int,
    val trackId: Long,
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

/** Одна строка (id = 0): состояние плеера для восстановления. */
@Entity(tableName = "player_snapshot")
data class PlayerSnapshotEntity(
    @PrimaryKey val id: Int = 0,
    val currentIndex: Int,
    val positionMs: Long,
    val repeat: String,
    val shuffle: Boolean,
    val speed: Float
)
