package com.hsk.dayflow.feature.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hsk.dayflow.core.icalendar.ICalendarRepository
import com.hsk.dayflow.feature.event.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val importedCount: Int = 0,
    val exportedCount: Int = 0
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val iCalendarRepository: ICalendarRepository,
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    /**
     * 导入 .ics 文件
     */
    fun importFromUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, message = null)
            
            iCalendarRepository.importFromUri(uri)
                .onSuccess { count ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "成功导入 $count 个日程",
                        importedCount = count
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "导入失败: ${error.message}"
                    )
                }
        }
    }

    /**
     * 导出所有事件到 .ics 文件
     */
    fun exportAllToUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, message = null)
            
            try {
                val events = eventRepository.getAllEvents().first()
                
                if (events.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "没有日程可导出"
                    )
                    return@launch
                }

                iCalendarRepository.exportToUri(events, uri)
                    .onSuccess {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            message = "成功导出 ${events.size} 个日程",
                            exportedCount = events.size
                        )
                    }
                    .onFailure { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            message = "导出失败: ${error.message}"
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "导出失败: ${e.message}"
                )
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}
