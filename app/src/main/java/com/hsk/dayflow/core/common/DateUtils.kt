package com.hsk.dayflow.core.common

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

/**
 * 日期时间工具类
 */
object DateUtils {

    // 格式化器
    val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val DATE_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    val MONTH_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年MM月")
    val DAY_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("MM月dd日")
    val WEEK_DAY_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("E")

    /**
     * 获取今天的开始时间
     */
    fun todayStart(): LocalDateTime = LocalDate.now().atStartOfDay()

    /**
     * 获取今天的结束时间
     */
    fun todayEnd(): LocalDateTime = LocalDate.now().atTime(LocalTime.MAX)

    /**
     * 获取本周的开始日期（周一）
     */
    fun weekStart(date: LocalDate = LocalDate.now()): LocalDate {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    }

    /**
     * 获取本周的结束日期（周日）
     */
    fun weekEnd(date: LocalDate = LocalDate.now()): LocalDate {
        return date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
    }

    /**
     * 获取本月的开始日期
     */
    fun monthStart(date: LocalDate = LocalDate.now()): LocalDate {
        return date.with(TemporalAdjusters.firstDayOfMonth())
    }

    /**
     * 获取本月的结束日期
     */
    fun monthEnd(date: LocalDate = LocalDate.now()): LocalDate {
        return date.with(TemporalAdjusters.lastDayOfMonth())
    }

    /**
     * 判断是否是今天
     */
    fun isToday(date: LocalDate): Boolean = date == LocalDate.now()

    /**
     * 判断是否是周末
     */
    fun isWeekend(date: LocalDate): Boolean {
        return date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY
    }

    /**
     * 格式化日期为友好显示
     */
    fun formatFriendly(date: LocalDate): String {
        val today = LocalDate.now()
        return when {
            date == today -> "今天"
            date == today.minusDays(1) -> "昨天"
            date == today.plusDays(1) -> "明天"
            else -> date.format(DAY_FORMATTER)
        }
    }

    /**
     * 格式化时间范围
     */
    fun formatTimeRange(start: LocalDateTime, end: LocalDateTime): String {
        val startTime = start.format(TIME_FORMATTER)
        val endTime = end.format(TIME_FORMATTER)
        return "$startTime - $endTime"
    }

    /**
     * 获取星期几的中文名称
     */
    fun getWeekDayName(dayOfWeek: DayOfWeek): String {
        return when (dayOfWeek) {
            DayOfWeek.MONDAY -> "周一"
            DayOfWeek.TUESDAY -> "周二"
            DayOfWeek.WEDNESDAY -> "周三"
            DayOfWeek.THURSDAY -> "周四"
            DayOfWeek.FRIDAY -> "周五"
            DayOfWeek.SATURDAY -> "周六"
            DayOfWeek.SUNDAY -> "周日"
        }
    }

    /**
     * 获取星期几的短名称
     */
    fun getWeekDayShortName(dayOfWeek: DayOfWeek): String {
        return when (dayOfWeek) {
            DayOfWeek.MONDAY -> "一"
            DayOfWeek.TUESDAY -> "二"
            DayOfWeek.WEDNESDAY -> "三"
            DayOfWeek.THURSDAY -> "四"
            DayOfWeek.FRIDAY -> "五"
            DayOfWeek.SATURDAY -> "六"
            DayOfWeek.SUNDAY -> "日"
        }
    }
}
