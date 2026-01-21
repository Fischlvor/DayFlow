package com.hsk.dayflow.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.hsk.dayflow.core.database.dao.EventDao
import com.hsk.dayflow.core.database.entity.EventEntity

/**
 * DayFlow 应用数据库
 */
@Database(
    entities = [EventEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class DayFlowDatabase : RoomDatabase() {

    abstract fun eventDao(): EventDao

    companion object {
        const val DATABASE_NAME = "dayflow_database"
    }
}
