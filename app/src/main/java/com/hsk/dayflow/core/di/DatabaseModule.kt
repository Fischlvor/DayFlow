package com.hsk.dayflow.core.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.hsk.dayflow.core.database.DayFlowDatabase
import com.hsk.dayflow.core.database.dao.EventDao
import com.hsk.dayflow.core.database.dao.SubscriptionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 数据库依赖注入模块
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // 创建订阅表
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS subscriptions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    name TEXT NOT NULL,
                    url TEXT NOT NULL,
                    color TEXT NOT NULL DEFAULT 'BLUE',
                    isEnabled INTEGER NOT NULL DEFAULT 1,
                    syncInterval INTEGER NOT NULL DEFAULT 24,
                    lastSyncTime TEXT,
                    lastSyncStatus TEXT NOT NULL DEFAULT 'NEVER',
                    errorMessage TEXT,
                    createdAt TEXT NOT NULL
                )
            """.trimIndent())
            
            // 给 events 表添加 subscriptionId 列
            db.execSQL("ALTER TABLE events ADD COLUMN subscriptionId INTEGER DEFAULT NULL")
        }
    }

    @Provides
    @Singleton
    fun provideDayFlowDatabase(
        @ApplicationContext context: Context
    ): DayFlowDatabase {
        return Room.databaseBuilder(
            context,
            DayFlowDatabase::class.java,
            DayFlowDatabase.DATABASE_NAME
        )
            .addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideEventDao(database: DayFlowDatabase): EventDao {
        return database.eventDao()
    }

    @Provides
    @Singleton
    fun provideSubscriptionDao(database: DayFlowDatabase): SubscriptionDao {
        return database.subscriptionDao()
    }
}
