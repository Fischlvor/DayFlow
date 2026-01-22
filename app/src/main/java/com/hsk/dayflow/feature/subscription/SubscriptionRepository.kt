package com.hsk.dayflow.feature.subscription

import android.content.Context
import com.hsk.dayflow.core.database.dao.EventDao
import com.hsk.dayflow.core.database.dao.SubscriptionDao
import com.hsk.dayflow.core.database.entity.SubscriptionEntity
import com.hsk.dayflow.core.di.IoDispatcher
import com.hsk.dayflow.core.icalendar.ICalendarParser
import com.hsk.dayflow.core.model.CalendarEvent
import com.hsk.dayflow.feature.event.EventRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val subscriptionDao: SubscriptionDao,
    private val eventDao: EventDao,
    private val eventRepository: EventRepository,
    private val iCalendarParser: ICalendarParser,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    fun getAllSubscriptions(): Flow<List<SubscriptionEntity>> {
        return subscriptionDao.getAllSubscriptions()
    }

    fun getEnabledSubscriptions(): Flow<List<SubscriptionEntity>> {
        return subscriptionDao.getEnabledSubscriptions()
    }

    suspend fun getSubscriptionById(id: Long): SubscriptionEntity? {
        return subscriptionDao.getSubscriptionById(id)
    }

    suspend fun addSubscription(name: String, url: String, color: String = "BLUE"): Result<Long> {
        return withContext(ioDispatcher) {
            try {
                // 检查 URL 是否已存在
                val existing = subscriptionDao.getSubscriptionByUrl(url)
                if (existing != null) {
                    return@withContext Result.failure(Exception("该订阅已存在"))
                }

                val subscription = SubscriptionEntity(
                    name = name,
                    url = url,
                    color = color
                )
                val id = subscriptionDao.insert(subscription)
                Result.success(id)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun updateSubscription(subscription: SubscriptionEntity) {
        subscriptionDao.update(subscription)
    }

    suspend fun deleteSubscription(id: Long) = withContext(ioDispatcher) {
        // 先删除该订阅关联的事件
        eventDao.deleteEventsBySubscription(id)
        // 再删除订阅本身
        subscriptionDao.deleteById(id)
    }

    suspend fun setEnabled(id: Long, enabled: Boolean) {
        subscriptionDao.setEnabled(id, enabled)
    }

    /**
     * 同步订阅
     */
    suspend fun syncSubscription(id: Long): Result<Int> = withContext(ioDispatcher) {
        val subscription = subscriptionDao.getSubscriptionById(id)
            ?: return@withContext Result.failure(Exception("订阅不存在"))

        try {
            // 下载 .ics 文件
            val events = downloadAndParseIcs(subscription.url)
            
            // 先删除该订阅的旧事件，避免重复
            eventDao.deleteEventsBySubscription(id)
            
            // 导入事件（设置 subscriptionId）
            var importedCount = 0
            events.forEach { event ->
                eventRepository.insertEvent(event.copy(subscriptionId = id))
                importedCount++
            }

            // 更新同步状态
            subscriptionDao.updateSyncStatus(
                id = id,
                time = LocalDateTime.now(),
                status = "SUCCESS",
                error = null
            )

            Result.success(importedCount)
        } catch (e: Exception) {
            // 更新失败状态
            subscriptionDao.updateSyncStatus(
                id = id,
                time = LocalDateTime.now(),
                status = "FAILED",
                error = e.message
            )
            Result.failure(e)
        }
    }

    /**
     * 同步所有启用的订阅
     */
    suspend fun syncAllEnabled(): Map<Long, Result<Int>> = withContext(ioDispatcher) {
        val results = mutableMapOf<Long, Result<Int>>()
        val subscriptions = subscriptionDao.getSubscriptionById(0) // 获取所有启用的订阅
        // TODO: 实现批量同步
        results
    }

    /**
     * 下载并解析 .ics 文件
     */
    private fun downloadAndParseIcs(urlString: String): List<CalendarEvent> {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.setRequestProperty("User-Agent", "DayFlow/1.0")

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw Exception("HTTP 错误: ${connection.responseCode}")
            }

            val inputStream = BufferedInputStream(connection.inputStream)
            return iCalendarParser.parse(inputStream)
        } finally {
            connection.disconnect()
        }
    }

    /**
     * 验证 URL 是否有效
     */
    suspend fun validateUrl(urlString: String): Result<String> = withContext(ioDispatcher) {
        try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            
            try {
                connection.requestMethod = "HEAD"
                connection.connectTimeout = 10000
                connection.setRequestProperty("User-Agent", "DayFlow/1.0")

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    Result.success("URL 有效")
                } else {
                    Result.failure(Exception("HTTP 错误: ${connection.responseCode}"))
                }
            } finally {
                connection.disconnect()
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
