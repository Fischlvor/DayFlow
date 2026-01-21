package com.hsk.dayflow.core.di

import android.content.Context
import androidx.room.Room
import com.hsk.dayflow.core.database.DayFlowDatabase
import com.hsk.dayflow.core.database.dao.EventDao
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

    @Provides
    @Singleton
    fun provideDayFlowDatabase(
        @ApplicationContext context: Context
    ): DayFlowDatabase {
        return Room.databaseBuilder(
            context,
            DayFlowDatabase::class.java,
            DayFlowDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideEventDao(database: DayFlowDatabase): EventDao {
        return database.eventDao()
    }
}
