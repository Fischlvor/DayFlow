package com.hsk.dayflow.feature.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hsk.dayflow.core.model.CalendarEvent
import com.hsk.dayflow.core.model.ViewType
import com.hsk.dayflow.feature.event.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class CalendarUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val currentMonth: YearMonth = YearMonth.now(),
    val viewType: ViewType = ViewType.MONTH,
    val isLoading: Boolean = false
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    val monthEvents: StateFlow<List<CalendarEvent>> = _uiState
        .flatMapLatest { state ->
            eventRepository.getEventsForMonth(state.currentMonth.year, state.currentMonth.monthValue)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedDateEvents: StateFlow<List<CalendarEvent>> = _uiState
        .flatMapLatest { state -> eventRepository.getEventsForDate(state.selectedDate) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(selectedDate = date, currentMonth = YearMonth.from(date))
    }

    fun previousMonth() {
        _uiState.value = _uiState.value.copy(currentMonth = _uiState.value.currentMonth.minusMonths(1))
    }

    fun nextMonth() {
        _uiState.value = _uiState.value.copy(currentMonth = _uiState.value.currentMonth.plusMonths(1))
    }

    fun goToToday() {
        _uiState.value = _uiState.value.copy(selectedDate = LocalDate.now(), currentMonth = YearMonth.now())
    }

    fun switchViewType() {
        _uiState.value = _uiState.value.copy(viewType = _uiState.value.viewType.next())
    }

    fun setViewType(viewType: ViewType) {
        _uiState.value = _uiState.value.copy(viewType = viewType)
    }

    fun setMonth(yearMonth: YearMonth) {
        _uiState.value = _uiState.value.copy(currentMonth = yearMonth)
    }

    // 周视图导航
    fun previousWeek() {
        val newDate = _uiState.value.selectedDate.minusWeeks(1)
        _uiState.value = _uiState.value.copy(selectedDate = newDate, currentMonth = YearMonth.from(newDate))
    }

    fun nextWeek() {
        val newDate = _uiState.value.selectedDate.plusWeeks(1)
        _uiState.value = _uiState.value.copy(selectedDate = newDate, currentMonth = YearMonth.from(newDate))
    }

    // 日视图导航
    fun previousDay() {
        val newDate = _uiState.value.selectedDate.minusDays(1)
        _uiState.value = _uiState.value.copy(selectedDate = newDate, currentMonth = YearMonth.from(newDate))
    }

    fun nextDay() {
        val newDate = _uiState.value.selectedDate.plusDays(1)
        _uiState.value = _uiState.value.copy(selectedDate = newDate, currentMonth = YearMonth.from(newDate))
    }

    // 年视图导航
    fun previousYear() {
        val newDate = _uiState.value.selectedDate.minusYears(1)
        _uiState.value = _uiState.value.copy(selectedDate = newDate, currentMonth = YearMonth.from(newDate))
    }

    fun nextYear() {
        val newDate = _uiState.value.selectedDate.plusYears(1)
        _uiState.value = _uiState.value.copy(selectedDate = newDate, currentMonth = YearMonth.from(newDate))
    }
}
