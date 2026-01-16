package com.ciyin.app.application


import ciyin.application.MultiplatformApplication
import com.ciyin.app.di.initKoin

/**
 * 通用 `CommonApplication` 抽象类
 */
abstract class CommonApplication() : MultiplatformApplication {

    /** 应用初始化入口（类似 Android 的 onCreate） */
    override fun onCreate() {
        initKoin(context)
    }

    /** 应用关闭时调用 */
    override fun onDestroy() {

    }
}
