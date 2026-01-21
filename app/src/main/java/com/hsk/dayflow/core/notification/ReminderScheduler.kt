package com.hsk.dayflow.core.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.hsk.dayflow.core.model.CalendarEvent
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * 为事件设置提醒
     */
    fun scheduleReminder(event: CalendarEvent) {
        val reminder = event.reminder ?: return
        if (reminder.minutesBefore < 0) return // NONE

        val reminderTime = event.startTime.minusMinutes(reminder.minutesBefore)
        val triggerAtMillis = reminderTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        // 如果提醒时间已过，不设置
        if (triggerAtMillis <= System.currentTimeMillis()) return

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_EVENT_ID, event.id)
            putExtra(ReminderReceiver.EXTRA_EVENT_TITLE, event.title)
            putExtra(ReminderReceiver.EXTRA_EVENT_TIME, event.startTime.toString())
            putExtra(ReminderReceiver.EXTRA_EVENT_LOCATION, event.location)
            putExtra(ReminderReceiver.EXTRA_EVENT_IS_ALL_DAY, event.isAllDay)
            putExtra(ReminderReceiver.EXTRA_EVENT_COLOR, event.color.ordinal)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            event.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 设置精确闹钟
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                // 没有精确闹钟权限，使用非精确闹钟
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    /**
     * 取消事件提醒
     */
    fun cancelReminder(eventId: Long) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            eventId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    /**
     * 更新事件提醒
     */
    fun updateReminder(event: CalendarEvent) {
        cancelReminder(event.id)
        scheduleReminder(event)
    }
}
