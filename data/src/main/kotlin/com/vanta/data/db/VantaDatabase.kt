package com.vanta.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.vanta.data.db.dao.PlayStatDao
import com.vanta.data.db.dao.PlaylistDao
import com.vanta.data.db.dao.QueueDao
import com.vanta.data.db.dao.ResumeDao
import com.vanta.data.db.dao.TrackDao
import com.vanta.data.db.entity.PlayStatEntity
import com.vanta.data.db.entity.PlayerSnapshotEntity
import com.vanta.data.db.entity.PlaylistEntity
import com.vanta.data.db.entity.PlaylistTrackEntity
import com.vanta.data.db.entity.QueueItemEntity
import com.vanta.data.db.entity.ResumePositionEntity
import com.vanta.data.db.entity.TrackEntity

@Database(
    entities = [
        TrackEntity::class, PlayStatEntity::class,
        PlaylistEntity::class, PlaylistTrackEntity::class,
        QueueItemEntity::class, PlayerSnapshotEntity::class,
        ResumePositionEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class VantaDatabase : RoomDatabase() {
    abstract fun tracks(): TrackDao
    abstract fun playStats(): PlayStatDao
    abstract fun playlists(): PlaylistDao
    abstract fun queue(): QueueDao
    abstract fun resume(): ResumeDao

    companion object {
        fun create(context: Context): VantaDatabase =
            Room.databaseBuilder(context.applicationContext, VantaDatabase::class.java, "vanta.db")
                .build()
    }
}
