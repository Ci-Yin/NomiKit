package ciyin.io

import ciyin.io.File.Companion.separatorChar

/**
 * 文件路径操作相关扩展函数
 */

/**
 * 为目录路径加上一个文件名
 * 例如 `File("/foo/bar").concat("gav")` 输出 `"/foo/bar/gav"`。
 *
 * @param name 文件名
 * @return 连接后的路径
 */
fun File.concat(name: String): String {
    return "$path$separatorChar$name"
}

/**
 * 为目录路径加上一个文件名
 * 例如 `File("/foo/bar").concatFile("gav")` 输出 `File("/foo/bar/gav")`。
 *
 * @param name 文件名
 * @return 连接后的文件
 */
fun File.concatFile(name: String): File {
    return concat(name).toFile()
}

/**
 * 替换父目录
 * 例如 `File("/root/user/bar").replaceParent(File("/gav/test"))` 输出 `"/gav/test/bar"`。
 *
 * @param parent 需要替换的父目录文件
 * @return 替换后的文件路径
 */
fun File.replaceParent(parent: File): String {
    return "${parent.path}$separatorChar$name"
}

/**
 * 替换父目录
 * 例如 `File("/root/user/bar").replaceParent("/gav/test")` 输出 `"/gav/test/bar"`。
 *
 * @param parent 需要替换的父目录路径
 * @return 替换后的文件路径
 */
fun File.replaceParent(parent: String): String {
    return replaceParent(parent.toFile())
}

/**
 * 替换父目录文件
 * 例如 `File("/root/user/bar").replaceParentFile(File("/gav/test"))` 输出 `File("/gav/test/bar")`。
 *
 * @param parent 需要替换的父目录文件
 * @return 替换后的文件
 */
fun File.replaceParentFile(parent: File): File {
    return replaceParent(parent).toFile()
}

/**
 * 替换父目录文件
 * 例如 `File("/root/user/bar").replaceParentFile("/gav/test")` 输出 `File("/gav/test/bar")`。
 *
 * @param parent 需要替换的父目录路径
 * @return 替换后的文件
 */
fun File.replaceParentFile(parent: String): File {
    return replaceParent(parent).toFile()
}
