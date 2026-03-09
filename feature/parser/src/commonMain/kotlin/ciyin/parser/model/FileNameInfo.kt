package ciyin.parser.model

/** 定义文件信息数据类 */
data class FileNameInfo(
    /** 来源标识，比如 web、app 等 */
    val site: String,
    /** 文件 ID（数字部分） */
    val id: Int,
    /** 文件 MD5，可为空字符串 */
    val md5: String,
    /** 文件扩展名，例如 jpg、mp4 */
    val fileExt: String
)