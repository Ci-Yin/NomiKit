package com.ciyin.app.data.project

import com.ciyin.app.util.parseArgs


/**
 * 表示脚本运行时的启动参数。
 *
 * @property driverPath 驱动程序可执行文件的路径（例如 `WindowsDriver.exe`）。
 * @property scriptProjectClass 要执行的脚本项目的主类。
 * @property platform 平台枚举的序号（例如 `Platform.Windows.ordinal`）。
 * @property args 项目参数，包含了脚本项目的绝对路径等信息。
 * @see ProjectArgs
 */
data class ScriptArgs(
    val driverPath: String,
    val scriptProjectClass: String,
    val platform: Int,
    val args: ProjectArgs = ProjectArgs(),
)

/**
 * 将 [ScriptArgs] 转换为 "--key=value" 的键值对参数形式。
 *
 * 通常用于传递给 `main(args: Array<String>)` 的参数，
 * 或在命令行中直接指定参数。例如：
 *
 * ```
 * val keyValueArgs = scriptArgs.toKeyValueArgs()
 * main(keyValueArgs.toTypedArray())
 * ```
 *
 * 参数格式：
 * ```
 * --driverPath=<路径>
 * --scriptProjectClass=<主类名>
 * --platform=<平台序号>
 * --runtimeDataFile=<文件路径>
 * --args=<以逗号分隔的附加参数>
 * ```
 *
 * @receiver 当前的 [ScriptArgs] 实例
 * @return 包含 "--key=value" 格式参数的 [List]<String>
 */
fun ScriptArgs.toKeyValueArgs(): List<String> {
    val result = mutableListOf<String>()
    result.add("--${ScriptArgs::driverPath.name}=${driverPath}")
    result.add("--${ScriptArgs::scriptProjectClass.name}=${scriptProjectClass}")
    result.add("--${ScriptArgs::platform.name}=${platform}")
    // 将 ProjectArgs 的参数展平到同一层级
    result.addAll(args.toKeyValueArgs())
    return result
}

/**
 * 从命令行参数数组解析为 [ScriptArgs]。
 *
 * 支持键值对形式：
 *    ```
 *    --driverPath=C:/Aibote/WindowsDriver.exe
 *    --scriptProjectClass=org.example.MyScript
 *    --platform=0
 *    --runtimeDataFile=C:/scripts/data.json
 *    --args="arg1","arg2","arg3"
 *    ```
 *
 * 内部优先尝试使用 [parseArgs] 解析带 `--` 前缀的参数。
 *
 * @receiver 命令行参数数组
 * @return 解析后的 [ScriptArgs]
 * @throws IllegalArgumentException 如果缺少必需字段或格式错误
 */
fun Array<String>.toScriptArgs(): ScriptArgs {
    val map = parseArgs(this)

    // 提取 ProjectArgs 相关的键
    val projectArgsKeys = listOf(
        ProjectArgs::runtimeDataFile.name,
        ProjectArgs::args.name
    )

    // 构建 ProjectArgs 的参数列表
    val projectArgsList = buildList {
        projectArgsKeys.forEach { key ->
            map[key]?.let { value ->
                add("--$key=$value")
            }
        }
    }

    return ScriptArgs(
        driverPath = map[ScriptArgs::driverPath.name]
            ?: error("缺少参数: --${ScriptArgs::driverPath.name}"),
        scriptProjectClass = map[ScriptArgs::scriptProjectClass.name]
            ?: error("缺少参数: --${ScriptArgs::scriptProjectClass.name}"),
        platform = map[ScriptArgs::platform.name]?.toIntOrNull()
            ?: error("缺少参数或格式错误: --${ScriptArgs::platform.name}"),
        args = projectArgsList.toProjectArgs()
    )
}