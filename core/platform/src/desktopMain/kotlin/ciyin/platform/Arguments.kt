package ciyin.platform


data class AppArguments(
    val jarPath: String = "",
    val windowsDriverPath: String = "",
    val webDriverPath: String = "",
    val scriptProjectClass: String = "",
    val timing: Boolean = false
)

fun AppArguments.toProgramArgs(): List<String> {
    val args = mutableListOf<String>()
    if (jarPath.isNotEmpty()) {
        args += "${::jarPath.name}=$jarPath"
    }
    if (windowsDriverPath.isNotEmpty()) {
        args += "${::windowsDriverPath.name}=$windowsDriverPath"
    }
    if (webDriverPath.isNotEmpty()) {
        args += "${::webDriverPath.name}=$webDriverPath"
    }
    if (scriptProjectClass.isNotEmpty()) {
        args += "${::scriptProjectClass.name}=$scriptProjectClass"
    }
    if (timing) {
        args += "${::timing.name}=true"
    }
    return args
}

/**
 * 解析应用程序启动参数。
 *
 * 该方法接受一个字符串数组作为输入，每个元素代表一个命令行参数。参数格式支持键值对（key=value）和额外参数。
 * 键值对将被解析并存储在返回的 [AppArguments] 对象中，而额外参数则会被忽略。
 *
 * @param args 应用程序启动时传入的参数数组。
 * @return 包含解析后的参数的 [AppArguments] 对象。
 */
fun parseAppArguments(args: Array<String>): AppArguments {

    val keyValueMap = mutableMapOf<String, String>()
    val extras = mutableListOf<String>()

    args.forEach { arg ->
        val parts = arg.split("=", limit = 2)
        if (parts.size == 2) {
            keyValueMap[parts[0].trim()] = parts[1].trim()
        } else {
            extras += arg.trim()
        }
    }

    return AppArguments(
        jarPath = keyValueMap["jarPath"] ?: "",
        windowsDriverPath = keyValueMap["windowsDriverPath"] ?: "",
        scriptProjectClass = keyValueMap["scriptProjectClass"] ?: "",
        timing = keyValueMap["timing"]?.toBoolean() ?: false
    )

}
