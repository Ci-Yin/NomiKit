package ciyin.io

import kotlin.math.log10
import kotlin.math.pow

/**
 * 文件格式化相关扩展函数
 */

private val sizeUnits =
    arrayOf("B", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB", "BB", "NB", "DB", "CB")

/**
 * 格式化文件大小
 *
 * @return 格式化后的大小（例如：12.35MB、876.00KB）
 */
fun Long.formatFileSize(): String {
    if (this <= 0L) return "0B"

    val unit = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (log10(this.toDouble()) / log10(1024.0)).toInt().coerceIn(0, unit.lastIndex)
    val size = this / 1024.0.pow(digitGroups.toDouble())

    // 手动保留两位小数，避免使用 String.format（在 commonMain 不可用）
    val rounded = (size * 100).toInt() / 100.0

    // 使用字符串模板拼接
    return "$rounded ${unit[digitGroups]}"
}

/**
 * 获取已格式化的文件(夹)大小
 *
 * @return 格式化后的大小
 */
val File.formatSize: String get() = dirSize().formatFileSize()

/**
 * 遍历计算文件或文件夹及其所有子文件的总大小
 *
 * @param onProgress 进度回调，返回是否继续计算
 * @return 返回文件或文件夹及其所有子文件的总大小
 */
fun File.dirSize(onProgress: (Long) -> Boolean = { true }): Long {
    var len = 0L
    walkFileTree {
        val length = it.length()
        onProgress(length).apply { len += length }
    }
    return len
}
