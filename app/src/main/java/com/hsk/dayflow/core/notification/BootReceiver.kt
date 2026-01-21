package com.hsk.dayflow.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hsk.dayflow.feature.event.EventRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 开机启动接收器
 * 设备重启后重新设置所有提醒
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var eventRepository: EventRepository

    @Inject
    lateinit var reminderScheduler: ReminderScheduler

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            rescheduleAllReminders()
        }
    }

    private fun rescheduleAllReminders() {
        scope.launch {
            val events = eventRepository.getAllEvents().first()
            events.forEach { event ->
                if (event.reminder != null) {
                    reminderScheduler.scheduleReminder(event)
                }
            }
        }
    }
}
