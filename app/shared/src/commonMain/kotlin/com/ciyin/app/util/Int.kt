package com.ciyin.app.util


/**
 *
 * kotlin文件作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2024/10/3 上午1:43
 */

@OptIn(ExperimentalStdlibApi::class)
fun Int.toColorStr(): String {
    val hexString = this.toHexString()
    return "#${hexString.substring(2)}"
}