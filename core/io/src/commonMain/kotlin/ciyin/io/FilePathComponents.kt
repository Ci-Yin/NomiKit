/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
package ciyin.io


/**
 * 根据给定的文件名估算根名称。
 *
 * 此实现能够找到 /、Drive:/、Drive: 或
 * //network.name/root 作为可能的根名称。
 * / 在这里表示 File.separator，所以也可以使用 \。
 * 所有其他可能的根都无法通过此实现来识别。
 * 也不能保证（但有可能）函数能够检测到
 * 对于当前操作系统不正确的根。例如，在 Unix 中，函数无法检测
 * 像 //network.name/root 这样的网络根名称，但可以检测 Windows 的根，如 C:/。
 *
 * @return 此路径的根的长度或表示根的子字符串，如果此文件名是相对的，则为零。
 */
private fun String.getRootLength(): Int {
    // 注意：分隔符应该已经被替换为系统分隔符
    var first = indexOf(File.separatorChar, 0)
    if (first == 0) {
        if (length > 1 && this[1] == File.separatorChar) {
            // 像 //my.host/home/something 这样的网络名称 ? => //my.host/home/ 应该是根
            // 注意：在 Unix 中不起作用，因为 //my.host/home 在那里被转换为 /my.host/home
            // 所以在 Windows 中，我们将拥有 //my.host/home 的根，但在 Unix 中只是 /
            first = indexOf(File.separatorChar, 2)
            if (first >= 0) {
                first = indexOf(File.separatorChar, first + 1)
                if (first >= 0)
                    return first + 1
                else
                    return length
            }
        }
        return 1
    }
    // C:\
    if (first > 0 && this[first - 1] == ':') {
        first++
        return first
    }
    // C:
    if (first == -1 && endsWith(':'))
        return length
    return 0
}

/**
 * 估算此文件的根名称。
 *
 * 此实现能够找到 /、Drive:/、Drive: 或
 * //network.name/root 作为可能的根名称。
 * / 在这里表示 File.separator，所以也可以使用 \。
 * 所有其他可能的根都无法通过此实现来识别。
 * 也不能保证（但有可能）函数能够检测到
 * 对于当前操作系统不正确的根。例如，在 Unix 中，函数无法检测
 * 像 //network.name/root 这样的网络根名称，但可以检测 Windows 的根，如 C:/。
 *
 * @return 表示此文件根的字符串，如果此文件名是相对的，则为空字符串。
 */
internal val File.rootName: String
    get() = path.substring(0, path.getRootLength())

/**
 * 返回此抽象名称的根组件，例如 /home/user 中的 /，或 C:\file.tmp 中的 C:\，
 * 或 //my.host/home/user 中的 //my.host/home
 */
internal val File.root: File
    get() = File(rootName)

/**
 * 确定此文件是否具有根或表示相对路径。
 *
 * 当此文件具有非空根时返回 `true`。
 */
val File.isRooted: Boolean
    get() = path.getRootLength() > 0

/**
 * 将文件路径表示为目录的集合。
 *
 * @property root 代表路径根的 [File] 对象（例如，`/` 或 `C:`，对于相对路径则为空）。
 * @property segments 代表路径中每个目录的 [File] 对象列表，
 *     直到并包括文件本身。
 */
@ConsistentCopyVisibility
internal data class FilePathComponents
internal constructor(val root: File, val segments: List<File>) {

    /**
     *  返回表示此文件根的字符串，如果此文件名是相对的，则为空字符串。
     */
    val rootName: String get() = root.path

    /**
     * 当 [root] 不为空时返回 `true`。
     */
    val isRooted: Boolean get() = root.path.isNotEmpty()

    /**
     * 返回文件路径中的元素数。
     */
    val size: Int get() = segments.size

    /**
     * 返回路径的子路径，从指定的 [beginIndex] 处的目录开始，
     * 直到指定的 [endIndex]。
     */
    fun subPath(beginIndex: Int, endIndex: Int): File {
        if (beginIndex < 0 || beginIndex > endIndex || endIndex > size)
            throw IllegalArgumentException()

        return File(segments.subList(beginIndex, endIndex).joinToString(File.separator))
    }
}

/**
 * 将文件拆分为路径组件（包含目录的名称和文件本身的名称）
 * 并返回生成的组件集合。
 */
internal fun File.toComponents(): FilePathComponents {
    val path = path
    val rootLength = path.getRootLength()
    val rootName = path.substring(0, rootLength)
    val subPath = path.substring(rootLength)
    val list = if (subPath.isEmpty()) listOf() else subPath.split(File.separatorChar).map(::File)
    return FilePathComponents(File(rootName), list)
}

/**
 * 返回一个相对路径名，它是此路径名的子序列，
 * 从组件 [beginIndex]（含）开始，
 * 到组件 [endIndex]（不含）结束。
 * 编号 0 属于最接近根的组件，
 * 编号 count-1 属于离根最远的组件。
 * @throws IllegalArgumentException 如果 [beginIndex] 为负数，
 * 或 [endIndex] 大于现有组件的数量，
 * 或 [beginIndex] 大于 [endIndex]。
 */
internal fun File.subPath(beginIndex: Int, endIndex: Int): File =
    toComponents().subPath(beginIndex, endIndex)
