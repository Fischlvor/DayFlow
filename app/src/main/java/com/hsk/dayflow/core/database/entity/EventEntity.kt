package com.hsk.dayflow.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.hsk.dayflow.core.model.CalendarEvent
import com.hsk.dayflow.core.model.EventColor
import com.hsk.dayflow.core.model.ReminderType
import java.time.LocalDateTime

/**
 * 日程事件数据库实体
 */
@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val isAllDay: Boolean = false,
    val location: String = "",
    val colorOrdinal: Int = EventColor.BLUE.ordinal,
    val reminderMinutes: Long? = null,
    val recurrenceRule: String? = null,
    val calendarId: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * 转换为领域模型
     */
    fun toCalendarEvent(): CalendarEvent {
        return CalendarEvent(
            id = id,
            title = title,
            description = description,
            startTime = startTime,
            endTime = endTime,
            isAllDay = isAllDay,
            location = location,
            color = EventColor.fromOrdinal(colorOrdinal),
            reminder = reminderMinutes?.let { ReminderType.fromMinutes(it) },
            recurrenceRule = recurrenceRule,
            calendarId = calendarId,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        /**
         * 从领域模型创建实体
         */
        fun fromCalendarEvent(event: CalendarEvent): EventEntity {
            return EventEntity(
                id = event.id,
                title = event.title,
                description = event.description,
                startTime = event.startTime,
                endTime = event.endTime,
                isAllDay = event.isAllDay,
                location = event.location,
                colorOrdinal = event.color.ordinal,
                reminderMinutes = event.reminder?.minutesBefore,
                recurrenceRule = event.recurrenceRule,
                calendarId = event.calendarId,
                createdAt = event.createdAt,
                updatedAt = event.updatedAt
            )
        }
    }
}
