package ciyin.parser.util

import ciyin.parser.model.FileNameInfo


object FileNameParser {

    /**
     * 文件名正则表达式，用于匹配文件命名规则。
     * 规则为：`^(\w+)_(\d+)_(\w*)\.(\w+)$`
     * - 第一个捕获组：[FileNameInfo.site]
     * - 第二个捕获组：[FileNameInfo.id]
     * - 第三个捕获组：[FileNameInfo.md5]
     * - 第四个捕获组：[FileNameInfo.fileExt]
     */
    private val FILE_NAME_REGEX = Regex("""^(\w+)_(\d+)_(\w*)\.(\w+)$""")

    /**
     * 解析文件名为 [FileNameInfo]
     *
     * @param fileName 传入的文件名，例如 "web_12345_a1b2c3d4.jpg"
     * @return 匹配成功返回 FileInfo，否则返回 null
     */
    fun parse(fileName: String): FileNameInfo? {
        val match = FILE_NAME_REGEX.find(fileName.trim()) ?: return null
        val (web, idStr, md5, fileExt) = match.destructured
        return FileNameInfo(
            site = web.trim(),
            id = idStr.toInt(),
            md5 = md5.trim(),
            fileExt = fileExt.trim()
        )
    }

    /**
     * 是否为规定文件
     */
    fun isValid(fileName: String): Boolean {
        return FILE_NAME_REGEX.containsMatchIn(fileName)
    }

    fun buildFileName(fileInfo: FileNameInfo): String {
        return "${fileInfo.site}_${fileInfo.id}_${fileInfo.md5}.${fileInfo.fileExt}"
    }

}

/**
 * 解析文件名为 [FileNameInfo]
 *
 * @see FileNameParser.parse
 */
fun String.toFileName(): FileNameInfo? = FileNameParser.parse(this)

/**
 * 是否为规定文件
 */
fun String.isValidFileName(): Boolean = FileNameParser.isValid(this)

/**
 * 构建文件名
 */
fun FileNameInfo.buildFileName(): String = FileNameParser.buildFileName(this)