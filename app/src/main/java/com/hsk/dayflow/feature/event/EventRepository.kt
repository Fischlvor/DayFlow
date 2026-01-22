package com.hsk.dayflow.feature.event

import com.hsk.dayflow.core.database.dao.EventDao
import com.hsk.dayflow.core.database.entity.EventEntity
import com.hsk.dayflow.core.di.IoDispatcher
import com.hsk.dayflow.core.model.CalendarEvent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepository @Inject constructor(
    private val eventDao: EventDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    fun getAllEvents(): Flow<List<CalendarEvent>> =
        eventDao.getVisibleEvents().map { entities -> entities.map { it.toCalendarEvent() } }

    suspend fun getEventById(eventId: Long): CalendarEvent? = withContext(ioDispatcher) {
        eventDao.getEventById(eventId)?.toCalendarEvent()
    }

    fun getEventsForDate(date: LocalDate): Flow<List<CalendarEvent>> =
        eventDao.getVisibleEventsForDate(date.atStartOfDay()).map { it.map { e -> e.toCalendarEvent() } }

    fun getEventsBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<CalendarEvent>> =
        eventDao.getVisibleEventsBetween(startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX))
            .map { it.map { e -> e.toCalendarEvent() } }

    fun getEventsForMonth(year: Int, month: Int): Flow<List<CalendarEvent>> {
        val startDate = LocalDate.of(year, month, 1)
        val endDate = startDate.plusMonths(1).minusDays(1)
        return getEventsBetween(startDate, endDate)
    }

    fun searchEvents(query: String): Flow<List<CalendarEvent>> =
        eventDao.searchEvents(query).map { it.map { e -> e.toCalendarEvent() } }

    suspend fun insertEvent(event: CalendarEvent): Long = withContext(ioDispatcher) {
        eventDao.insertEvent(EventEntity.fromCalendarEvent(event))
    }

    suspend fun updateEvent(event: CalendarEvent) = withContext(ioDispatcher) {
        eventDao.updateEvent(EventEntity.fromCalendarEvent(event.copy(updatedAt = LocalDateTime.now())))
    }

    suspend fun deleteEvent(event: CalendarEvent) = withContext(ioDispatcher) {
        eventDao.deleteEvent(EventEntity.fromCalendarEvent(event))
    }

    suspend fun deleteEventById(eventId: Long) = withContext(ioDispatcher) {
        eventDao.deleteEventById(eventId)
    }
}
