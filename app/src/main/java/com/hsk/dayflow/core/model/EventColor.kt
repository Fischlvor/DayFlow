package com.hsk.dayflow.core.model

import androidx.compose.ui.graphics.Color

/**
 * 事件颜色枚举
 */
enum class EventColor(
    val displayName: String,
    val color: Color
) {
    RED("红色", Color(0xFFE53935)),
    PINK("粉色", Color(0xFFD81B60)),
    PURPLE("紫色", Color(0xFF8E24AA)),
    DEEP_PURPLE("深紫", Color(0xFF5E35B1)),
    INDIGO("靛蓝", Color(0xFF3949AB)),
    BLUE("蓝色", Color(0xFF1E88E5)),
    LIGHT_BLUE("浅蓝", Color(0xFF039BE5)),
    CYAN("青色", Color(0xFF00ACC1)),
    TEAL("蓝绿", Color(0xFF00897B)),
    GREEN("绿色", Color(0xFF43A047)),
    LIGHT_GREEN("浅绿", Color(0xFF7CB342)),
    LIME("青柠", Color(0xFFC0CA33)),
    YELLOW("黄色", Color(0xFFFDD835)),
    AMBER("琥珀", Color(0xFFFFB300)),
    ORANGE("橙色", Color(0xFFFB8C00)),
    DEEP_ORANGE("深橙", Color(0xFFF4511E)),
    BROWN("棕色", Color(0xFF6D4C41)),
    GREY("灰色", Color(0xFF757575));

    companion object {
        fun fromOrdinal(ordinal: Int): EventColor {
            return entries.getOrElse(ordinal) { BLUE }
        }
    }
}
