package com.hsk.dayflow.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.hsk.dayflow.feature.calendar.CalendarScreen
import com.hsk.dayflow.feature.event.EventScreen

object Routes {
    const val CALENDAR = "calendar"
    const val EVENT_DETAIL = "event/{eventId}"
    const val EVENT_ADD = "event/add?date={date}"
    const val SETTINGS = "settings"

    fun eventDetail(eventId: Long) = "event/$eventId"
    fun eventAdd(date: String? = null) = if (date != null) "event/add?date=$date" else "event/add"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    onMenuClick: () -> Unit
) {
    NavHost(navController = navController, startDestination = Routes.CALENDAR) {
        composable(Routes.CALENDAR) {
            CalendarScreen(
                onAddEvent = { navController.navigate(Routes.eventAdd()) },
                onEventClick = { event -> navController.navigate(Routes.eventDetail(event.id)) },
                onMenuClick = onMenuClick
            )
        }

        // 事件详情/编辑页面
        composable(
            route = Routes.EVENT_DETAIL,
            arguments = listOf(navArgument("eventId") { type = NavType.LongType })
        ) {
            EventScreen(onNavigateBack = { navController.popBackStack() })
        }

        // 新建事件页面
        composable(
            route = Routes.EVENT_ADD,
            arguments = listOf(
                navArgument("date") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            EventScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Routes.SETTINGS) {
            // SettingsScreen
        }
    }
}
