package com.hsk.dayflow.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * DayFlow 应用程序类
 */
@HiltAndroidApp
class DayFlowApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // 初始化操作可以在这里进行
    }
}
