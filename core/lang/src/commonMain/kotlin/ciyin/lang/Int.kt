package ciyin.lang

import kotlin.math.log10
import kotlin.math.pow


/**
 *
 * kotlin文件作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2024/8/27 上午3:52
 */


private val unit = arrayOf("K", "M", "G", "T", "P", "E", "Z", "Y", "B", "N", "D", "C")
private val unitChinese =
    arrayOf("万", "亿", "兆", "京", "垓", "秭", "穰", "沟", "涧", "正", "载", "极")

/**
 * 将长整型数值格式化为带有单位的字符串表示，支持国际单位和中文单位。
 *
 * @param decimal 保留的小数位数，默认为 1
 * @param isChinese 是否使用中文单位（如“万”、“亿”等），默认为 false
 * @return 格式化后的字符串，例如：1.5K、2.3万
 */
fun Long.formatUnit(decimal: Int = 1, isChinese: Boolean = false): String {
    if (this == 0L) return "0"
    if (this < 0) return "-${(-this).formatUnit(decimal, isChinese)}"

    val base = if (isChinese) 4 else 3        // 中文一万进1级，英文一千进1级
    val divide = if (isChinese) 10000.0 else 1000.0
    val units = if (isChinese) unitChinese else unit

    // 转成 double 再 log10，但保证不要溢出
    val log = (log10(this.toDouble()).toInt() / base).coerceAtLeast(0)
    if (log == 0) return this.toString()

    val value = this / divide.pow(log)

    // 单位索引从 0 开始
    val unitStr = units.getOrElse(log - 1) { units.last() }

    return "${decimals(value, decimal)}$unitStr"
}

/**
 * 将整型数值转换为格式化的字符串表示，内部调用 [Long.formatUnit] 实现。
 *
 * @return 格式化后的字符串
 */
fun Int.formatUnit() = toLong().formatUnit()

/**
 * 如果当前数值为零，则返回由 [defaultValue] 提供的替代值。
 *
 * @param defaultValue 当前值为零时提供的替代值生成函数
 * @return 如果当前值不为零则返回自身，否则返回 [defaultValue] 的结果
 */
inline fun <R : Number> R.ifZero(defaultValue: () -> R): R {
    return if (this.toDouble() == 0.0) defaultValue() else this
}
