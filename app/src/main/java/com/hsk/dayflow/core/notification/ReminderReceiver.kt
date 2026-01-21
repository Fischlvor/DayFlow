package com.hsk.dayflow.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hsk.dayflow.core.model.CalendarEvent
import com.hsk.dayflow.core.model.EventColor
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDateTime
import javax.inject.Inject

@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    companion object {
        const val EXTRA_EVENT_ID = "event_id"
        const val EXTRA_EVENT_TITLE = "event_title"
        const val EXTRA_EVENT_TIME = "event_time"
        const val EXTRA_EVENT_LOCATION = "event_location"
        const val EXTRA_EVENT_IS_ALL_DAY = "event_is_all_day"
        const val EXTRA_EVENT_COLOR = "event_color"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val eventId = intent.getLongExtra(EXTRA_EVENT_ID, 0)
        val title = intent.getStringExtra(EXTRA_EVENT_TITLE) ?: return
        val timeStr = intent.getStringExtra(EXTRA_EVENT_TIME) ?: return
        val location = intent.getStringExtra(EXTRA_EVENT_LOCATION) ?: ""
        val isAllDay = intent.getBooleanExtra(EXTRA_EVENT_IS_ALL_DAY, false)
        val colorOrdinal = intent.getIntExtra(EXTRA_EVENT_COLOR, EventColor.BLUE.ordinal)

        val startTime = LocalDateTime.parse(timeStr)
        val color = EventColor.fromOrdinal(colorOrdinal)

        val event = CalendarEvent(
            id = eventId,
            title = title,
            startTime = startTime,
            endTime = startTime.plusHours(1),
            location = location,
            isAllDay = isAllDay,
            color = color
        )

        notificationHelper.showEventReminder(event)
    }
}
