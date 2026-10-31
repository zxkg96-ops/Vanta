package com.vanta.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.vanta.data.db.entity.PlayerSnapshotEntity
import com.vanta.data.db.entity.QueueItemEntity

@Dao
abstract class QueueDao {

    @Query("SELECT * FROM queue_items ORDER BY position ASC")
    abstract suspend fun queue(): List<QueueItemEntity>

    @Query("DELETE FROM queue_items")
    abstract suspend fun clearQueue()

    @Insert
    abstract suspend fun insertQueue(items: List<QueueItemEntity>)

    @Transaction
    open suspend fun replaceQueue(items: List<QueueItemEntity>) {
        clearQueue()
        insertQueue(items)
    }

    @Query("SELECT * FROM player_snapshot WHERE id = 0")
    abstract suspend fun snapshot(): PlayerSnapshotEntity?

    @Upsert
    abstract suspend fun saveSnapshot(snapshot: PlayerSnapshotEntity)
}
