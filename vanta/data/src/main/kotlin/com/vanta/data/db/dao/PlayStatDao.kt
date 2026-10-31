package com.vanta.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.vanta.data.db.entity.PlayStatEntity

@Dao
abstract class PlayStatDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertIfAbsent(stat: PlayStatEntity)

    @Query("UPDATE play_stats SET playCount = playCount + 1, lastPlayedAt = :now WHERE trackId = :trackId")
    abstract suspend fun increment(trackId: Long, now: Long)

    /** Без SQL upsert-синтаксиса: он требует SQLite 3.24, а на Android 8-10 версия старше. */
    @Transaction
    open suspend fun registerPlay(trackId: Long, now: Long) {
        insertIfAbsent(PlayStatEntity(trackId, 0, now))
        increment(trackId, now)
    }
}
