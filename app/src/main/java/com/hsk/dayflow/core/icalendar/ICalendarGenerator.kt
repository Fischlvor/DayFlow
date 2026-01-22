package com.hsk.dayflow.core.icalendar

import com.hsk.dayflow.core.model.CalendarEvent
import com.hsk.dayflow.core.model.ReminderType
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * iCalendar 生成器
 * 将 CalendarEvent 转换为 .ics 格式
 */
@Singleton
class ICalendarGenerator @Inject constructor() {

    companion object {
        private val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss")
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd")
        private const val PRODID = "-//DayFlow//Calendar//CN"
    }

    /**
     * 生成单个事件的 iCalendar 内容
     */
    fun generate(event: CalendarEvent): String {
        return generate(listOf(event))
    }

    /**
     * 生成多个事件的 iCalendar 内容
     */
    fun generate(events: List<CalendarEvent>): String {
        val sb = StringBuilder()
        
        // 日历头部
        sb.appendLine("BEGIN:VCALENDAR")
        sb.appendLine("VERSION:2.0")
        sb.appendLine("PRODID:$PRODID")
        sb.appendLine("CALSCALE:GREGORIAN")
        sb.appendLine("METHOD:PUBLISH")

        // 事件列表
        events.forEach { event ->
            sb.append(generateEvent(event))
        }

        // 日历尾部
        sb.appendLine("END:VCALENDAR")

        return sb.toString()
    }

    private fun generateEvent(event: CalendarEvent): String {
        val sb = StringBuilder()
        
        sb.appendLine("BEGIN:VEVENT")
        
        // UID - 唯一标识符
        val uid = if (event.id > 0) "event-${event.id}@dayflow.app" else "${UUID.randomUUID()}@dayflow.app"
        sb.appendLine("UID:$uid")
        
        // 时间戳
        val now = java.time.LocalDateTime.now().format(DATE_TIME_FORMATTER)
        sb.appendLine("DTSTAMP:$now")
        
        // 开始和结束时间
        if (event.isAllDay) {
            sb.appendLine("DTSTART;VALUE=DATE:${event.startTime.format(DATE_FORMATTER)}")
            sb.appendLine("DTEND;VALUE=DATE:${event.endTime.plusDays(1).format(DATE_FORMATTER)}")
        } else {
            sb.appendLine("DTSTART:${event.startTime.format(DATE_TIME_FORMATTER)}")
            sb.appendLine("DTEND:${event.endTime.format(DATE_TIME_FORMATTER)}")
        }
        
        // 标题
        sb.appendLine("SUMMARY:${event.title.escapeICalendar()}")
        
        // 描述
        if (event.description.isNotBlank()) {
            sb.appendLine("DESCRIPTION:${event.description.escapeICalendar()}")
        }
        
        // 地点
        if (event.location.isNotBlank()) {
            sb.appendLine("LOCATION:${event.location.escapeICalendar()}")
        }
        
        // 提醒
        event.reminder?.let { reminder ->
            if (reminder != ReminderType.NONE) {
                sb.append(generateAlarm(reminder))
            }
        }
        
        sb.appendLine("END:VEVENT")
        
        return sb.toString()
    }

    private fun generateAlarm(reminder: ReminderType): String {
        val trigger = when (reminder) {
            ReminderType.AT_TIME -> "-PT0M"
            ReminderType.MINUTES_5 -> "-PT5M"
            ReminderType.MINUTES_10 -> "-PT10M"
            ReminderType.MINUTES_15 -> "-PT15M"
            ReminderType.MINUTES_30 -> "-PT30M"
            ReminderType.HOURS_1 -> "-PT1H"
            ReminderType.HOURS_2 -> "-PT2H"
            ReminderType.DAYS_1 -> "-P1D"
            ReminderType.DAYS_2 -> "-P2D"
            ReminderType.NONE -> return ""
        }

        val sb = StringBuilder()
        sb.appendLine("BEGIN:VALARM")
        sb.appendLine("TRIGGER:$trigger")
        sb.appendLine("ACTION:DISPLAY")
        sb.appendLine("DESCRIPTION:Reminder")
        sb.appendLine("END:VALARM")
        return sb.toString()
    }

    private fun String.escapeICalendar(): String {
        return this
            .replace("\\", "\\\\")
            .replace("\n", "\\n")
            .replace(",", "\\,")
            .replace(";", "\\;")
    }
}
