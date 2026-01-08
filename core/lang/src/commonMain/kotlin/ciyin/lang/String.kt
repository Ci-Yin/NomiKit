package ciyin.lang

import org.intellij.lang.annotations.Language
import kotlin.math.pow
import kotlin.math.round

/**
 *
 * 字符串处理相关扩展函数
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2024/8/24 下午11:15
 */

/**
 * 格式化浮点数，保留指定精度的小数位
 *
 * @param value 要格式化的浮点数
 * @param precision 保留的小数位数
 * @return 格式化后的字符串
 */
private fun formatDoubleWithPrecision(value: Double, precision: Int): String {
    if (precision <= 0) {
        return value.toInt().toString()
    }
    val factor = 10.0.pow(precision)
    val rounded = round(value * factor) / factor

    // 转换为字符串并确保有足够的小数位
    val str = rounded.toString()
    val dotIndex = str.indexOf('.')

    return if (dotIndex == -1) {
        // 没有小数点，添加小数点和零
        "$str.${"0".repeat(precision)}"
    } else {
        val decimalPart = str.substring(dotIndex + 1)
        val integerPart = str.substring(0, dotIndex)
        if (decimalPart.length < precision) {
            // 小数位不足，补零
            "$integerPart.$decimalPart${"0".repeat(precision - decimalPart.length)}"
        } else if (decimalPart.length > precision) {
            // 小数位过多，截断（理论上不应该发生，因为已经四舍五入）
            "$integerPart.${decimalPart.substring(0, precision)}"
        } else {
            str
        }
    }
}

/**
 * Uses this string as a format string and returns a string obtained
 * by substituting format specifiers in the format string with the provided arguments,
 * using the default locale.
 *
 * Supported format specifiers:
 * - `%s` - String substitution
 * - `%d` - Integer substitution
 * - `%f` - Float/Double substitution
 * - `%%` - Literal percent sign
 *
 * @param args Arguments to be substituted into the format string
 * @return Formatted string with arguments substituted
 *
 * @sample
 * ```
 * val result = "Hello %s, you have %d items".format("World", 5)
 * // result: "Hello World, you have 5 items"
 * ```
 */
fun String.format(vararg args: Any?): String = String.format(this, *args)

/**
 * Uses the provided [format] as a format string and returns a string obtained
 * by substituting format specifiers in the format string with the provided arguments,
 * using the default locale.
 *
 * Supported format specifiers:
 * - `%s` - String substitution
 * - `%d` - Integer substitution
 * - `%f` - Float/Double substitution
 * - `%%` - Literal percent sign
 *
 * @param format The format string containing format specifiers
 * @param args Arguments to be substituted into the format string
 * @return Formatted string with arguments substituted
 *
 * @sample
 * ```
 * val result = String.format("Price: %.2f, Quantity: %d", 19.99, 3)
 * // result: "Price: 19.99, Quantity: 3"
 * ```
 */
fun String.Companion.format(format: String, vararg args: Any?): String {
    var result = format
    var argIndex = 0

    // 支持 %s, %d, %f, %% 等
    // 匹配 %s, %d, %f, %% 或带精度的 %f (如 %.2f)
    val regex = Regex("""%(%|\.\d+f|[sdf])""")
    result = regex.replace(result) { matchResult ->
        val specifier = matchResult.value
        when {
            specifier == "%%" -> "%"
            specifier == "%s" -> {
                args.getOrNull(argIndex++)?.toString() ?: "null"
            }

            specifier == "%d" -> {
                args.getOrNull(argIndex++)?.toString()?.toIntOrNull()?.toString() ?: "0"
            }

            specifier.matches(Regex("%\\.\\d+f")) -> {
                // 处理带精度的浮点数格式，如 %.2f
                val precision = specifier.substring(2, specifier.length - 1).toIntOrNull() ?: 2
                val value = args.getOrNull(argIndex++)?.toString()?.toDoubleOrNull() ?: 0.0
                // 手动格式化浮点数精度，避免使用 Java 特定的 API
                formatDoubleWithPrecision(value, precision)
            }

            specifier == "%f" -> {
                args.getOrNull(argIndex++)?.toString()?.toDoubleOrNull()?.toString() ?: "0.0"
            }

            else -> matchResult.value
        }
    }
    return result
}

/**
 * 以正则表达式匹配内容文本
 * 只会输出第一个匹配的组（如果有捕获组则返回第一个捕获组，否则返回整个匹配）
 *
 * @param regex 正则表达式
 * @return 匹配的字符串，如果没有匹配则返回空字符串
 *
 * @sample
 * ```
 * "Hello123World".match("\\d+") // "123"
 * "Hello World".match("(\\w+) (\\w+)") // "Hello"
 * "test".match("xyz") // ""
 * ```
 */
fun CharSequence.match(@Language("REGEXP") regex: String): String {
    val group = matchGroup(regex)
    if (group.isNotEmpty()) {
        // 如果只有一个组（整个匹配），返回它；否则返回第一个捕获组（索引1）
        return if (group.size == 1) group.first() else group[1]
    }
    return ""
}

/**
 * 在字符串中查找正则表达式模式的匹配项
 *
 * @param pattern 正则表达式模式，用于匹配字符串
 * @return 如果字符串中包含与模式匹配的子字符串，则返回true；否则返回false
 *
 * @sample
 * ```
 * "Hello123".matchIn("\\d+") // true
 * "Hello".matchIn("\\d+") // false
 * "test@example.com".matchIn("@") // true
 * ```
 */
fun CharSequence.matchIn(@Language("REGEXP") pattern: String): Boolean {
    return Regex(pattern).containsMatchIn(this)
}

/**
 * 在字符串中查找正则表达式模式的匹配项，并返回匹配的组
 * 第一个元素是整个匹配，后续元素是捕获组
 *
 * @param pattern 正则表达式模式，用于匹配字符串
 * @return 包含匹配组的列表，第一个元素是整个匹配，后续是捕获组。如果无匹配，则返回空列表
 *
 * @sample
 * ```
 * "Hello World".matchGroup("(\\w+) (\\w+)") // ["Hello World", "Hello", "World"]
 * "123".matchGroup("\\d+") // ["123"]
 * "test".matchGroup("xyz") // []
 * ```
 */
fun CharSequence.matchGroup(@Language("REGEXP") pattern: String): List<String> {
    val matchResult = Regex(pattern).find(this)
    return matchResult?.groupValues ?: emptyList()
}

/**
 * 检查字符串是否包含中文字符
 * 包括基本汉字、扩展汉字、CJK统一汉字等Unicode范围
 *
 * @return 如果字符串中至少包含一个中文字符，则返回true；否则返回false
 *
 * @sample
 * ```
 * "你好".isChinese() // true
 * "Hello".isChinese() // false
 * "Hello你好".isChinese() // true
 * ```
 */
fun CharSequence.isChinese(): Boolean {
    // 匹配中文字符的Unicode范围：
    // \u4E00-\u9FFF: CJK统一汉字
    // \u3400-\u4DBF: CJK扩展A
    // \u20000-\u2A6DF: CJK扩展B
    // \u2A700-\u2B73F: CJK扩展C
    // \u2B740-\u2B81F: CJK扩展D
    // \u2B820-\u2CEAF: CJK扩展E
    // \uF900-\uFAFF: CJK兼容汉字
    // \u3300-\u33FF: CJK兼容
    return Regex("""[\u4E00-\u9FFF\u3400-\u4DBF\uF900-\uFAFF\u3300-\u33FF]""").containsMatchIn(this)
}

/**
 * 使用正则表达式检查当前字符序列是否包含匹配的子序列，
 * 如果包含则返回当前字符序列的字符串表示，否则返回默认值。
 *
 * @param regex 正则表达式对象，用于匹配
 * @param default 默认值提供者，如果当前字符序列不匹配[regex]，则调用此函数获取默认值
 * @return 当前字符序列的字符串表示，或者默认值
 *
 * @sample
 * ```
 * "hello123".containsOrDefault(Regex("\\d+")) { "no match" } // "hello123"
 * "hello".containsOrDefault(Regex("\\d+")) { "no match" } // "no match"
 * ```
 */
fun CharSequence.containsOrDefault(regex: Regex, default: () -> String): String {
    return if (regex.containsMatchIn(this)) this.toString() else default()
}

/**
 * 使用正则表达式检查当前字符序列是否包含匹配的子序列，
 * 如果包含则返回当前字符序列的字符串表示，否则返回默认值。
 * 此函数重载了[containsOrDefault]，允许传递一个字符串模式而不是正则表达式对象
 *
 * @param pattern 正则表达式字符串模式，用于匹配
 * @param default 默认值提供者，如果当前字符序列不匹配[pattern]，则调用此函数获取默认值
 * @return 当前字符序列的字符串表示，或者默认值
 *
 * @sample
 * ```
 * "hello123".containsOrDefault("\\d+") { "no match" } // "hello123"
 * "hello".containsOrDefault("\\d+") { "no match" } // "no match"
 * ```
 */
fun CharSequence.containsOrDefault(
    @Language("REGEXP") pattern: String,
    default: () -> String,
): String {
    return containsOrDefault(Regex(pattern), default)
}

/**
 * 在当前字符串中查找正则表达式的第一个匹配项，如果没有找到则返回默认值
 *
 * @param regex 要查找的正则表达式
 * @param default 如果没有找到匹配项时返回的默认值提供者
 * @return 匹配项的值或默认值
 *
 * @sample
 * ```
 * "price: 19.99".findOrDefault(Regex("\\d+\\.\\d+")) { "0.0" } // "19.99"
 * "price: unknown".findOrDefault(Regex("\\d+\\.\\d+")) { "0.0" } // "0.0"
 * ```
 */
fun CharSequence.findOrDefault(regex: Regex, default: () -> String): String {
    val matchResult = regex.find(this)
    return matchResult?.value ?: default()
}

/**
 * 在当前字符串中查找符合给定模式的字符串，如果没有找到则返回默认值
 * 此函数重载了[findOrDefault]，允许传递一个字符串模式而不是正则表达式对象
 *
 * @param pattern 要匹配的字符串模式
 * @param default 如果没有找到匹配项时返回的默认值提供者
 * @return 匹配项的值或默认值
 *
 * @sample
 * ```
 * "price: 19.99".findOrDefault("\\d+\\.\\d+") { "0.0" } // "19.99"
 * "price: unknown".findOrDefault("\\d+\\.\\d+") { "0.0" } // "0.0"
 * ```
 */
fun CharSequence.findOrDefault(
    @Language("REGEXP") pattern: String,
    default: () -> String,
): String {
    return findOrDefault(Regex(pattern), default)
}

/**
 * 判断当前字符串是否为HTTP或HTTPS URL
 *
 * @return 如果是HTTP或HTTPS URL则返回true，否则返回false
 */
fun CharSequence.isHttp(): Boolean {
    return matchIn("^https?://")
}
