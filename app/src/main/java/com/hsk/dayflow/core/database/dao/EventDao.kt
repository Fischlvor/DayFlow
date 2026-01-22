package com.hsk.dayflow.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hsk.dayflow.core.database.entity.EventEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * 日程事件数据访问对象
 */
@Dao
interface EventDao {

    /**
     * 获取所有事件（按开始时间排序）
     */
    @Query("SELECT * FROM events ORDER BY startTime ASC")
    fun getAllEvents(): Flow<List<EventEntity>>

    /**
     * 根据ID获取事件
     */
    @Query("SELECT * FROM events WHERE id = :eventId")
    suspend fun getEventById(eventId: Long): EventEntity?

    /**
     * 根据ID获取事件（Flow）
     */
    @Query("SELECT * FROM events WHERE id = :eventId")
    fun getEventByIdFlow(eventId: Long): Flow<EventEntity?>

    /**
     * 获取指定日期范围内的事件
     */
    @Query("""
        SELECT * FROM events 
        WHERE startTime >= :startDate AND startTime < :endDate 
        OR endTime >= :startDate AND endTime < :endDate
        OR startTime < :startDate AND endTime >= :endDate
        ORDER BY startTime ASC
    """)
    fun getEventsBetween(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<EventEntity>>

    /**
     * 获取指定日期的事件
     */
    @Query("""
        SELECT * FROM events 
        WHERE date(startTime) <= date(:date) AND date(endTime) >= date(:date)
        ORDER BY startTime ASC
    """)
    fun getEventsForDate(date: LocalDateTime): Flow<List<EventEntity>>

    /**
     * 获取指定日期的可见事件
     */
    @Query("""
        SELECT * FROM events 
        WHERE (subscriptionId IS NULL OR subscriptionId IN (SELECT id FROM subscriptions WHERE isEnabled = 1))
        AND date(startTime) <= date(:date) AND date(endTime) >= date(:date)
        ORDER BY startTime ASC
    """)
    fun getVisibleEventsForDate(date: LocalDateTime): Flow<List<EventEntity>>

    /**
     * 获取有提醒的事件
     */
    @Query("SELECT * FROM events WHERE reminderMinutes IS NOT NULL AND startTime > :now ORDER BY startTime ASC")
    suspend fun getEventsWithReminder(now: LocalDateTime): List<EventEntity>

    /**
     * 搜索事件
     */
    @Query("""
        SELECT * FROM events 
        WHERE title LIKE '%' || :query || '%' 
        OR description LIKE '%' || :query || '%'
        OR location LIKE '%' || :query || '%'
        ORDER BY startTime DESC
    """)
    fun searchEvents(query: String): Flow<List<EventEntity>>

    /**
     * 插入事件
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity): Long

    /**
     * 批量插入事件
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<EventEntity>): List<Long>

    /**
     * 更新事件
     */
    @Update
    suspend fun updateEvent(event: EventEntity)

    /**
     * 删除事件
     */
    @Delete
    suspend fun deleteEvent(event: EventEntity)

    /**
     * 根据ID删除事件
     */
    @Query("DELETE FROM events WHERE id = :eventId")
    suspend fun deleteEventById(eventId: Long)

    /**
     * 删除所有事件
     */
    @Query("DELETE FROM events")
    suspend fun deleteAllEvents()

    /**
     * 获取事件数量
     */
    @Query("SELECT COUNT(*) FROM events")
    suspend fun getEventCount(): Int

    /**
     * 获取指定订阅的事件
     */
    @Query("SELECT * FROM events WHERE subscriptionId = :subscriptionId ORDER BY startTime ASC")
    fun getEventsBySubscription(subscriptionId: Long): Flow<List<EventEntity>>

    /**
     * 删除指定订阅的所有事件
     */
    @Query("DELETE FROM events WHERE subscriptionId = :subscriptionId")
    suspend fun deleteEventsBySubscription(subscriptionId: Long)

    /**
     * 获取启用的订阅ID列表
     */
    @Query("SELECT id FROM subscriptions WHERE isEnabled = 1")
    suspend fun getEnabledSubscriptionIds(): List<Long>

    /**
     * 获取可见事件（排除禁用订阅的事件）
     */
    @Query("""
        SELECT * FROM events 
        WHERE subscriptionId IS NULL 
        OR subscriptionId IN (SELECT id FROM subscriptions WHERE isEnabled = 1)
        ORDER BY startTime ASC
    """)
    fun getVisibleEvents(): Flow<List<EventEntity>>

    /**
     * 获取指定日期范围内的可见事件
     */
    @Query("""
        SELECT * FROM events 
        WHERE (subscriptionId IS NULL OR subscriptionId IN (SELECT id FROM subscriptions WHERE isEnabled = 1))
        AND (startTime >= :startDate AND startTime < :endDate 
        OR endTime >= :startDate AND endTime < :endDate
        OR startTime < :startDate AND endTime >= :endDate)
        ORDER BY startTime ASC
    """)
    fun getVisibleEventsBetween(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<EventEntity>>
}
