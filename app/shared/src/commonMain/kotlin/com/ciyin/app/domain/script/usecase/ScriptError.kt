package com.ciyin.app.domain.script.usecase


/**
 *
 * kotlin类作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2025/11/4 15:59
 */
sealed class ScriptError(open val message: String) {
    data object JarNotExist : ScriptError("Jar文件不存在")
    data class WindowsDriverNotExist(override val message: String = "WindowsDriver文件不存在") :
        ScriptError(message)

    data object NotRunning : ScriptError("脚本未运行")
    data object Stop : ScriptError("脚本已停止")
}