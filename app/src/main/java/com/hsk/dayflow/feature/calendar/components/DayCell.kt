package com.hsk.dayflow.feature.calendar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hsk.dayflow.core.common.DateUtils
import com.hsk.dayflow.core.model.CalendarEvent
import java.time.LocalDate

@Composable
fun DayCell(
    date: LocalDate,
    isSelected: Boolean,
    isCurrentMonth: Boolean,
    events: List<CalendarEvent>,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val isToday = DateUtils.isToday(date)
    val isWeekend = DateUtils.isWeekend(date)

    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        !isCurrentMonth -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        isToday -> MaterialTheme.colorScheme.primary
        isWeekend -> MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
        else -> MaterialTheme.colorScheme.onSurface
    }

    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        else -> Color.Transparent
    }

    Column(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable { onDateClick(date) }
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            color = textColor,
            fontSize = 14.sp,
            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(2.dp))
        if (events.isNotEmpty()) {
            EventIndicators(events = events.take(3))
        }
    }
}

@Composable
private fun EventIndicators(events: List<CalendarEvent>, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.Center) {
        events.forEach { event ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 1.dp)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(event.color.color)
            )
        }
    }
}

@Composable
fun WeekDayHeader(modifier: Modifier = Modifier) {
    val weekDays = listOf("一", "二", "三", "四", "五", "六", "日")
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        weekDays.forEachIndexed { index, day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                color = if (index >= 5) MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}
