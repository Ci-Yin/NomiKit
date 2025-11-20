package com.ciyin.app.domain.script

import ciyin.platform.Log
import ciyin.system.coroutines.IO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch

/**
 * [ScriptManager] 是一个用于管理脚本的单例对象。
 * 它负责脚本的加载、执行和管理相关工作。
 */
object ScriptManager {

    /**
     * 日志标签，用于标识日志信息。
     */
    private const val TAG = "ScriptManager"

    /**
     * 用于管理协程生命周期的 [Job] 对象。
     */
    private lateinit var scriptJob: Job


    /**
     * 创建一个 [CoroutineScope]，使用 IO 调度器处理异步任务。
     */
    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * 运行脚本。
     * 如果当前有脚本正在运行，则先停止当前脚本。
     * 如果未连接到 WindowsDriver，则无法执行脚本。
     */
    fun runScript() {

        if (::scriptJob.isInitialized && scriptJob.isActive) {
            stopScript()
        }

        scriptJob = scope.launch {
            Log.info(TAG, "开始执行脚本")
            //windowBot.doScript()
            //doScript()
//            platform.runKotlinScript(ScriptProjectDir.concat("kts-test\\src\\main\\kotlin\\main.gradle.kts"))

            Log.info(TAG, "脚本执行完毕")
        }

    }

    /**
     * 停止当前正在运行的脚本。
     * 如果未连接到 WindowsDriver，则无法停止脚本。
     */
    fun stopScript() = scope.launch {
        Log.info(TAG, "已停止脚本")
        scriptJob.cancelAndJoin()
    }

}