package com.hsk.dayflow.feature.event

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hsk.dayflow.core.model.CalendarEvent
import com.hsk.dayflow.core.model.EventColor
import com.hsk.dayflow.core.model.ReminderType
import com.hsk.dayflow.core.notification.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

data class EventUiState(
    val id: Long = 0,
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val startDate: LocalDate = LocalDate.now(),
    val startTime: LocalTime = LocalTime.of(9, 0),
    val endDate: LocalDate = LocalDate.now(),
    val endTime: LocalTime = LocalTime.of(10, 0),
    val isAllDay: Boolean = false,
    val color: EventColor = EventColor.BLUE,
    val reminder: ReminderType = ReminderType.MINUTES_30,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class EventViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val reminderScheduler: ReminderScheduler,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val eventId: Long = savedStateHandle.get<Long>("eventId") ?: 0L
    private val initialDate: String? = savedStateHandle.get<String>("date")

    private val _uiState = MutableStateFlow(EventUiState())
    val uiState: StateFlow<EventUiState> = _uiState.asStateFlow()

    init {
        if (eventId > 0) {
            loadEvent(eventId)
        } else if (initialDate != null) {
            val date = LocalDate.parse(initialDate)
            _uiState.value = _uiState.value.copy(
                startDate = date,
                endDate = date
            )
        }
    }

    private fun loadEvent(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            eventRepository.getEventById(id)?.let { event ->
                _uiState.value = _uiState.value.copy(
                    id = event.id,
                    title = event.title,
                    description = event.description,
                    location = event.location,
                    startDate = event.startTime.toLocalDate(),
                    startTime = event.startTime.toLocalTime(),
                    endDate = event.endTime.toLocalDate(),
                    endTime = event.endTime.toLocalTime(),
                    isAllDay = event.isAllDay,
                    color = event.color,
                    reminder = event.reminder ?: ReminderType.NONE,
                    isLoading = false
                )
            }
        }
    }

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun updateLocation(location: String) {
        _uiState.value = _uiState.value.copy(location = location)
    }

    fun updateStartDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(startDate = date)
        // 如果开始日期晚于结束日期，自动调整结束日期
        if (date.isAfter(_uiState.value.endDate)) {
            _uiState.value = _uiState.value.copy(endDate = date)
        }
        // 同一天时检查时间
        adjustEndTimeIfNeeded()
    }

    fun updateStartTime(time: LocalTime) {
        _uiState.value = _uiState.value.copy(startTime = time)
        // 同一天时检查时间
        adjustEndTimeIfNeeded()
    }

    fun updateEndDate(date: LocalDate) {
        val state = _uiState.value
        // 不允许结束日期早于开始日期
        val newEndDate = if (date.isBefore(state.startDate)) state.startDate else date
        _uiState.value = state.copy(endDate = newEndDate)
        // 同一天时检查时间
        adjustEndTimeIfNeeded()
    }

    fun updateEndTime(time: LocalTime) {
        val state = _uiState.value
        // 同一天时，不允许结束时间早于开始时间
        val newEndTime = if (state.startDate == state.endDate && time.isBefore(state.startTime)) {
            state.startTime.plusHours(1)
        } else {
            time
        }
        _uiState.value = state.copy(endTime = newEndTime)
    }

    /**
     * 如果开始和结束在同一天，确保结束时间不早于开始时间
     */
    private fun adjustEndTimeIfNeeded() {
        val state = _uiState.value
        if (state.startDate == state.endDate && state.endTime.isBefore(state.startTime)) {
            _uiState.value = state.copy(endTime = state.startTime.plusHours(1))
        }
    }

    fun updateAllDay(isAllDay: Boolean) {
        _uiState.value = _uiState.value.copy(isAllDay = isAllDay)
    }

    fun updateColor(color: EventColor) {
        _uiState.value = _uiState.value.copy(color = color)
    }

    fun updateReminder(reminder: ReminderType) {
        _uiState.value = _uiState.value.copy(reminder = reminder)
    }

    fun saveEvent() {
        val state = _uiState.value
        if (state.title.isBlank()) {
            _uiState.value = state.copy(errorMessage = "请输入标题")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val event = CalendarEvent(
                id = state.id,
                title = state.title.trim(),
                description = state.description.trim(),
                location = state.location.trim(),
                startTime = LocalDateTime.of(state.startDate, state.startTime),
                endTime = LocalDateTime.of(state.endDate, state.endTime),
                isAllDay = state.isAllDay,
                color = state.color,
                reminder = if (state.reminder == ReminderType.NONE) null else state.reminder
            )

            val savedEvent = if (state.id > 0) {
                eventRepository.updateEvent(event)
                event
            } else {
                val newId = eventRepository.insertEvent(event)
                event.copy(id = newId)
            }

            // 设置提醒
            if (savedEvent.reminder != null) {
                reminderScheduler.scheduleReminder(savedEvent)
            } else {
                reminderScheduler.cancelReminder(savedEvent.id)
            }

            _uiState.value = _uiState.value.copy(isLoading = false, isSaved = true)
        }
    }

    fun deleteEvent() {
        val state = _uiState.value
        if (state.id <= 0) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            // 取消提醒
            reminderScheduler.cancelReminder(state.id)
            eventRepository.deleteEventById(state.id)
            _uiState.value = _uiState.value.copy(isLoading = false, isDeleted = true)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
