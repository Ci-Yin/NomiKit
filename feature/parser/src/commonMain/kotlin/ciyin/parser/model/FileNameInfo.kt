package ciyin.parser.model

/**
 * 文件名中承载的媒体文件信息。
 *
 * @property site 来源站点标识
 * @property id 文件 ID 字符串，允许字母、数字和下划线
 * @property md5 文件 MD5，可为空字符串
 * @property fileExt 文件扩展名，例如 jpg、mp4
 */
data class FileNameInfo(
    val site: String,
    val id: String,
    val md5: String,
    val fileExt: String,
)