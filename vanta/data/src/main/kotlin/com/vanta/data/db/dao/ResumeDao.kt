package com.vanta.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.vanta.data.db.entity.ResumePositionEntity

@Dao
interface ResumeDao {

    @Query("SELECT positionMs FROM resume_positions WHERE uri = :uri")
    suspend fun position(uri: String): Long?

    @Upsert
    suspend fun save(entry: ResumePositionEntity)

    @Query("DELETE FROM resume_positions WHERE uri = :uri")
    suspend fun clear(uri: String)

    @Query("DELETE FROM resume_positions WHERE updatedAt < :before")
    suspend fun prune(before: Long)
}
