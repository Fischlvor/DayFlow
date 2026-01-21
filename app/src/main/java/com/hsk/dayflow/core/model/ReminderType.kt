package com.hsk.dayflow.core.model

/**
 * 提醒类型枚举
 */
enum class ReminderType(
    val displayName: String,
    val minutesBefore: Long
) {
    NONE("不提醒", -1),
    AT_TIME("准时", 0),
    MINUTES_5("5分钟前", 5),
    MINUTES_10("10分钟前", 10),
    MINUTES_15("15分钟前", 15),
    MINUTES_30("30分钟前", 30),
    HOURS_1("1小时前", 60),
    HOURS_2("2小时前", 120),
    DAYS_1("1天前", 1440),
    DAYS_2("2天前", 2880);

    companion object {
        fun fromMinutes(minutes: Long): ReminderType? {
            return entries.find { it.minutesBefore == minutes }
        }
    }
}
