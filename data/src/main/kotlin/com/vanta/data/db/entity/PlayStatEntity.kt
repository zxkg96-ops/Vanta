package com.vanta.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Статистика вынесена из tracks, чтобы пересканирование её не затирало. */
@Entity(tableName = "play_stats")
data class PlayStatEntity(
    @PrimaryKey val trackId: Long,
    val playCount: Int,
    val lastPlayedAt: Long
)
