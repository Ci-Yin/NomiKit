package ciyin.parser.core.picture.model

import ciyin.parser.core.picture.PictureParserType
import ciyin.parser.model.ParserRequest

/**
 * 图片解析请求模型。
 *
 * @property type 图片解析业务动作
 * @property id 帖子或画集标识
 * @property page 页码，从 1 开始
 * @property tags 标签筛选条件
 * @property search 画集或站点搜索关键词
 * @property scale 热门榜单的时间范围
 */
data class PictureRequest(
    override val type: PictureParserType,
    val id: String = "",
    override val page: Int = 1,
    val tags: List<String> = emptyList(),
    val search: String = "",
    val scale: PictureRankingScale = PictureRankingScale.Day,
) : ParserRequest {
    init {
        require(page >= 1) {
            "图片请求页码必须大于等于 1：$page"
        }
    }
}

/**
 * 图片热门榜单的时间范围。
 *
 * @property value 站点请求所使用的值
 */
enum class PictureRankingScale(val value: String) {
    /** 日榜。 */
    Day("day"),

    /** 周榜。 */
    Week("week"),

    /** 月榜。 */
    Month("month"),
}
