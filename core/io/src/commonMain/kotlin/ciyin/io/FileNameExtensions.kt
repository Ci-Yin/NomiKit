package ciyin.io

import ciyin.io.File.Companion.separatorChar

/**
 * 文件名操作相关扩展函数
 */

/**
 * 替换文件名字
 * 例如 `File("/root/user/bar").replaceName("gav")` 输出 `"/root/user/gav"`。
 *
 * @param name 需要替换的文件名
 * @return 替换后的文件路径
 */
fun File.replaceName(name: String): String {
    return "$parent$separatorChar$name"
}

/**
 * 替换文件名字
 * 例如 `File("/root/user/bar").replaceNameFile("gav")` 输出 `File("/root/user/gav")`。
 *
 * @param name 需要替换的文件名
 * @return 替换后的文件对象
 */
fun File.replaceNameFile(name: String): File {
    return replaceName(name).toFile()
}

/**
 * 替换文件名字（不包括扩展名）
 * 例如 `File("/root/user/image.png").replaceNameWithoutExt("test")` 输出 `"/root/user/test.png"`。
 *
 * @param name 需要替换的文件名
 * @return 替换后的文件路径
 */
fun File.replaceNameWithoutExt(name: String): String {
    return if (extension.isEmpty()) {
        replaceName(name)
    } else {
        "${replaceName(name)}.$extension"
    }
}

/**
 * 替换文件名字（不包括扩展名）
 * 例如 `File("/root/user/image.png").replaceNameWithoutExtFile("test")` 输出 `File("/root/user/test.png")`。
 *
 * @param name 需要替换的文件名
 * @return 替换后的文件对象
 */
fun File.replaceNameWithoutExtFile(name: String): File {
    return replaceNameWithoutExt(name).toFile()
}

/**
 * 在文件名字开头添加上一段文字
 * 例如 `File("/root/user/image.png").addNameFirst("test-")` 输出 `"/root/user/test-image.png"`。
 *
 * @param name 需要添加的文字
 * @return 添加后的文件路径
 */
fun File.addNameFirst(name: String): String {
    return replaceNameWithoutExt(name + nameWithoutExtension)
}

/**
 * 在文件名字开头添加上一段文字
 * 例如 `File("/root/user/image.png").addNameFirstFile("test-")` 输出 `File("/root/user/test-image.png")`。
 *
 * @param name 需要添加的文字
 * @return 添加后的文件对象
 */
fun File.addNameFirstFile(name: String): File {
    return addNameFirst(name).toFile()
}

/**
 * 在文件名字结尾添加上一段文字，不是扩展名结尾
 * 例如 `File("/root/user/image.png").addNameLast("-test")` 输出 `"/root/user/image-test.png"`。
 *
 * @param name 需要添加的文字
 * @return 添加后的文件路径
 */
fun File.addNameLast(name: String): String {
    return replaceNameWithoutExt(nameWithoutExtension + name)
}

/**
 * 在文件名字结尾添加上一段文字，不是扩展名结尾
 * 例如 `File("/root/user/image.png").addNameLastFile("-test")` 输出 `File("/root/user/image-test.png")`。
 *
 * @param name 需要添加的文字
 * @return 添加后的文件对象
 */
fun File.addNameLastFile(name: String): File {
    return addNameLast(name).toFile()
}

/**
 * 替换文件扩展名
 */
fun CharSequence.replaceExtension(ext: String): String {
    return replaceFirst("\\.([^.]+)$".toRegex(), ".$ext")
}

/**
 * 替换文件扩展名
 *
 * @param ext 需要替换的文件扩展名
 * @return 替换后的文件路径
 */
fun File.replaceExtension(ext: String): String {
    return path.replaceFirst("[^.]+$".toRegex(), ext)
}

/**
 * 替换文件扩展名
 *
 * @param ext 需要替换的文件扩展名
 * @return 替换后的文件对象
 */
fun File.replaceExtensionFile(ext: String): File {
    return replaceExtension(ext).toFile()
}
