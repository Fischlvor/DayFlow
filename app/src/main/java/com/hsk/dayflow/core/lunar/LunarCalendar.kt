package com.hsk.dayflow.core.lunar

import java.time.LocalDate

/**
 * 农历日期数据类
 */
data class LunarDate(
    val year: Int,          // 农历年
    val month: Int,         // 农历月 (1-12)
    val day: Int,           // 农历日 (1-30)
    val isLeapMonth: Boolean = false,  // 是否闰月
    val yearGanZhi: String, // 年干支 (如：甲子)
    val monthGanZhi: String,// 月干支
    val dayGanZhi: String,  // 日干支
    val zodiac: String,     // 生肖
    val lunarMonthName: String, // 农历月名 (如：正月)
    val lunarDayName: String,   // 农历日名 (如：初一)
    val solarTerm: String? = null, // 节气 (如果当天是节气)
    val festival: String? = null   // 节日
)

/**
 * 农历计算器
 * 基于寿星万年历算法
 */
object LunarCalendar {

    // 天干
    private val TIAN_GAN = arrayOf("甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸")
    
    // 地支
    private val DI_ZHI = arrayOf("子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥")
    
    // 生肖
    private val ZODIAC = arrayOf("鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪")
    
    // 农历月份名称
    private val LUNAR_MONTH_NAMES = arrayOf(
        "正月", "二月", "三月", "四月", "五月", "六月",
        "七月", "八月", "九月", "十月", "冬月", "腊月"
    )
    
    // 农历日期名称
    private val LUNAR_DAY_NAMES = arrayOf(
        "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
        "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
        "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
    )

    // 二十四节气
    private val SOLAR_TERMS = arrayOf(
        "小寒", "大寒", "立春", "雨水", "惊蛰", "春分",
        "清明", "谷雨", "立夏", "小满", "芒种", "夏至",
        "小暑", "大暑", "立秋", "处暑", "白露", "秋分",
        "寒露", "霜降", "立冬", "小雪", "大雪", "冬至"
    )

    // 农历数据表 (1900-2100年)
    // 每年用一个整数表示：
    // - 低4位：闰月月份 (0表示无闰月)
    // - 第5位：闰月是大月(1)还是小月(0)
    // - 第6-17位：1-12月每月是大月(1)还是小月(0)
    private val LUNAR_INFO = intArrayOf(
        0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0, 0x055d2,
        0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0, 0x0ada2, 0x095b0, 0x14977,
        0x04970, 0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970,
        0x06566, 0x0d4a0, 0x0ea50, 0x06e95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950,
        0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557,
        0x06ca0, 0x0b550, 0x15355, 0x04da0, 0x0a5d0, 0x14573, 0x052d0, 0x0a9a8, 0x0e950, 0x06aa0,
        0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0,
        0x096d0, 0x04dd5, 0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b5a0, 0x195a6,
        0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50, 0x06d40, 0x0af46, 0x0ab60, 0x09570,
        0x04af5, 0x04970, 0x064b0, 0x074a3, 0x0ea50, 0x06b58, 0x055c0, 0x0ab60, 0x096d5, 0x092e0,
        0x0c960, 0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0, 0x092d0, 0x0cab5,
        0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9, 0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930,
        0x07954, 0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260, 0x0ea65, 0x0d530,
        0x05aa0, 0x076a3, 0x096d0, 0x04afb, 0x04ad0, 0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45,
        0x0b5a0, 0x056d0, 0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0,
        0x14b63, 0x09370, 0x049f8, 0x04970, 0x064b0, 0x168a6, 0x0ea50, 0x06b20, 0x1a6c4, 0x0aae0,
        0x0a2e0, 0x0d2e3, 0x0c960, 0x0d557, 0x0d4a0, 0x0da50, 0x05d55, 0x056a0, 0x0a6d0, 0x055d4,
        0x052d0, 0x0a9b8, 0x0a950, 0x0b4a0, 0x0b6a6, 0x0ad50, 0x055a0, 0x0aba4, 0x0a5b0, 0x052b0,
        0x0b273, 0x06930, 0x07337, 0x06aa0, 0x0ad50, 0x14b55, 0x04b60, 0x0a570, 0x054e4, 0x0d160,
        0x0e968, 0x0d520, 0x0daa0, 0x16aa6, 0x056d0, 0x04ae0, 0x0a9d4, 0x0a2d0, 0x0d150, 0x0f252,
        0x0d520
    )

    /**
     * 公历转农历
     */
    fun solarToLunar(date: LocalDate): LunarDate {
        val year = date.year
        val month = date.monthValue
        val day = date.dayOfMonth

        // 计算距离1900年1月31日(农历1900年正月初一)的天数
        val baseDate = LocalDate.of(1900, 1, 31)
        var offset = (date.toEpochDay() - baseDate.toEpochDay()).toInt()

        // 计算农历年
        var lunarYear = 1900
        var daysInYear: Int
        while (lunarYear < 2100) {
            daysInYear = getLunarYearDays(lunarYear)
            if (offset < daysInYear) break
            offset -= daysInYear
            lunarYear++
        }

        // 计算农历月和日
        val leapMonth = getLeapMonth(lunarYear)
        var lunarMonth = 1
        var isLeapMonth = false
        var daysInMonth: Int
        var isLeap = false

        for (m in 1..12) {
            // 先处理正常月份
            daysInMonth = getLunarMonthDays(lunarYear, m)
            if (offset < daysInMonth) {
                lunarMonth = m
                isLeapMonth = false
                break
            }
            offset -= daysInMonth

            // 如果这个月后面有闰月
            if (m == leapMonth) {
                daysInMonth = getLeapMonthDays(lunarYear)
                if (offset < daysInMonth) {
                    lunarMonth = m
                    isLeapMonth = true
                    break
                }
                offset -= daysInMonth
            }
        }

        val lunarDay = offset + 1

        // 计算干支
        val yearGanZhi = getYearGanZhi(lunarYear)
        val monthGanZhi = getMonthGanZhi(year, month)
        val dayGanZhi = getDayGanZhi(date)
        val zodiac = getZodiac(lunarYear)

        // 获取节气
        val solarTerm = getSolarTerm(date)

        // 获取节日
        val festival = getFestival(lunarMonth, lunarDay, isLeapMonth, month, day)

        return LunarDate(
            year = lunarYear,
            month = lunarMonth,
            day = lunarDay,
            isLeapMonth = isLeapMonth,
            yearGanZhi = yearGanZhi,
            monthGanZhi = monthGanZhi,
            dayGanZhi = dayGanZhi,
            zodiac = zodiac,
            lunarMonthName = if (isLeapMonth) "闰${LUNAR_MONTH_NAMES[lunarMonth - 1]}" else LUNAR_MONTH_NAMES[lunarMonth - 1],
            lunarDayName = LUNAR_DAY_NAMES[lunarDay - 1],
            solarTerm = solarTerm,
            festival = festival
        )
    }

    /**
     * 获取农历年的总天数
     */
    private fun getLunarYearDays(year: Int): Int {
        var sum = 348 // 12个月，每月29天
        val info = LUNAR_INFO[year - 1900]
        
        // 计算12个月中大月的天数
        var mask = 0x8000
        for (i in 0 until 12) {
            if (info and mask != 0) sum++
            mask = mask shr 1
        }
        
        // 加上闰月天数
        return sum + getLeapMonthDays(year)
    }

    /**
     * 获取农历某月的天数
     */
    private fun getLunarMonthDays(year: Int, month: Int): Int {
        val info = LUNAR_INFO[year - 1900]
        return if (info and (0x10000 shr month) != 0) 30 else 29
    }

    /**
     * 获取闰月月份 (0表示无闰月)
     */
    private fun getLeapMonth(year: Int): Int {
        return LUNAR_INFO[year - 1900] and 0xf
    }

    /**
     * 获取闰月天数
     */
    private fun getLeapMonthDays(year: Int): Int {
        val leapMonth = getLeapMonth(year)
        if (leapMonth == 0) return 0
        return if (LUNAR_INFO[year - 1900] and 0x10000 != 0) 30 else 29
    }

    /**
     * 获取年干支
     */
    private fun getYearGanZhi(year: Int): String {
        val ganIndex = (year - 4) % 10
        val zhiIndex = (year - 4) % 12
        return TIAN_GAN[ganIndex] + DI_ZHI[zhiIndex]
    }

    /**
     * 获取月干支
     */
    private fun getMonthGanZhi(year: Int, month: Int): String {
        val ganIndex = (year * 12 + month + 13) % 10
        val zhiIndex = (month + 1) % 12
        return TIAN_GAN[ganIndex] + DI_ZHI[zhiIndex]
    }

    /**
     * 获取日干支
     */
    private fun getDayGanZhi(date: LocalDate): String {
        val baseDate = LocalDate.of(1900, 1, 1)
        val days = (date.toEpochDay() - baseDate.toEpochDay()).toInt()
        val ganIndex = (days + 10) % 10
        val zhiIndex = (days + 12) % 12
        return TIAN_GAN[ganIndex] + DI_ZHI[zhiIndex]
    }

    /**
     * 获取生肖
     */
    private fun getZodiac(year: Int): String {
        return ZODIAC[(year - 4) % 12]
    }

    /**
     * 获取节气 (简化版)
     */
    private fun getSolarTerm(date: LocalDate): String? {
        // 简化的节气计算，实际应使用天文算法
        val termDays = arrayOf(
            intArrayOf(6, 20),   // 1月：小寒、大寒
            intArrayOf(4, 19),   // 2月：立春、雨水
            intArrayOf(6, 21),   // 3月：惊蛰、春分
            intArrayOf(5, 20),   // 4月：清明、谷雨
            intArrayOf(6, 21),   // 5月：立夏、小满
            intArrayOf(6, 21),   // 6月：芒种、夏至
            intArrayOf(7, 23),   // 7月：小暑、大暑
            intArrayOf(7, 23),   // 8月：立秋、处暑
            intArrayOf(8, 23),   // 9月：白露、秋分
            intArrayOf(8, 23),   // 10月：寒露、霜降
            intArrayOf(7, 22),   // 11月：立冬、小雪
            intArrayOf(7, 22)    // 12月：大雪、冬至
        )

        val month = date.monthValue
        val day = date.dayOfMonth
        val termIndex = (month - 1) * 2

        return when (day) {
            termDays[month - 1][0] -> SOLAR_TERMS[termIndex]
            termDays[month - 1][1] -> SOLAR_TERMS[termIndex + 1]
            else -> null
        }
    }

    /**
     * 获取节日
     */
    private fun getFestival(
        lunarMonth: Int,
        lunarDay: Int,
        isLeapMonth: Boolean,
        solarMonth: Int,
        solarDay: Int
    ): String? {
        // 公历节日
        val solarFestival = when {
            solarMonth == 1 && solarDay == 1 -> "元旦"
            solarMonth == 2 && solarDay == 14 -> "情人节"
            solarMonth == 3 && solarDay == 8 -> "妇女节"
            solarMonth == 4 && solarDay == 1 -> "愚人节"
            solarMonth == 5 && solarDay == 1 -> "劳动节"
            solarMonth == 5 && solarDay == 4 -> "青年节"
            solarMonth == 6 && solarDay == 1 -> "儿童节"
            solarMonth == 7 && solarDay == 1 -> "建党节"
            solarMonth == 8 && solarDay == 1 -> "建军节"
            solarMonth == 9 && solarDay == 10 -> "教师节"
            solarMonth == 10 && solarDay == 1 -> "国庆节"
            solarMonth == 12 && solarDay == 25 -> "圣诞节"
            else -> null
        }

        if (solarFestival != null) return solarFestival

        // 农历节日 (非闰月)
        if (!isLeapMonth) {
            return when {
                lunarMonth == 1 && lunarDay == 1 -> "春节"
                lunarMonth == 1 && lunarDay == 15 -> "元宵节"
                lunarMonth == 2 && lunarDay == 2 -> "龙抬头"
                lunarMonth == 5 && lunarDay == 5 -> "端午节"
                lunarMonth == 7 && lunarDay == 7 -> "七夕"
                lunarMonth == 7 && lunarDay == 15 -> "中元节"
                lunarMonth == 8 && lunarDay == 15 -> "中秋节"
                lunarMonth == 9 && lunarDay == 9 -> "重阳节"
                lunarMonth == 12 && lunarDay == 8 -> "腊八节"
                lunarMonth == 12 && lunarDay == 23 -> "小年"
                lunarMonth == 12 && lunarDay == 30 -> "除夕"
                else -> null
            }
        }

        return null
    }

    /**
     * 获取农历日期的简短显示文本
     */
    fun getLunarDayText(date: LocalDate): String {
        val lunar = solarToLunar(date)
        
        // 优先显示节日
        lunar.festival?.let { return it }
        
        // 其次显示节气
        lunar.solarTerm?.let { return it }
        
        // 初一显示月份
        if (lunar.day == 1) {
            return lunar.lunarMonthName
        }
        
        // 其他显示日期
        return lunar.lunarDayName
    }
}
