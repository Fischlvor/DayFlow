package com.hsk.dayflow.feature.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarViewDay
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hsk.dayflow.core.model.CalendarEvent
import com.hsk.dayflow.core.model.ViewType
import com.hsk.dayflow.feature.calendar.components.DayCalendar
import com.hsk.dayflow.feature.calendar.components.EventList
import com.hsk.dayflow.feature.calendar.components.MonthCalendar
import com.hsk.dayflow.feature.calendar.components.WeekCalendar
import com.hsk.dayflow.feature.calendar.components.YearCalendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onAddEvent: () -> Unit,
    onEventClick: (CalendarEvent) -> Unit,
    onMenuClick: () -> Unit,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val monthEvents by viewModel.monthEvents.collectAsState()
    val selectedDateEvents by viewModel.selectedDateEvents.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("DayFlow") },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "菜单")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.goToToday() }) {
                        Icon(Icons.Default.Today, contentDescription = "今天")
                    }
                    IconButton(onClick = { viewModel.switchViewType() }) {
                        Icon(
                            imageVector = when (uiState.viewType) {
                                ViewType.YEAR -> Icons.Default.DateRange
                                ViewType.MONTH -> Icons.Default.CalendarMonth
                                ViewType.WEEK -> Icons.Default.CalendarViewWeek
                                ViewType.DAY -> Icons.Default.CalendarViewDay
                            },
                            contentDescription = "切换视图: ${uiState.viewType.displayName}"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddEvent,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加日程", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            when (uiState.viewType) {
                ViewType.YEAR -> {
                    YearCalendar(
                        currentYear = uiState.currentMonth.year,
                        selectedDate = uiState.selectedDate,
                        events = monthEvents,
                        onMonthSelected = { yearMonth ->
                            viewModel.setMonth(yearMonth)
                            viewModel.setViewType(ViewType.MONTH)
                        },
                        onPreviousYear = { viewModel.previousYear() },
                        onNextYear = { viewModel.nextYear() },
                        onTodayClick = { viewModel.goToToday() },
                        modifier = Modifier.weight(1f)
                    )
                }

                ViewType.MONTH -> {
                    MonthCalendar(
                        currentMonth = uiState.currentMonth,
                        selectedDate = uiState.selectedDate,
                        events = monthEvents,
                        onDateSelected = { viewModel.selectDate(it) },
                        onMonthChanged = { viewModel.setMonth(it) },
                        onTodayClick = { viewModel.goToToday() },
                        onYearClick = { viewModel.setViewType(ViewType.YEAR) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    EventList(
                        date = uiState.selectedDate,
                        events = selectedDateEvents,
                        onEventClick = onEventClick,
                        modifier = Modifier.weight(1f)
                    )
                }

                ViewType.WEEK -> {
                    WeekCalendar(
                        selectedDate = uiState.selectedDate,
                        events = monthEvents,
                        onDateSelected = { viewModel.selectDate(it) },
                        onPreviousWeek = { viewModel.previousWeek() },
                        onNextWeek = { viewModel.nextWeek() },
                        onTodayClick = { viewModel.goToToday() },
                        modifier = Modifier.weight(1f)
                    )
                }

                ViewType.DAY -> {
                    DayCalendar(
                        selectedDate = uiState.selectedDate,
                        events = monthEvents,
                        onPreviousDay = { viewModel.previousDay() },
                        onNextDay = { viewModel.nextDay() },
                        onTodayClick = { viewModel.goToToday() },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
