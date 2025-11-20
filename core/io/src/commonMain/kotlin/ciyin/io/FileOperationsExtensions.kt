package ciyin.io

/**
 * 文件操作相关扩展函数（创建、删除等）
 */

/**
 * 创建目录
 *
 * 如果文件已存在，则不进行任何操作并返回`false`
 * 否则尝试创建目录，并返回操作结果
 *
 * @return Boolean 创建目录操作的结果，如果目录已存在则返回`false`
 */
fun File.mkDir(): Boolean {
    if (exists()) {
        return false
    }
    return mkdir()
}

/**
 * 创建目录（包括父目录）
 *
 * 如果文件已存在，则不进行任何操作并返回`false`
 * 否则尝试创建目录，并返回操作结果
 *
 * @return Boolean 创建目录操作的结果，如果目录已存在则返回`false`
 */
fun File.mkDirs(): Boolean {
    if (exists()) {
        return false
    }
    return mkdirs()
}

/**
 * 创建新文件
 *
 * @param cover 是否覆盖 true:覆盖，false:不覆盖
 * @return 是否成功创建
 */
fun File.createNewFile(cover: Boolean): Boolean {
    if (cover) {
        return createNewFile()
    } else if (!exists()) {
        parentFile?.mkdirs()
        return createNewFile()
    }
    return false
}
