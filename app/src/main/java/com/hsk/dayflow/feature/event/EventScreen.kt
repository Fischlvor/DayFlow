package com.hsk.dayflow.feature.event

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hsk.dayflow.core.model.EventColor
import com.hsk.dayflow.core.model.ReminderType
import com.hsk.dayflow.feature.event.components.DateTimePickerDialog
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventScreen(
    onNavigateBack: () -> Unit,
    viewModel: EventViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    var showStartDateTimePicker by remember { mutableStateOf(false) }
    var showEndDateTimePicker by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showReminderPicker by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy年M月d日", Locale.CHINESE) }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    // 保存或删除成功后返回
    LaunchedEffect(uiState.isSaved, uiState.isDeleted) {
        if (uiState.isSaved || uiState.isDeleted) {
            onNavigateBack()
        }
    }

    // 错误提示
    uiState.errorMessage?.let { message ->
        LaunchedEffect(message) {
            // Snackbar 会自动显示
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.id > 0) "编辑日程" else "新建日程") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (uiState.id > 0) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "删除")
                        }
                    }
                    TextButton(
                        onClick = { viewModel.saveEvent() },
                        enabled = !uiState.isLoading
                    ) {
                        Text("保存")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            // 标题输入
            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.updateTitle(it) },
                label = { Text("标题") },
                placeholder = { Text("添加标题") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
                isError = uiState.errorMessage != null
            )

            // 全天开关
            EventSettingItem(
                icon = Icons.Default.Schedule,
                title = "全天",
                trailing = {
                    Switch(
                        checked = uiState.isAllDay,
                        onCheckedChange = { viewModel.updateAllDay(it) }
                    )
                }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // 开始时间
            EventSettingItem(
                icon = Icons.Default.PlayArrow,
                title = "开始",
                subtitle = buildString {
                    append(uiState.startDate.format(dateFormatter))
                    if (!uiState.isAllDay) {
                        append("  ")
                        append(uiState.startTime.format(timeFormatter))
                    }
                },
                onClick = { showStartDateTimePicker = true }
            )

            // 结束时间
            EventSettingItem(
                icon = Icons.Default.Stop,
                title = "结束",
                subtitle = buildString {
                    append(uiState.endDate.format(dateFormatter))
                    if (!uiState.isAllDay) {
                        append("  ")
                        append(uiState.endTime.format(timeFormatter))
                    }
                },
                onClick = { showEndDateTimePicker = true }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // 地点
            OutlinedTextField(
                value = uiState.location,
                onValueChange = { viewModel.updateLocation(it) },
                label = { Text("地点") },
                placeholder = { Text("添加地点") },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true
            )

            // 备注
            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.updateDescription(it) },
                label = { Text("备注") },
                placeholder = { Text("添加备注") },
                leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                minLines = 3,
                maxLines = 5
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // 颜色选择
            EventSettingItem(
                icon = Icons.Default.Palette,
                title = "颜色",
                trailing = {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(uiState.color.color)
                    )
                },
                onClick = { showColorPicker = true }
            )

            // 提醒
            EventSettingItem(
                icon = Icons.Default.Notifications,
                title = "提醒",
                subtitle = uiState.reminder.displayName,
                onClick = { showReminderPicker = true }
            )
        }
    }

    // 开始时间选择器
    if (showStartDateTimePicker) {
        DateTimePickerDialog(
            title = "选择开始时间",
            initialDate = uiState.startDate,
            initialTime = uiState.startTime,
            isAllDay = uiState.isAllDay,
            onConfirm = { date, time ->
                viewModel.updateStartDate(date)
                viewModel.updateStartTime(time)
                showStartDateTimePicker = false
            },
            onDismiss = { showStartDateTimePicker = false }
        )
    }

    // 结束时间选择器
    if (showEndDateTimePicker) {
        DateTimePickerDialog(
            title = "选择结束时间",
            initialDate = uiState.endDate,
            initialTime = uiState.endTime,
            isAllDay = uiState.isAllDay,
            onConfirm = { date, time ->
                viewModel.updateEndDate(date)
                viewModel.updateEndTime(time)
                showEndDateTimePicker = false
            },
            onDismiss = { showEndDateTimePicker = false }
        )
    }

    // 颜色选择器
    if (showColorPicker) {
        AlertDialog(
            onDismissRequest = { showColorPicker = false },
            title = { Text("选择颜色") },
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    EventColor.entries.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(color.color)
                                .clickable {
                                    viewModel.updateColor(color)
                                    showColorPicker = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (color == uiState.color) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // 提醒选择器
    if (showReminderPicker) {
        AlertDialog(
            onDismissRequest = { showReminderPicker = false },
            title = { Text("选择提醒时间") },
            text = {
                Column {
                    ReminderType.entries.forEach { reminder ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateReminder(reminder)
                                    showReminderPicker = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = reminder == uiState.reminder,
                                onClick = {
                                    viewModel.updateReminder(reminder)
                                    showReminderPicker = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(reminder.displayName)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // 删除确认对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除日程") },
            text = { Text("确定要删除这个日程吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteEvent()
                        showDeleteDialog = false
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun EventSettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        trailing?.invoke()
    }
}
