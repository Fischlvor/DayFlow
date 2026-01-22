# DayFlow 日历应用

<p align="center">
  <img src="docs/screenshots/calendar_main.jpg" width="200" alt="DayFlow 日历"/>
  <img src="docs/screenshots/event_edit.jpg" width="200" alt="日程编辑"/>
  <img src="docs/screenshots/notification.jpg" width="200" alt="通知提醒"/>
</p>

一款功能完善的 Android 日历应用，采用 Material Design 3 设计语言，支持农历、智能提醒、iCalendar 导入导出和网络日历订阅。

## ✨ 功能特性

### 📅 日历视图
- **月视图** - 整月日程概览
- **周视图** - 按周展示日程
- **日视图** - 详细单日日程
- **年视图** - 年度日历总览

### 📝 日程管理
- 创建、编辑、删除日程
- 全天事件支持
- 多种颜色标签分类
- 地点和备注信息

### 🔔 智能提醒
- 准时提醒
- 提前 5/10/15/30 分钟提醒
- 提前 1/2 小时、1 天、1 周提醒
- 设备重启后自动恢复提醒

### 🌙 农历支持
- 农历日期显示
- 传统节日标注（春节、中秋等）
- 二十四节气
- 干支纪年、生肖

### 📤 数据导入导出
- iCalendar (.ics) 格式支持
- 符合 RFC 5545 标准
- 可与 Google Calendar、Outlook 互通

### 🌐 网络日历订阅
- URL 订阅远程日历
- 手动/自动同步
- 订阅管理（启用/禁用/删除）

<p align="center">
  <img src="docs/screenshots/subscription.jpg" width="200" alt="日历订阅"/>
</p>

## 🛠️ 技术栈

| 类别 | 技术 |
|------|------|
| 语言 | Kotlin |
| UI | Jetpack Compose |
| 架构 | MVVM + Clean Architecture |
| 依赖注入 | Hilt |
| 数据库 | Room |
| 异步 | Coroutines + Flow |
| 导航 | Navigation Compose |

## 📁 项目结构

```
app/src/main/java/com/hsk/dayflow/
├── app/                    # 应用入口
│   ├── DayFlowApplication.kt
│   └── navigation/         # 导航配置
├── core/                   # 核心模块
│   ├── database/           # Room 数据库
│   ├── di/                 # Hilt 依赖注入
│   ├── model/              # 数据模型
│   ├── notification/       # 通知提醒
│   ├── icalendar/          # iCalendar 解析
│   └── lunar/              # 农历计算
├── feature/                # 功能模块
│   ├── calendar/           # 日历功能
│   ├── event/              # 日程管理
│   ├── settings/           # 设置
│   └── subscription/       # 订阅管理
└── MainActivity.kt
```

## 🚀 快速开始

### 环境要求

- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 17
- Android SDK 35
- Gradle 8.x

### 构建运行

```bash
# 克隆项目
git clone https://github.com/your-repo/DayFlow.git

# 打开 Android Studio
# File -> Open -> 选择 DayFlow 目录

# 运行
# 点击 Run 按钮或按 Shift+F10
```

### 生成 APK

```bash
# Debug 版本
./gradlew assembleDebug

# Release 版本（需要签名）
./gradlew assembleRelease
```

## 📱 系统要求

- Android 8.0 (API 26) 或更高版本
- 推荐 Android 12+ 以获得最佳体验

## 📄 许可证

MIT License

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

---

**DayFlow** - 让日程管理更简单 📅
