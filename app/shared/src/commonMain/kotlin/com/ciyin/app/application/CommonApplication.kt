package com.ciyin.app.application


import ciyin.platform.Context
import ciyin.platform.EmptyContext
import com.ciyin.app.di.KoinManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * 通用 `CommonApplication` 抽象类
 */
class CommonApplication(
    override val context: Context = EmptyContext,
) : MultiplatformApplication {

    /** 应用作用域，用于管理全局协程任务 */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** 应用初始化入口（类似 Android 的 onCreate） */
    override fun onCreate() {
        KoinManager.init()
    }

    /** 应用关闭时调用 */
    override fun onDestroy() {
        scope.cancel()
    }
}
