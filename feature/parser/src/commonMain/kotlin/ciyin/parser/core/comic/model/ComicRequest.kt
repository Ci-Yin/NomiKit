package ciyin.parser.core.comic.model

import ciyin.parser.core.comic.ComicParserType
import ciyin.parser.model.ParserRequest
import ciyin.parser.model.Tag

/**
 * 漫画解析请求模型。
 *
 * @property type 漫画解析业务动作
 * @property id 漫画标识
 * @property chapterId 章节标识
 * @property page 页码，从 1 开始
 * @property tags 标签筛选条件
 */
data class ComicRequest(
    override val type: ComicParserType = ComicParserType.Feed,
    val id: String = "",
    val chapterId: String = "",
    override val page: Int = 1,
    val tags: List<Tag> = emptyList(),
) : ParserRequest {
    init {
        require(page >= 1) {
            "漫画请求页码必须大于等于 1：$page"
        }
    }
}
