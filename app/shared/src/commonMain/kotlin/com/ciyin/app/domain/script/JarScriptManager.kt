package com.ciyin.app.domain.script

import ciyin.jar.JarScript
import ciyin.jar.jarScript
import com.ciyin.app.domain.script.JarScriptManager.destroy
import com.ciyin.app.domain.script.JarScriptManager.run
import com.ciyin.app.domain.script.JarScriptManager.wait
import com.ciyin.app.domain.script.model.ScriptState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * `JarScriptManager` 类用于管理和执行 `JAR` 文件。它提供运行、销毁以及等待 `JAR` 进程结束的功能。
 *
 * 使用该类可以方便地启动外部 `JAR` 文件，并通过控制 [Process] 对象来管理其生命周期。
 * 本类还提供了日志记录功能，帮助跟踪 `JAR` 文件的执行状态。
 *
 * @see run 启动指定路径下的 `JAR` 文件。
 * @see destroy 强制终止当前正在运行的 `JAR` 进程。
 * @see wait 暂停当前协程直到 `JAR` 进程退出，并可选地执行一个回调函数。
 */
object JarScriptManager {

    private val jarScriptMap = mutableMapOf<String, JarScript>()

    private val _state = MutableStateFlow(ScriptState())
    val state = _state.asStateFlow()

    fun run(jarPath: String, java: String = "java", args: List<String> = emptyList()): JarScript {
        destroy(jarPath)
        return jarScript().apply {
            jarScriptMap[jarPath] = this
            run(jarPath, java, args)
            _state.update { it.copy(isRunning = true) }
        }
    }

    suspend fun wait(jarPath: String, name: String = "JarScript", function: () -> Unit = {}) {
        jarScriptMap[jarPath]?.wait(name, function)
    }

    fun destroyAll() {
        jarScriptMap.values.forEach { it.destroy() }
        jarScriptMap.clear()
        _state.update { it.copy(isRunning = false) }
    }

    fun destroy(jarPath: String) {
        jarScriptMap[jarPath]?.destroy()
        jarScriptMap.remove(jarPath)
        _state.update { it.copy(isRunning = jarScriptMap.isNotEmpty()) }
    }

}

