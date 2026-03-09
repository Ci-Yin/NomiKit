package ciyin.parser.core.movie.model

import ciyin.parser.model.Rating
import ciyin.parser.model.Tag

/**
 * 影视信息领域模型。
 *
 * 表示一部影视作品或影视条目的完整业务信息，
 * 用于领域层与业务逻辑处理，不依赖数据库、网络或平台实现。
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2023/11/30 23:47
 * @property id 影视标识
 * @property fileName 影视文件名（唯一标识）
 *
 * @property name 影视作品名称
 * @property episodeName 影视集名称
 * @property alias 影视别名
 * @property description 影视内容描述
 *
 * @property rating 影视分级
 * @property site 来源站点标识
 *
 * @property playCount 播放次数
 * @property duration 播放时长（秒）
 *
 * @property playUrl 播放地址
 *
 * @property coverUrl 封面 URL
 * @property sourceUrl 来源页面 URL
 *
 * @property fileSize 文件大小（字节）
 * @property width 封面宽度（像素）
 * @property height 封面高度（像素）
 *
 * @property videos 已缓存的视频列表
 * @property playlist 播放列表
 * @property related 推荐 / 相关影视列表
 *
 * @property tags 影视标签集合
 *
 * @property createdAt 创建时间戳
 * @property updatedAt 更新时间戳
 */
data class Movie(
    val id: String = "",
    val key: String = "",
    val fileName: String = "",
    val name: String = "",
    val episodeName: String = "",
    val alias: String = "",
    val description: String = "",
    val rating: Rating = Rating.Safe,
    val site: String = "",
    val playCount: Long = 0L,
    val duration: Long = 0L,
    val playUrl: String = "",
    val coverUrl: String = "",
    val sourceUrl: String = "",
    val fileSize: Long = 0L,
    val width: Int = 0,
    val height: Int = 0,
    val videos: List<Video> = emptyList(),
    val playlist: List<Movie> = emptyList(),
    val related: List<Movie> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
)

data class Video(
    val url: String,
    val size: Int,
)