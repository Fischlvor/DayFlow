package com.hsk.dayflow.feature.event.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

/**
 * 日期时间选择对话框
 * 日期和时间在同一行，分别点击选择
 */
@Composable
fun DateTimePickerDialog(
    title: String,
    initialDate: LocalDate,
    initialTime: LocalTime,
    isAllDay: Boolean,
    onConfirm: (LocalDate, LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedDate by remember { mutableStateOf(initialDate) }
    var selectedTime by remember { mutableStateOf(initialTime) }
    var showDatePicker by remember { mutableStateOf(true) }
    var currentMonth by remember { mutableStateOf(YearMonth.from(initialDate)) }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy年M月d日", Locale.CHINESE) }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 标题
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 日期时间选择行
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("开始", modifier = Modifier.width(48.dp))

                    // 日期按钮
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (showDatePicker) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { showDatePicker = true }
                    ) {
                        Text(
                            text = selectedDate.format(dateFormatter),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = if (showDatePicker) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // 时间按钮（非全天时显示）
                    if (!isAllDay) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (!showDatePicker) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { showDatePicker = false }
                        ) {
                            Text(
                                text = selectedTime.format(timeFormatter),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                color = if (!showDatePicker) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 选择器内容
                if (showDatePicker) {
                    // 日历选择器
                    CalendarPicker(
                        currentMonth = currentMonth,
                        selectedDate = selectedDate,
                        onDateSelected = { selectedDate = it },
                        onMonthChanged = { currentMonth = it }
                    )
                } else {
                    // 时间滚轮选择器
                    TimeWheelPicker(
                        selectedTime = selectedTime,
                        onTimeChanged = { selectedTime = it }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 底部按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    TextButton(onClick = { onConfirm(selectedDate, selectedTime) }) {
                        Text("确定")
                    }
                }
            }
        }
    }
}

/**
 * 日历选择器
 */
@Composable
private fun CalendarPicker(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onMonthChanged: (YearMonth) -> Unit
) {
    val monthFormatter = remember { DateTimeFormatter.ofPattern("yyyy年M月", Locale.CHINESE) }
    val weekDays = listOf("日", "一", "二", "三", "四", "五", "六")

    Column {
        // 月份导航
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onMonthChanged(currentMonth.minusMonths(1)) }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "上个月")
            }
            Text(
                text = currentMonth.format(monthFormatter),
                style = MaterialTheme.typography.titleSmall
            )
            IconButton(onClick = { onMonthChanged(currentMonth.plusMonths(1)) }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "下个月")
            }
        }

        // 星期标题
        Row(modifier = Modifier.fillMaxWidth()) {
            weekDays.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 日期网格
        val days = remember(currentMonth) { generateMonthDays(currentMonth) }
        days.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    val isCurrentMonth = date.month == currentMonth.month
                    val isSelected = date == selectedDate
                    val isToday = date == LocalDate.now()

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isSelected -> MaterialTheme.colorScheme.primary
                                    isToday -> MaterialTheme.colorScheme.primaryContainer
                                    else -> Color.Transparent
                                }
                            )
                            .clickable(enabled = isCurrentMonth) { onDateSelected(date) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = date.dayOfMonth.toString(),
                            fontSize = 14.sp,
                            color = when {
                                isSelected -> MaterialTheme.colorScheme.onPrimary
                                !isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                isToday -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 时间滚轮选择器 - 真正的滚动效果
 */
@Composable
private fun TimeWheelPicker(
    selectedTime: LocalTime,
    onTimeChanged: (LocalTime) -> Unit
) {
    var hour by remember { mutableIntStateOf(selectedTime.hour) }
    var minute by remember { mutableIntStateOf(selectedTime.minute) }

    LaunchedEffect(hour, minute) {
        onTimeChanged(LocalTime.of(hour, minute))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 小时选择
        ScrollWheelPicker(
            items = (0..23).toList(),
            selectedIndex = hour,
            onSelectedChange = { hour = it },
            modifier = Modifier.width(80.dp)
        )

        Text(
            text = ":",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        // 分钟选择
        ScrollWheelPicker(
            items = (0..59).toList(),
            selectedIndex = minute,
            onSelectedChange = { minute = it },
            modifier = Modifier.width(80.dp)
        )
    }
}

/**
 * 可滚动的滚轮选择器
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ScrollWheelPicker(
    items: List<Int>,
    selectedIndex: Int,
    onSelectedChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val itemHeightDp = 40.dp
    val visibleCount = 5
    val halfVisible = visibleCount / 2
    
    val density = LocalDensity.current
    val itemHeightPx = with(density) { itemHeightDp.toPx() }
    
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val coroutineScope = rememberCoroutineScope()

    // 监听滚动停止，更新选中项
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val firstVisible = listState.firstVisibleItemIndex
            val offset = listState.firstVisibleItemScrollOffset
            // 计算中间项
            val centerIndex = if (offset > itemHeightPx / 2) {
                firstVisible + 1
            } else {
                firstVisible
            }
            if (centerIndex in items.indices && centerIndex != selectedIndex) {
                onSelectedChange(centerIndex)
            }
        }
    }

    // 外部选中项变化时滚动到对应位置
    LaunchedEffect(selectedIndex) {
        if (listState.firstVisibleItemIndex != selectedIndex) {
            coroutineScope.launch {
                listState.animateScrollToItem(selectedIndex)
            }
        }
    }

    Box(
        modifier = modifier.height(itemHeightDp * visibleCount),
        contentAlignment = Alignment.Center
    ) {
        // 选中区域背景
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeightDp)
                .background(
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    RoundedCornerShape(8.dp)
                )
        )

        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            contentPadding = PaddingValues(vertical = itemHeightDp * halfVisible),
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(items.size) { index ->
                val isSelected = index == selectedIndex
                Box(
                    modifier = Modifier
                        .height(itemHeightDp)
                        .fillMaxWidth()
                        .clickable {
                            onSelectedChange(index)
                            coroutineScope.launch {
                                listState.animateScrollToItem(index)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = String.format("%02d", items[index]),
                        fontSize = if (isSelected) 24.sp else 16.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

private fun generateMonthDays(yearMonth: YearMonth): List<LocalDate> {
    val firstDayOfMonth = yearMonth.atDay(1)
    val firstDayOfGrid = firstDayOfMonth.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))

    return (0 until 42).map { firstDayOfGrid.plusDays(it.toLong()) }
}
