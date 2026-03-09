package ciyin.parser.core.engine

/**
 * 解码 Unicode 转义序列的扩展函数
 */
fun String.decodeUnicode(): String {
    if (!contains("\\u")) return this

    val regex = Regex("\\\\u([0-9a-fA-F]{4})")
    return regex.replace(this) { matchResult ->
        val hexCode = matchResult.groupValues[1]
        val charCode = hexCode.toInt(16)
        charCode.toChar().toString()
    }
}

/**
 * 解码各种转义字符
 */
fun String.decodeEscapeChars(): String {
    return this
        .decodeUnicode() // 处理 Unicode 转义
        .replace("\\\"", "\"") // 处理双引号转义
        .replace("\\\\", "\\") // 处理反斜杠转义
        .replace("\\n", "\n") // 处理换行符转义
        .replace("\\r", "\r") // 处理回车符转义
        .replace("\\t", "\t") // 处理制表符转义
        .replace("\\/", "/") // 处理斜杠转义
}

/**
 * 解码 HTML 实体
 */
fun String.decodeHtmlEntities(): String {
    return this
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&amp;", "&")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace("&nbsp;", " ")
}

/**
 * 全面解码字符串
 */
fun String.decodeAll(): String {
    return this
        .decodeUnicode()
        .decodeEscapeChars()
        .decodeHtmlEntities()
}
