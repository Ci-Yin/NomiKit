package ciyin.parser.core.comic.model

import ciyin.parser.model.Media
import ciyin.parser.model.Rating
import ciyin.parser.model.Tag


/**
 * 漫画领域模型，描述一部漫画的完整业务信息。
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2025/3/17 18:29
 * @property id 漫画唯一标识（业务 ID）
 * @property title 漫画名称
 * @property alias 漫画别名
 * @property description 漫画内容描述
 * @property originalUrl 封面原图 URL
 * @property sampleUrl 封面示例图 URL
 * @property thumbUrl 封面缩略图 URL
 * @property sourceUrl 漫画来源页面 URL
 * @property region 漫画所属地区
 * @property restrict 漫画分级或限制信息
 * @property status 漫画状态（如：连载中 / 已完结）
 * @property latestChapter 最新章节信息
 * @property lastUpdatedChapter 最后更新的章节信息
 * @property popularity 漫画热度值
 * @property chapterGroups 章节分组列表
 * @property contents 漫画正文内容媒体列表
 * @property relatedContents 更多相关内容列表
 * @property tags 标签列表
 * @property createdAt 创建时间戳
 * @property updatedAt 更新时间戳
 * @property fileName 文件名标识
 * @property rating 内容评级
 * @property fileSize 文件大小（字节）
 * @property site 来源网站
 * @property md5 文件 MD5 校验值
 * @property width 媒体宽度
 * @property height 媒体高度
 */
data class Comic(
    val id: String = "",
    val title: String = "",
    val alias: String = "",
    val description: String = "",
    val originalUrl: String = "",
    val sampleUrl: String = "",
    val thumbUrl: String = "",
    val sourceUrl: String = "",
    val region: String = "",
    val restrict: String = "",
    val status: Int = 0,
    val latestChapter: ComicChapter = ComicChapter(),
    val lastUpdatedChapter: ComicChapter = ComicChapter(),
    val popularity: Int = 0,
    val chapterGroups: List<ComicChapterGroup> = emptyList(),
    val contents: List<Media> = emptyList(),
    val relatedContents: List<Media> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val fileName: String = "",
    val rating: Rating = Rating.Safe,
    val fileSize: Long = 0L,
    val md5: String = "",
    val site: String = "",
    val width: Int = 0,
    val height: Int = 0,
)

/**
 * 漫画章节分组领域模型。
 *
 * @property type 分组类型（如：正篇 / 番外 / 特典）
 * @property name 分组名称
 * @property chapters 该分组下的章节列表
 */
data class ComicChapterGroup(
    val type: String = "",
    val name: String = "",
    val chapters: List<ComicChapter> = emptyList(),
)

/**
 * 漫画章节领域模型。
 *
 * @property id 章节唯一标识
 * @property title 章节名称
 * @property index 章节序号
 * @property pageCount 章节包含的页面数量
 * @property contents 章节内的媒体内容列表
 * @property createdAt 章节创建时间戳
 */
data class ComicChapter(
    val id: String = "",
    val title: String = "",
    val index: Int = 0,
    val pageCount: Long = 0L,
    val contents: List<Media> = emptyList(),
    val createdAt: Long = 0L,
)

fun Media.toComicInfo() = Comic(
    fileName = fileName,
    id = id.toString(),
    title = name,
    site = web,
    description = description,
    rating = rating,
    originalUrl = originalUrl,
    sampleUrl = sampleUrl,
    thumbUrl = thumbUrl,
    sourceUrl = sourceUrl,
    fileSize = fileSize,
    md5 = md5,
    width = width,
    height = height,
    createdAt = createdAt,
    updatedAt = updatedAt
)

/**
 * 获取获取解析类id
 */
//val ComicInfo.parserId: Int get() = ParserManager.id(web)