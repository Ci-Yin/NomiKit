package com.ciyin.app.domain.script.usecase


/**
 *
 * kotlin类作用描述
 *
 * @author 次音(CiYin) QQ:2964221430
 * @github <a href="https://github.com/Ci-Yin">CiYin</a>
 * @since 2025/11/4 15:59
 * @version: 1.0
 */
sealed class ScriptError(open val message: String) {
    data object JarNotExist : ScriptError("Jar文件不存在")
    data class WindowsDriverNotExist(override val message: String = "WindowsDriver文件不存在") :
        ScriptError(message)

    data object NotRunning : ScriptError("脚本未运行")
    data object Stop : ScriptError("脚本已停止")
}