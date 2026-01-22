package com.hsk.dayflow.feature.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hsk.dayflow.core.database.entity.SubscriptionEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubscriptionUiState(
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val message: String? = null,
    val showAddDialog: Boolean = false
)

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val repository: SubscriptionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubscriptionUiState())
    val uiState: StateFlow<SubscriptionUiState> = _uiState.asStateFlow()

    val subscriptions: StateFlow<List<SubscriptionEntity>> = repository.getAllSubscriptions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun showAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = true)
    }

    fun hideAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = false)
    }

    fun addSubscription(name: String, url: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, showAddDialog = false)
            
            repository.addSubscription(name, url)
                .onSuccess { id ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "订阅添加成功"
                    )
                    // 立即同步
                    syncSubscription(id)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "添加失败: ${error.message}"
                    )
                }
        }
    }

    fun deleteSubscription(id: Long) {
        viewModelScope.launch {
            repository.deleteSubscription(id)
            _uiState.value = _uiState.value.copy(message = "订阅已删除")
        }
    }

    fun toggleEnabled(id: Long, enabled: Boolean) {
        viewModelScope.launch {
            repository.setEnabled(id, enabled)
        }
    }

    fun syncSubscription(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true)
            
            repository.syncSubscription(id)
                .onSuccess { count ->
                    _uiState.value = _uiState.value.copy(
                        isSyncing = false,
                        message = "同步成功，导入 $count 个日程"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isSyncing = false,
                        message = "同步失败: ${error.message}"
                    )
                }
        }
    }

    fun syncAllSubscriptions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true)
            
            subscriptions.value.forEach { subscription ->
                if (subscription.isEnabled) {
                    repository.syncSubscription(subscription.id)
                }
            }
            
            _uiState.value = _uiState.value.copy(
                isSyncing = false,
                message = "全部同步完成"
            )
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}
