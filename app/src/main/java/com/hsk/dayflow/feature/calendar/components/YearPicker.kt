package com.hsk.dayflow.feature.calendar.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hsk.dayflow.core.model.CalendarEvent
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters

/**
 * 年度视图 - 全屏显示12个月的完整日历
 * 类似图片中的样式：3列4行，每个月显示完整日历网格
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun YearCalendar(
    currentYear: Int,
    selectedDate: LocalDate,
    events: List<CalendarEvent>,
    onMonthSelected: (YearMonth) -> Unit,
    onPreviousYear: () -> Unit,
    onNextYear: () -> Unit,
    onTodayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 支持前后50年滑动
    val yearRange = (currentYear - 50)..(currentYear + 50)
    val years = yearRange.toList()
    val initialPage = years.indexOf(currentYear)

    val pagerState = rememberPagerState(initialPage = initialPage) { years.size }
    val scope = rememberCoroutineScope()

    Column(modifier = modifier.fillMaxSize()) {
        // 年份标题栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${years[pagerState.currentPage]}年",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                // 农历信息（可选）
                Text(
                    text = "· 丙午马年 · 每月农历初一 ·",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(onClick = { /* 更多选项 */ }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "更多")
                }
            }
        }

        // 12个月网格 - 可左右滑动切换年份
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            YearMonthsGrid(
                year = years[page],
                selectedDate = selectedDate,
                events = events,
                onMonthClick = onMonthSelected
            )
        }
    }
}

/**
 * 年度12个月网格
 */
@Composable
private fun YearMonthsGrid(
    year: Int,
    selectedDate: LocalDate,
    events: List<CalendarEvent>,
    onMonthClick: (YearMonth) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 8.dp)
    ) {
        // 4行 x 3列
        for (row in 0..3) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (col in 0..2) {
                    val month = row * 3 + col + 1
                    MiniMonthCalendar(
                        yearMonth = YearMonth.of(year, month),
                        selectedDate = selectedDate,
                        events = events,
                        onMonthClick = onMonthClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * 迷你月历 - 显示完整的月份日历网格
 * 点击整个月份区域进入该月视图
 */
@Composable
private fun MiniMonthCalendar(
    yearMonth: YearMonth,
    selectedDate: LocalDate,
    events: List<CalendarEvent>,
    onMonthClick: (YearMonth) -> Unit,
    modifier: Modifier = Modifier
) {
    val isCurrentMonth = YearMonth.now() == yearMonth
    val days = remember(yearMonth) { generateMonthDaysForMini(yearMonth) }
    val weekDays = listOf("日", "一", "二", "三", "四", "五", "六")

    Column(
        modifier = modifier
            .padding(4.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onMonthClick(yearMonth) },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 月份标题
        Text(
            text = "${yearMonth.monthValue}月",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (isCurrentMonth) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        // 星期标题行
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            weekDays.forEach { day ->
                Text(
                    text = day,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // 日期网格（只显示，不可点击）
        days.chunked(7).forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                week.forEach { date ->
                    MiniDayCell(
                        date = date,
                        isCurrentMonth = date?.month == yearMonth.month,
                        isToday = date == LocalDate.now(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * 迷你日期单元格（只显示，不可点击）
 */
@Composable
private fun MiniDayCell(
    date: LocalDate?,
    isCurrentMonth: Boolean,
    isToday: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        if (date != null && isCurrentMonth) {
            // 今天的背景圆圈
            if (isToday) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                )
            }
            // 日期文字（移除字体内边距，确保垂直居中）
            Text(
                text = date.dayOfMonth.toString(),
                fontSize = 10.sp,
                lineHeight = 10.sp,
                color = when {
                    isToday -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
                },
                textAlign = TextAlign.Center,
                style = LocalTextStyle.current.copy(
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                )
            )
        }
    }
}

/**
 * 生成月份日期（周日开始）
 */
private fun generateMonthDaysForMini(yearMonth: YearMonth): List<LocalDate?> {
    val firstDayOfMonth = yearMonth.atDay(1)
    val lastDayOfMonth = yearMonth.atEndOfMonth()
    // 周日开始
    val firstDayOfGrid = firstDayOfMonth.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))

    val days = mutableListOf<LocalDate?>()
    var current = firstDayOfGrid

    // 生成6周的日期
    repeat(42) {
        days.add(current)
        current = current.plusDays(1)
    }

    return days
}

// 保留旧的对话框版本作为备用
@Composable
fun YearPickerDialog(
    currentMonth: YearMonth,
    events: List<CalendarEvent>,
    onMonthSelected: (YearMonth) -> Unit,
    onDismiss: () -> Unit
) {
    // 直接调用月份选择
    onMonthSelected(currentMonth)
    onDismiss()
}
