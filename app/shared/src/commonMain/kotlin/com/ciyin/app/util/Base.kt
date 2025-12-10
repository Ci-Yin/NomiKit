package com.ciyin.app.util

import ciyin.lang.containsOrDefault
import ciyin.lang.match
import org.intellij.lang.annotations.Language


/**
 *
 * kotlin文件作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2024/10/1 下午3:22
 */

/**
 * 该函数用于在给定列表中找到或生成一个不重复的名称
 * 它通过在名称末尾添加递增的数字来确保名称的唯一性
 *
 * @param games 包含所有元素的列表，用于检查名称是否重复
 * @param pattern 一个正则表达式字符串，用于匹配数字后缀
 * @param predicate 一个函数，用于从元素中提取名称字符串
 * @return 返回一个不重复的名称字符串
 */
fun <E> String.withIncrementName(
    games: List<E>,
    @Language("REGEXP") pattern: String = "\\((\\d*)\\)$",
    name: (Int) -> String = { " ($it)" },
    predicate: (E) -> String
): String {
    // 获取当前元素的原始名称
    val rawPreset = this
    // 检查是否有重复名称，如果没有则直接返回原始名称
    if (games.any { predicate(it) == rawPreset }.not()) {
        return rawPreset
    }
    // 去除原始名称中的数字后缀，获取基础名称
    val basePreset = rawPreset.replace(Regex(pattern), "")
    // 找到所有包含基础名称的元素，并获取其名称
    // 然后找到这些名称中数字后缀最大的一个
    // 如果没有数字后缀，则默认为0
    val preset = games.filter { predicate(it).contains(basePreset) }
        .map { predicate(it) }
        .maxBy { it.match("\\d+").toIntOrNull() ?: 0 }
        .replace(Regex(pattern)) {
            // 在最大数字后缀的基础上加1
            name(it.groupValues.getOrElse(1) { "0" }.toInt() + 1)
        }
        .containsOrDefault(pattern) { "$rawPreset${name(1)}" }
    // 返回新的不重复名称
    return preset
}

