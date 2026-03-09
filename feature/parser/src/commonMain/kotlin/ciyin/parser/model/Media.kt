package ciyin.parser.model


/**
 * 媒体信息领域模型。
 *
 * 表示一条媒体资源（插画 / 图片 / 视频等）的核心业务数据，
 * 不包含任何数据库、序列化或平台相关实现，仅用于领域层与业务逻辑。
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2025/4/9 11:27
 * @property fileName 本地或逻辑层面的文件名（唯一标识）
 * @property id 媒体唯一 ID（通常来自远端或业务系统）
 * @property name 媒体名称
 * @property web 媒体所属或来源网站标识
 * @property description 媒体内容描述
 * @property rating 媒体分级（安全等级）
 * @property originalUrl 原始媒体资源 URL
 * @property sampleUrl 预览或压缩版本 URL
 * @property thumbUrl 缩略图 URL
 * @property sourceUrl 媒体原始来源页面 URL
 * @property fileSize 文件大小（字节）
 * @property md5 文件 MD5 校验值
 * @property width 媒体宽度（像素）
 * @property height 媒体高度（像素）
 * @property createdAt 媒体创建时间戳
 * @property updatedAt 媒体更新时间戳
 */
data class Media(
    val fileName: String = "",
    val id: Int = 0,
    val name: String = "",
    val web: String = "",
    val description: String = "",
    val rating: Rating = Rating.Safe,
    val originalUrl: String = "",
    val sampleUrl: String = "",
    val thumbUrl: String = "",
    val sourceUrl: String = "",
    val fileSize: Long = 0L,
    val md5: String = "",
    val width: Int = 0,
    val height: Int = 0,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
)



