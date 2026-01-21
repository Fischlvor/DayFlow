package com.hsk.dayflow.feature.calendar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hsk.dayflow.core.common.DateUtils
import com.hsk.dayflow.core.model.CalendarEvent
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun WeekCalendar(
    selectedDate: LocalDate,
    events: List<CalendarEvent>,
    onDateSelected: (LocalDate) -> Unit,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onTodayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val weekStart = DateUtils.weekStart(selectedDate)
    val weekEnd = DateUtils.weekEnd(selectedDate)
    val weekDays = remember(weekStart) {
        (0..6).map { weekStart.plusDays(it.toLong()) }
    }
    val weekFormatter = remember { DateTimeFormatter.ofPattern("M月d日", Locale.CHINESE) }

    Column(modifier = modifier.fillMaxWidth()) {
        // 周导航栏
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousWeek) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "上一周")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${weekStart.format(weekFormatter)} - ${weekEnd.format(weekFormatter)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onTodayClick) { Text("今天") }
            }
            IconButton(onClick = onNextWeek) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "下一周")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 周日期选择
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            weekDays.forEach { date ->
                WeekDayItem(
                    date = date,
                    isSelected = date == selectedDate,
                    hasEvents = events.any { it.isOnDate(date) },
                    onClick = { onDateSelected(date) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()

        // 当天事件时间轴
        WeekDayTimeline(
            date = selectedDate,
            events = events.filter { it.isOnDate(selectedDate) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun WeekDayItem(
    date: LocalDate,
    isSelected: Boolean,
    hasEvents: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isToday = DateUtils.isToday(date)
    val isWeekend = DateUtils.isWeekend(date)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 星期
        Text(
            text = DateUtils.getWeekDayShortName(date.dayOfWeek),
            fontSize = 12.sp,
            color = if (isWeekend) MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
            else MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 日期
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isSelected -> MaterialTheme.colorScheme.primary
                        isToday -> MaterialTheme.colorScheme.primaryContainer
                        else -> Color.Transparent
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                fontSize = 14.sp,
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    isToday -> MaterialTheme.colorScheme.primary
                    isWeekend -> MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }

        // 事件指示点
        if (hasEvents) {
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
private fun WeekDayTimeline(
    date: LocalDate,
    events: List<CalendarEvent>,
    modifier: Modifier = Modifier
) {
    val hours = (0..23).toList()
    val eventsByHour = remember(events) {
        events.groupBy { it.startTime.hour }
    }

    LazyColumn(modifier = modifier.fillMaxWidth()) {
        items(hours) { hour ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.Top
            ) {
                // 时间标签
                Text(
                    text = String.format("%02d:00", hour),
                    modifier = Modifier.width(50.dp),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End
                )

                Spacer(modifier = Modifier.width(8.dp))

                // 时间线和事件
                Column(modifier = Modifier.weight(1f)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    eventsByHour[hour]?.forEach { event ->
                        TimelineEventItem(event = event)
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineEventItem(
    event: CalendarEvent,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(event.color.color.copy(alpha = 0.2f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(20.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(event.color.color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = event.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = DateUtils.formatTimeRange(event.startTime, event.endTime),
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
