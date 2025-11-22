package ciyin.lang

import org.intellij.lang.annotations.Language


/**
 *
 * 字符串处理相关扩展函数
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2024/8/24 下午11:15
 */


/**
 * 以正则表达式匹配内容文本
 * 只会输出第一个匹配的
 *
 * @param regex   正则表达式
 */
fun CharSequence.match(@Language("REGEXP") regex: String): String {
    val group = matchGroup(regex)
    if (group.isNotEmpty()) {
        return if (group.size == 1) group.first() else group[1]
    }
    return ""
}

/**
 * 在字符串中查找正则表达式模式的匹配项
 *
 * @param pattern 正则表达式模式，用于匹配字符串
 * @return 如果字符串中包含与模式匹配的子字符串，则返回true；否则返回false
 */
fun CharSequence.matchIn(@Language("REGEXP") pattern: String): Boolean {
    return Regex(pattern).containsMatchIn(this)
}

/**
 * 在字符串中查找正则表达式模式的匹配项，并返回匹配的组
 *
 * @param pattern 正则表达式模式，用于匹配字符串
 * @return 包含匹配组的列表如果无匹配，则返回空列表
 */
fun CharSequence.matchGroup(@Language("REGEXP") pattern: String): List<String> {
    val matchResult = Regex(pattern).find(this)
    return matchResult?.groupValues ?: emptyList()
}

/**
 * 检查字符串是否包含中文字符
 *
 * @return 如果字符串中至少包含一个中文字符，则返回true；否则返回false
 */
fun CharSequence.isChinese(): Boolean {
    return matches("[\\u0391-\\uFFE5]+".toRegex())
}

/**
 * 使用正则表达式检查当前字符序列是否包含匹配的子序列，
 * 如果包含则返回当前字符序列的字符串表示，否则返回默认值。
 *
 * @param regex 正则表达式对象，用于匹配
 * @param default 默认值提供者，如果当前字符序列不匹配[regex]，则调用此函数获取默认值
 * @return 当前字符序列的字符串表示，或者默认值
 */
fun CharSequence.containsOrDefault(regex: Regex, default: () -> String): String {
    return if (regex.containsMatchIn(this)) this.toString() else default()
}

/**
 * 使用正则表达式检查当前字符序列是否包含匹配的子序列，
 * 如果包含则返回当前字符序列的字符串表示，否则返回默认值。
 * 此函数重载了[findOrDefault]，允许传递一个字符串模式而不是正则表达式对象
 *
 * @param pattern 正则表达式字符串模式，用于匹配
 * @param default 默认值提供者，如果当前字符序列不匹配[pattern]，则调用此函数获取默认值
 * @return 当前字符序列的字符串表示，或者默认值
 */
fun CharSequence.containsOrDefault(
    @Language("REGEXP") pattern: String,
    default: () -> String
): String {
    return containsOrDefault(Regex(pattern), default)
}

/**
 * 在当前字符串中查找正则表达式的第一个匹配项，如果没有找到则返回默认值
 *
 * @param regex 要查找的正则表达式
 * @param default 如果没有找到匹配项时返回的默认值提供者
 * @return 匹配项的值或默认值
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
 */
fun CharSequence.findOrDefault(
    @Language("REGEXP") pattern: String,
    default: () -> String
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
