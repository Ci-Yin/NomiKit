package com.ciyin.app.data.project

import ciyin.io.File
import com.ciyin.app.util.parseArgs
import com.ciyin.app.util.removeOuterQuotes
import com.ciyin.app.util.toArgs
import com.ciyin.app.util.toArgsStr


/**
 * 项目参数数据类，用于在启动项目时传递的参数信息。
 *
 * @property runtimeDataFile 在平台中运行的项目传入的配置文件。
 * @property args 原始命令行参数列表，用于脚本运行时的动态输入。
 */
data class ProjectArgs(
    val runtimeDataFile: File = File(""),
    val args: List<String> = listOf()
)

/**
 * 将 [ProjectArgs] 转换为 "--key=value" 的键值对参数列表。
 *
 * 通常用于命令行传参或通过 `ProcessBuilder` 启动进程时，
 * 以标准的 `--key=value` 形式传递。
 *
 * 生成格式如下：
 * ```
 * --runtimeDataFile=<绝对路径>
 * --args=<参数字符串>
 * ```
 *
 * @receiver 当前的 [ProjectArgs] 实例
 * @return 包含键值对格式参数的 [List]<String>
 *
 * @see parseArgs
 */
fun ProjectArgs.toKeyValueArgs(): List<String> = buildList {
    add("--${ProjectArgs::runtimeDataFile.name}=${runtimeDataFile.absolutePath}")
    if (args.isNotEmpty()) {
        add("--${ProjectArgs::args.name}=${args.toArgsStr()}")
    }
}

/**
 * 从命令行参数列表解析为 [ProjectArgs] 实例。
 *
 * 支持两种输入：
 * 1. 通过 `--key=value` 形式传入，例如：
 *    ```
 *    --runtimeDataFile="C:/scripts/config.json" --args="arg1","arg2","arg3"
 *    ```
 * 2. 从普通的 `List<String>` 参数中，通过 [parseArgs] 自动识别。
 *
 * @receiver 命令行参数列表
 * @return 解析得到的 [ProjectArgs] 实例
 *
 * @throws IllegalArgumentException 如果缺少必要参数或格式错误
 *
 * @see parseArgs
 */
fun List<String>.toProjectArgs(): ProjectArgs {
    val map = parseArgs(toTypedArray())
    val runtimeDataFileValue = map[ProjectArgs::runtimeDataFile.name]
        ?: error("缺少参数: --${ProjectArgs::runtimeDataFile.name}")

    return ProjectArgs(
        runtimeDataFile = File(runtimeDataFileValue.removeOuterQuotes()),
        args = map[ProjectArgs::args.name]?.toArgs() ?: emptyList()
    )
}

/**
 * 判断当前 [ProjectArgs] 是否包含有效的命令行参数。
 *
 * 可用于在脚本逻辑中快速判定是否存在额外的运行参数。
 *
 * @receiver 当前的 [ProjectArgs] 实例
 * @return 若 [args] 非空则返回 `true`，否则返回 `false`
 */
fun ProjectArgs.hasArgs(): Boolean = args.isNotEmpty()