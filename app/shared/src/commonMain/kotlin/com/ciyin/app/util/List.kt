package com.ciyin.app.util

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.StateFactoryMarker

/**
 *
 * kotlin文件作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2024/8/29 下午4:48
 */

/**
 * 标记此函数为状态列表的生成工厂。
 *
 * @param elements 初始元素列表。
 * @return 一个初始化为指定元素列表的可变 SnapshotStateList。
 */
@StateFactoryMarker
fun <T> mutableStateList(elements: Collection<T>) =
    SnapshotStateList<T>().also { it.addAll(elements) }

/**
 * 为集合中的元素生成一个唯一的ID。
 *
 * @param idExtractor 从集合中每个元素提取ID的函数。
 * @return 一个新的、不与集合中任何现有元素ID冲突的唯一ID。
 */
fun <T> Iterable<T>.uniqueId(idExtractor: (T) -> Int): Int {
    val existingIds = this.map(idExtractor).toSet()
    var newId = 1

    while (existingIds.contains(newId)) {
        newId++
    }

    return newId
}

/**
 * 在指定元素之前添加新元素。
 *
 * @param item 要在其之前插入新元素的现有元素。
 * @param element 要插入的新元素。
 * @throws IllegalArgumentException 如果`item`不在列表中。
 */
fun <T> MutableList<T>.addBefore(item: T, element: T) {
    val index = indexOf(item)
    if (index == -1) error("未找该元素")
    add(index, element)
}

/**
 * 在列表中指定元素后添加新元素。
 *
 * @param item 要在其后插入新元素的现有元素。
 * @param element 要插入到列表中的新元素。
 * @throws IllegalArgumentException 如果`item`不在列表中。
 */
fun <T> MutableList<T>.addAfter(item: T, element: T) {
    val index = indexOf(item)
    if (index == -1) error("未找该元素")
    add(index + 1, element)
}

