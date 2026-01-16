package ciyin.lang

import kotlin.math.pow
import kotlin.math.round


/**
 *
 * kotlin文件作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2024/8/27 上午4:24
 */

/**
 * 将浮点数保留指定位数的小数
 *
 * 该函数的目的是对浮点数进行四舍五入，以获得指定位数的小数部分这对于处理浮点数运算中的精度问题非常有用
 * 它通过将数字乘以10的decimal次方，然后四舍五入，最后再除以10的decimal次方来实现这一目标
 *
 * @param number 需要进行小数位数限制的浮点数
 * @param decimal 小数位数的位数，默认为2如果未指定，则保留两位小数
 * @return 四舍五入后的浮点数，保留指定位数的小数
 */
fun decimals(number: Double, decimal: Int = 2): Double {
    if (decimal <= 0) return round(number)
    val factor = 10.0.pow(decimal)
    return round(number * factor) / factor
}

fun calPct(a: Int, b: Int, c: Int): Double {
    val range = b - a
    val percentage = (c - a).toDouble() / range
    return percentage
}