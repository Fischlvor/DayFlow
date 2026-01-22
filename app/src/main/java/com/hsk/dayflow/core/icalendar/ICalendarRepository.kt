package com.hsk.dayflow.core.icalendar

import android.content.Context
import android.net.Uri
import com.hsk.dayflow.core.di.IoDispatcher
import com.hsk.dayflow.core.model.CalendarEvent
import com.hsk.dayflow.feature.event.EventRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * iCalendar 仓库
 * 处理导入导出操作
 */
@Singleton
class ICalendarRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val parser: ICalendarParser,
    private val generator: ICalendarGenerator,
    private val eventRepository: EventRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    /**
     * 从 Uri 导入事件
     * @return 导入成功的事件数量
     */
    suspend fun importFromUri(uri: Uri): Result<Int> = withContext(ioDispatcher) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("无法打开文件"))

            val events = inputStream.use { parser.parse(it) }
            
            if (events.isEmpty()) {
                return@withContext Result.failure(Exception("未找到有效的日程"))
            }

            var importedCount = 0
            events.forEach { event ->
                eventRepository.insertEvent(event)
                importedCount++
            }

            Result.success(importedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 导出单个事件到 Uri
     */
    suspend fun exportToUri(event: CalendarEvent, uri: Uri): Result<Unit> = withContext(ioDispatcher) {
        try {
            val content = generator.generate(event)
            writeToUri(uri, content)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 导出多个事件到 Uri
     */
    suspend fun exportToUri(events: List<CalendarEvent>, uri: Uri): Result<Unit> = withContext(ioDispatcher) {
        try {
            val content = generator.generate(events)
            writeToUri(uri, content)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 生成事件的 iCalendar 内容（用于分享）
     */
    fun generateIcsContent(event: CalendarEvent): String {
        return generator.generate(event)
    }

    /**
     * 生成多个事件的 iCalendar 内容
     */
    fun generateIcsContent(events: List<CalendarEvent>): String {
        return generator.generate(events)
    }

    private fun writeToUri(uri: Uri, content: String) {
        val outputStream: OutputStream = context.contentResolver.openOutputStream(uri)
            ?: throw Exception("无法写入文件")
        
        outputStream.use { stream ->
            stream.write(content.toByteArray(Charsets.UTF_8))
        }
    }
}
