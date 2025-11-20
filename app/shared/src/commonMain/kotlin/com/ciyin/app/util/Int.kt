package com.ciyin.app.util


/**
 *
 * kotlin文件作用描述
 *
 * @author 次音(CiYin) QQ:2964221430
 * @github <a href="https://github.com/Ci-Yin">CiYin</a>
 * @since 2024/10/3 上午1:43
 * @version: 1.0
 */

@OptIn(ExperimentalStdlibApi::class)
fun Int.toColorStr(): String {
    val hexString = this.toHexString()
    return "#${hexString.substring(2)}"
}