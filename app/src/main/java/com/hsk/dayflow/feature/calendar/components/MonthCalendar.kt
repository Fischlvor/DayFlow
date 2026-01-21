package com.hsk.dayflow.feature.calendar.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hsk.dayflow.core.model.CalendarEvent
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MonthCalendar(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    events: List<CalendarEvent>,
    onDateSelected: (LocalDate) -> Unit,
    onMonthChanged: (YearMonth) -> Unit,
    onTodayClick: () -> Unit,
    onYearClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val monthFormatter = remember { DateTimeFormatter.ofPattern("yyyy年 M月", Locale.CHINESE) }

    // 支持前后100年滑动
    val startYear = remember { LocalDate.now().year - 100 }
    val totalMonths = 200 * 12

    // 计算当前月份对应的页码
    val initialPage = remember(currentMonth) {
        (currentMonth.year - startYear) * 12 + currentMonth.monthValue - 1
    }

    val pagerState = rememberPagerState(initialPage = initialPage) { totalMonths }

    // 当外部 currentMonth 变化时，同步 pager 位置
    LaunchedEffect(currentMonth) {
        val targetPage = (currentMonth.year - startYear) * 12 + currentMonth.monthValue - 1
        if (pagerState.currentPage != targetPage) {
            pagerState.scrollToPage(targetPage)
        }
    }

    // 监听用户手动滑动，更新当前月份（只在滑动停止后触发）
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { page ->
            val year = startYear + page / 12
            val month = page % 12 + 1
            val newMonth = YearMonth.of(year, month)
            if (newMonth != currentMonth) {
                onMonthChanged(newMonth)
            }
        }
    }

    // 计算当前显示的月份（基于 pager 当前页面，实时更新）
    val displayedMonth by remember(startYear) {
        derivedStateOf {
            val year = startYear + pagerState.currentPage / 12
            val month = pagerState.currentPage % 12 + 1
            YearMonth.of(year, month)
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // 月份导航栏
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左边 < 按钮：进入年视图（靠左，大一些）
            IconButton(
                onClick = onYearClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "年视图",
                    modifier = Modifier.size(28.dp)
                )
            }

            // 月份标题（使用 pager 当前显示的月份）
            Text(
                text = displayedMonth.format(monthFormatter),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.weight(1f))

            // 今天按钮
            TextButton(onClick = onTodayClick) { Text("今天") }
        }

        Spacer(modifier = Modifier.height(8.dp))
        WeekDayHeader(modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(modifier = Modifier.height(8.dp))

        // 可左右滑动的月份日历
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val year = startYear + page / 12
            val month = page % 12 + 1
            val pageMonth = YearMonth.of(year, month)

            MonthGrid(
                currentMonth = pageMonth,
                selectedDate = selectedDate,
                events = events,
                onDateSelected = onDateSelected,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
private fun MonthGrid(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    events: List<CalendarEvent>,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val daysInMonth = remember(currentMonth) { generateMonthDays(currentMonth) }
    val eventsByDate = remember(events) { events.groupBy { it.startTime.toLocalDate() } }

    Column(modifier = modifier) {
        daysInMonth.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                week.forEach { date ->
                    DayCell(
                        date = date,
                        isSelected = date == selectedDate,
                        isCurrentMonth = YearMonth.from(date) == currentMonth,
                        events = eventsByDate[date] ?: emptyList(),
                        onDateClick = onDateSelected,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

private fun generateMonthDays(yearMonth: YearMonth): List<LocalDate> {
    val firstDayOfMonth = yearMonth.atDay(1)
    val lastDayOfMonth = yearMonth.atEndOfMonth()
    val firstDayOfGrid = firstDayOfMonth.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val lastDayOfGrid = lastDayOfMonth.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

    val days = mutableListOf<LocalDate>()
    var currentDate = firstDayOfGrid
    while (!currentDate.isAfter(lastDayOfGrid)) {
        days.add(currentDate)
        currentDate = currentDate.plusDays(1)
    }
    return days
}
