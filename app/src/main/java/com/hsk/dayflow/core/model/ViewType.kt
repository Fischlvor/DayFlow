package com.hsk.dayflow.core.model

/**
 * 日历视图类型枚举
 */
enum class ViewType(val displayName: String) {
    YEAR("年视图"),
    MONTH("月视图"),
    WEEK("周视图"),
    DAY("日视图");

    fun next(): ViewType {
        return when (this) {
            YEAR -> MONTH
            MONTH -> WEEK
            WEEK -> DAY
            DAY -> YEAR
        }
    }
}
