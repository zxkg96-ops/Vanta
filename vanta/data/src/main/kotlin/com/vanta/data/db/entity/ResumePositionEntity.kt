package com.vanta.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Позиция в каждом файле (подкасты, аудиокниги). Ключ — uri, чтобы работало и для файлов вне MediaStore. */
@Entity(tableName = "resume_positions")
data class ResumePositionEntity(
    @PrimaryKey val uri: String,
    val positionMs: Long,
    val updatedAt: Long
)
