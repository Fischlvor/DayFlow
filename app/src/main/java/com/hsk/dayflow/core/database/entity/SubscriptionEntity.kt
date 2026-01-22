package com.hsk.dayflow.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * 日历订阅实体
 */
@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,                    // 订阅名称
    val url: String,                     // 订阅 URL
    val color: String = "BLUE",          // 显示颜色
    val isEnabled: Boolean = true,       // 是否启用
    val syncInterval: Int = 24,          // 同步间隔（小时）
    val lastSyncTime: LocalDateTime? = null,  // 上次同步时间
    val lastSyncStatus: String = "NEVER",     // 上次同步状态: NEVER, SUCCESS, FAILED
    val errorMessage: String? = null,    // 错误信息
    val createdAt: LocalDateTime = LocalDateTime.now()
)
