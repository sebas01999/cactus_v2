package com.phytotec.recepcion.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.phytotec.recepcion.data.local.dao.ConfirmacionDao
import com.phytotec.recepcion.data.local.entities.ConfirmacionLocalEntity
import com.phytotec.recepcion.data.local.entities.SyncStatus

class Converters {
    @TypeConverter
    fun fromSyncStatus(value: SyncStatus): String = value.name

    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus = SyncStatus.valueOf(value)
}

@Database(
    entities = [ConfirmacionLocalEntity::class],
    version = 2,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun confirmacionDao(): ConfirmacionDao
}
