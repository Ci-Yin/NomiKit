package ciyin.parser.core.picture.model

import ciyin.parser.model.Rating
import ciyin.parser.model.Tag

/**
 * 图片信息领域模型。
 *
 * 表示一张插画的完整业务信息，
 * 用于领域层与业务逻辑处理，不依赖数据库、网络或平台实现。
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2023/11/16 1:48
 * @property fileName 图片文件名（唯一标识）
 * @property id 图片唯一 ID
 * @property parentId 父图片 ID（用于多页 / 关联图片）
 * @property pixivId Pixiv 对应作品 ID
 *
 * @property name 图片名称
 * @property description 图片描述
 * @property site 图片来源站点标识
 *
 * @property originalUrl 原始图片 URL
 * @property sampleUrl 预览图片 URL
 * @property thumbnailUrl 缩略图 URL
 * @property sourceUrl 原始来源页面 URL
 * @property zipUrl 图片合集压缩包 URL
 * @property postUrl 图片帖子页面 URL
 * @property poolUrl 图片池页面 URL
 * @property poolId 图片池 ID
 * @property poolSummary 画集列表条目的权威摘要；普通图片为 `null`
 *
 * @property fileExt 文件扩展名
 * @property fileSize 文件大小（字节）
 * @property md5 文件 MD5 校验值
 *
 * @property width 图片宽度（像素）
 * @property height 图片高度（像素）
 *
 * @property rating 图片分级（安全等级）
 *
 * @property tags 图片标签集合（已结构化）
 * @property children 子图片文件名列表（用于多图）
 *
 * @property createdAt 创建时间戳
 * @property updatedAt 更新时间戳
 */
data class Picture(
    val fileName: String = "",
    val id: Int = 0,
    val parentId: Int = 0,
    val pixivId: Int = 0,
    val name: String = "",
    val description: String = "",
    val site: String = "",
    val originalUrl: String = "",
    val sampleUrl: String = "",
    val thumbnailUrl: String = "",
    val sourceUrl: String = "",
    val zipUrl: String = "",
    val postUrl: String = "",
    val poolUrl: String = "",
    val poolId: Int = 0,
    val poolSummary: PoolSummary? = null,
    val fileExt: String = "",
    val fileSize: Long = 0L,
    val md5: String = "",
    val width: Int = 0,
    val height: Int = 0,
    val rating: Rating = Rating.Safe,
    val tags: List<Tag> = emptyList(),
    val children: List<String> = emptyList(),
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
)

/**
 * 画集列表条目的不可变业务摘要。
 *
 * @property poolId 站点内画集唯一标识
 * @property title 画集标题
 * @property postCount 画集图片数量；站点未提供时为 `null`
 * @property url 画集详情页地址
 */
data class PoolSummary(
    val poolId: Int,
    val title: String,
    val postCount: Int?,
    val url: String,
)
