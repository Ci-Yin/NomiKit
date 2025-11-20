package ciyin.system.utils.image

import ciyin.io.File
import ciyin.io.extension


/**
 *
 * kotlin文件作用描述
 *
 * @author 次音(CiYin) QQ:2964221430
 * @github <a href="https://github.com/Ci-Yin">CiYin</a>
 * @since 2025/11/2 04:50
 * @version: 1.0
 */

object ImageUtils

internal expect fun resizeImageFile(
    utils: ImageUtils,
    input: File,
    output: File,
    width: Int,
    height: Int,
    format: String
)

fun ImageUtils.resizeImageFile(
    input: File,
    output: File,
    width: Int,
    height: Int,
    format: String = output.extension.ifBlank { "png" }
) = resizeImageFile(
    utils = this,
    input = input,
    output = output,
    width = width,
    height = height,
    format = format
)

