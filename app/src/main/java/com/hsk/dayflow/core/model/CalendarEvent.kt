package com.hsk.dayflow.core.model

import java.time.LocalDateTime

/**
 * 日程事件数据模型
 * 遵循 RFC 5545 iCalendar 规范设计
 */
data class CalendarEvent(
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val isAllDay: Boolean = false,
    val location: String = "",
    val color: EventColor = EventColor.BLUE,
    val reminder: ReminderType? = null,
    val recurrenceRule: String? = null,  // RFC 5545 RRULE
    val calendarId: String? = null,      // 订阅日历ID
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * 检查事件是否在指定日期
     */
    fun isOnDate(date: java.time.LocalDate): Boolean {
        val eventStartDate = startTime.toLocalDate()
        val eventEndDate = endTime.toLocalDate()
        return !date.isBefore(eventStartDate) && !date.isAfter(eventEndDate)
    }

    /**
     * 获取事件持续时间（分钟）
     */
    fun getDurationMinutes(): Long {
        return java.time.Duration.between(startTime, endTime).toMinutes()
    }
}
