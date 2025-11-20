package com.ciyin.app.util

/**
 * 解析命令行参数，例如：
 * --mode=debug --threads=4 --verbose --output "C:/path/to/file"
 *
 * 支持两种形式：
 * 1. --key=value
 * 2. --flag  （值默认为 "true"）
 *
 * 注意：不会移除值中的引号，以支持如 --args="a","b","c" 这样的列表参数
 */
fun parseArgs(args: Array<String>): Map<String, String> {
    return buildMap {
        args.forEach { arg ->
            if (arg.startsWith("--")) {
                val clean = arg.removePrefix("--").trim()
                if ("=" in clean) {
                    val (key, value) = clean.split("=", limit = 2)
                    // 不移除引号，让调用者决定如何处理
                    put(key.trim(), value.trim())
                } else {
                    put(clean, "true")
                }
            }
        }
    }
}

/**
 * 移除字符串外层的成对引号（如果存在）
 * 例如: "hello" -> hello, 'hello' -> hello, hello -> hello
 */
internal fun String.removeOuterQuotes(): String {
    val trimmed = this.trim()
    return when {
        trimmed.length >= 2 && trimmed.startsWith('"') && trimmed.endsWith('"') ->
            trimmed.substring(1, trimmed.length - 1)

        trimmed.length >= 2 && trimmed.startsWith('\'') && trimmed.endsWith('\'') ->
            trimmed.substring(1, trimmed.length - 1)

        else -> trimmed
    }
}

/**
 * 将逗号分隔的参数字符串转换为列表
 * 支持带引号的参数，包括转义引号
 */
fun String?.toArgs(): List<String> {
    if (this.isNullOrBlank()) return emptyList()

    val result = mutableListOf<String>()
    val builder = StringBuilder()
    var inQuotes = false
    var i = 0

    while (i < this.length) {
        val char = this[i]

        when {
            // 处理转义字符
            char == '\\' && i + 1 < this.length -> {
                builder.append(this[i + 1])
                i += 2
            }
            // 处理引号
            char == '"' -> {
                inQuotes = !inQuotes
                i++
            }
            // 处理分隔符（只在引号外才分割）
            char == ',' && !inQuotes -> {
                val arg = builder.toString().trim()
                if (arg.isNotBlank()) {
                    result.add(arg)
                }
                builder.clear()
                i++
            }
            // 其他字符直接添加
            else -> {
                builder.append(char)
                i++
            }
        }
    }

    // 添加最后一个参数
    val lastArg = builder.toString().trim()
    if (lastArg.isNotBlank()) {
        result.add(lastArg)
    }

    return result
}

/**
 * 将参数列表转换为逗号分隔的字符串
 */
fun List<String>.toArgsStr(): String {
    if (this.isEmpty()) return ""
    return this.joinToString(",") {
        "\"${it.replace("\\", "\\\\").replace("\"", "\\\"")}\""
    }
}