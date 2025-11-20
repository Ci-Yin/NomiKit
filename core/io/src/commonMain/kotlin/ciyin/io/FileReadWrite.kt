package ciyin.io

import okio.buffer
import okio.use

/**
 * Gets the entire content of this file as a String using UTF-8 or specified [charset].
 *
 * This method is not recommended on huge files. It has an internal limitation of 2 GB file size.
 *
 * @return the entire content of this file as a String.
 */
fun File.readText(): String {
    return SystemFileSystem.read(toPath()) {
        readUtf8() // ✅ 自动以 UTF-8 解码
    }
}

/**
 * Sets the content of this file as [text] encoded using UTF-8 or specified [charset].
 * If this file exists, it becomes overwritten.
 *
 * @param text text to write into file.
 */
fun File.writeText(text: String) {
    SystemFileSystem.sink(toPath(), mustCreate = false).buffer().use {
        it.writeUtf8(text)
    }
}

/**
 * 写入文件
 *
 * @param content 文本内容
 * @param append  如果为true，则字节将被写入文件的末尾，而不是开头
 * @return 是否成功写入
 */
fun File.write(
    content: String,
    append: Boolean = false
) {
    parentFile?.mkDirs()
    if (append) {
        SystemFileSystem.appendingSink(toPath(), mustExist = false)
    } else {
        SystemFileSystem.sink(toPath(), mustCreate = false)
    }.buffer().use {
        it.writeUtf8(content)
    }
}

/**
 * 读取文件
 *
 * @return 读取的内容
 */
fun File.read(): String {
    if (!SystemFileSystem.exists(toPath())) return ""
    return readText()
}