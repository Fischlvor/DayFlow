package com.hsk.dayflow.core.icalendar

import com.hsk.dayflow.core.model.CalendarEvent
import com.hsk.dayflow.core.model.EventColor
import com.hsk.dayflow.core.model.ReminderType
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * iCalendar 解析器
 * 解析 .ics 文件并转换为 CalendarEvent 列表
 */
@Singleton
class ICalendarParser @Inject constructor() {

    companion object {
        private val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss")
    }

    /**
     * 解析 iCalendar 输入流
     */
    fun parse(inputStream: InputStream): List<CalendarEvent> {
        val events = mutableListOf<CalendarEvent>()
        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
        
        var currentEvent: MutableMap<String, String>? = null
        var currentAlarm: MutableMap<String, String>? = null
        var inEvent = false
        var inAlarm = false

        reader.useLines { lines ->
            lines.forEach { line ->
                val trimmedLine = line.trim()
                
                when {
                    trimmedLine == "BEGIN:VEVENT" -> {
                        inEvent = true
                        currentEvent = mutableMapOf()
                    }
                    trimmedLine == "END:VEVENT" -> {
                        currentEvent?.let { eventMap ->
                            parseEventFromMap(eventMap, currentAlarm)?.let { events.add(it) }
                        }
                        inEvent = false
                        currentEvent = null
                        currentAlarm = null
                    }
                    trimmedLine == "BEGIN:VALARM" && inEvent -> {
                        inAlarm = true
                        currentAlarm = mutableMapOf()
                    }
                    trimmedLine == "END:VALARM" -> {
                        inAlarm = false
                    }
                    inAlarm && currentAlarm != null -> {
                        parseProperty(trimmedLine)?.let { (key, value) ->
                            currentAlarm!![key] = value
                        }
                    }
                    inEvent && currentEvent != null -> {
                        parseProperty(trimmedLine)?.let { (key, value) ->
                            currentEvent!![key] = value
                        }
                    }
                }
            }
        }

        return events
    }

    private fun parseProperty(line: String): Pair<String, String>? {
        val colonIndex = line.indexOf(':')
        if (colonIndex == -1) return null
        
        val key = line.substring(0, colonIndex).split(';').first()
        val value = line.substring(colonIndex + 1)
        return key to value
    }

    private fun parseEventFromMap(
        eventMap: Map<String, String>,
        alarmMap: Map<String, String>?
    ): CalendarEvent? {
        val summary = eventMap["SUMMARY"] ?: return null
        val dtStart = eventMap["DTSTART"] ?: return null
        
        val startTime = parseDateTime(dtStart) ?: return null
        val endTime = eventMap["DTEND"]?.let { parseDateTime(it) } ?: startTime.plusHours(1)
        
        val isAllDay = dtStart.length == 8
        val reminder = alarmMap?.get("TRIGGER")?.let { parseReminder(it) }

        return CalendarEvent(
            id = 0,
            title = summary.unescapeICalendar(),
            description = eventMap["DESCRIPTION"]?.unescapeICalendar() ?: "",
            location = eventMap["LOCATION"]?.unescapeICalendar() ?: "",
            startTime = startTime,
            endTime = endTime,
            isAllDay = isAllDay,
            color = EventColor.BLUE,
            reminder = reminder
        )
    }

    private fun parseDateTime(value: String): LocalDateTime? {
        return try {
            when {
                value.length == 8 -> {
                    LocalDateTime.parse(value + "T000000", DATE_TIME_FORMATTER)
                }
                value.contains("T") -> {
                    val cleanValue = value.removeSuffix("Z")
                    LocalDateTime.parse(cleanValue, DATE_TIME_FORMATTER)
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun parseReminder(trigger: String): ReminderType? {
        return when {
            trigger == "-PT0M" || trigger == "PT0M" -> ReminderType.AT_TIME
            trigger == "-PT5M" -> ReminderType.MINUTES_5
            trigger == "-PT10M" -> ReminderType.MINUTES_10
            trigger == "-PT15M" -> ReminderType.MINUTES_15
            trigger == "-PT30M" -> ReminderType.MINUTES_30
            trigger == "-PT1H" -> ReminderType.HOURS_1
            trigger == "-PT2H" -> ReminderType.HOURS_2
            trigger == "-P1D" -> ReminderType.DAYS_1
            trigger == "-P2D" -> ReminderType.DAYS_2
            else -> null
        }
    }

    private fun String.unescapeICalendar(): String {
        return this
            .replace("\\n", "\n")
            .replace("\\,", ",")
            .replace("\\;", ";")
            .replace("\\\\", "\\")
    }
}
