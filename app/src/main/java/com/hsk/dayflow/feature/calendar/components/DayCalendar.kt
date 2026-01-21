package com.hsk.dayflow.feature.calendar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun DayCalendar(
    selectedDate: LocalDate,
    events: List<CalendarEvent>,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onTodayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy年M月d日 E", Locale.CHINESE) }
    val dayEvents = remember(events, selectedDate) {
        events.filter { it.isOnDate(selectedDate) }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // 日期导航栏
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousDay) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "前一天")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = selectedDate.format(dateFormatter),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onTodayClick) { Text("今天") }
            }
            IconButton(onClick = onNextDay) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "后一天")
            }
        }

        // 今天标识
        if (DateUtils.isToday(selectedDate)) {
            Text(
                text = "今天",
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider()

        // 24小时时间轴
        DayTimeline(
            events = dayEvents,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DayTimeline(
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
                    .defaultMinSize(minHeight = 72.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.Top
            ) {
                // 时间标签
                Text(
                    text = String.format("%02d:00", hour),
                    modifier = Modifier
                        .width(56.dp)
                        .padding(top = 4.dp),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End
                )

                Spacer(modifier = Modifier.width(12.dp))

                // 时间线和事件
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 72.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    eventsByHour[hour]?.forEach { event ->
                        DayEventItem(event = event)
                    }
                }
            }
        }
    }
}

@Composable
private fun DayEventItem(
    event: CalendarEvent,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = event.color.color.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // 颜色指示条
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(event.color.color)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (event.isAllDay) "全天" else DateUtils.formatTimeRange(event.startTime, event.endTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (event.location.isNotBlank()) {
                    Text(
                        text = event.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
