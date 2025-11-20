package ciyin.lang

/**
 *
 * kotlin文件作用描述
 *
 * @author 次音(CiYin) QQ:2964221430
 * @github <a href="https://github.com/Ci-Yin">CiYin</a>
 * @since 2024/8/29 下午4:48
 * @version: 1.0
 */

// 但是没有 peek()、pop()、push() 方法，需要自己实现扩展函数
fun <T> ArrayDeque<T>.peek(): T? = lastOrNull()
fun <T> ArrayDeque<T>.pop(): T = removeLast()
fun <T> ArrayDeque<T>.push(element: T) = addLast(element)

/**
 * 重置列表并添加一个元素。
 *
 * @param element 要添加到列表中的元素。
 * @return 添加操作的结果。
 */
fun <T> MutableList<T>.sub(element: T): Boolean {
    clear()
    return add(element)
}

/**
 * 重置列表并添加一个元素集合。
 *
 * @param elements 要添加到列表中的元素集合。
 * @return 添加操作的结果。
 */
fun <T> MutableList<T>.subAll(elements: Collection<T>): Boolean {
    clear()
    return addAll(elements)
}

fun <T> MutableList<T>.replace(
    old: T,
    new: T,
    predicate: (T, T) -> Boolean = { a, b -> a == b }
): Boolean {
    return replace(indexOfFirst { predicate(it, old) }, new)
}

/**
 * 在列表中查找满足指定条件的元素的索引。
 * 如果找到满足条件的元素，则返回该元素的索引；如果未找到，则返回-1。
 *
 * @param predicate 用于判断元素是否满足条件的函数，接收列表中的一个元素作为参数，返回一个布尔值。
 * @return 满足条件的元素的索引，如果不存在则返回-1。
 */
fun <T> Collection<T>.findIndex(predicate: (T) -> Boolean): Int {
    forEachIndexed { index, t ->
        if (predicate(t)) return index
    }
    return -1
}

/**
 * 替换列表中的特定元素
 *
 * 该函数旨在替换列表中首次出现的特定元素。它首先查找该元素的位置，
 * 如果找到，则移除旧元素并在相同位置插入新元素。这确保了列表的其他部分保持不变。
 *
 * @param T 列表中元素的类型
 * @param old 需要被替换的元素
 * @param new 替代元素
 * @return 如果替换成功，则返回true；如果列表中没有该元素，则返回false。
 */
fun <T> MutableList<T>.replace(old: T, new: T): Boolean {
    return replace(indexOf(old), new)
}

/**
 * 向列表中替换一个指定位置的元素
 *
 * @param index 替换元素的索引位置
 * @param value 替换的新元素
 * @return 如果替换成功，返回true；如果指定位置不存在，返回false
 */
fun <T> MutableList<T>.replace(index: Int, value: T): Boolean {
    // 如果元素不存在，则返回false
    if (index == -1) return false
    // 移除旧元素
    removeAt(index)
    // 在相同位置插入新元素
    add(index, value)
    // 替换成功，返回true
    return true
}

/**
 * 筛选列表，只保留满足指定条件的元素。
 *
 * @param predicate 用于判断元素是否满足条件的函数，接收列表中的一个元素作为参数，返回一个布尔值。
 */
fun <V> MutableList<V>.filter2(predicate: (V) -> Boolean) {
    val list2 = ArrayList(this)
    clear()
    for (v in list2) {
        if (predicate(v)) {
            add(v)
        }
    }
}


/**
 * 如果在[elements]集合全部中找到，则返回 `true` 。
 *
 * @param elements 被查询的数据
 * @param block 比较函数，默认为相等比较
 * @return 是否查询到
 */
fun <V> Collection<V>.contains(
    elements: Collection<V>,
    block: (V, V) -> Boolean = { v1, v2 ->
        v1 == v2
    }
): Boolean {
    var count = 0
    for (element in elements) {
        for (value in this) {
            if (block(value, element)) {
                count++
                break
            }
        }
    }
    return count == elements.size
}

/**
 * 生成指定范围内的数字列表。
 *
 * @param startIndex 起始索引（包含）。
 * @param endIndex 结束索引（包含）。
 * @return 包含从 [startIndex] 到 [endIndex] 的数字列表，每个数字转换为字符串形式。
 */
fun numberList(startIndex: Int, endIndex: Int): List<String> {
    val list = arrayListOf<String>()
    for (i in startIndex..endIndex) {
        list.add(i.toString())
    }
    return list
}
