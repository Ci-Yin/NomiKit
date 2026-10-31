package ciyin.parser.core.movie.model

import ciyin.parser.core.movie.MovieParserType
import ciyin.parser.model.ParserRequest
import ciyin.parser.model.Tag

/**
 * 影视解析请求模型。
 *
 * @property type 影视解析业务动作
 * @property id 影视条目标识
 * @property page 页码，从 1 开始
 * @property tags 标签筛选条件
 * @property search 搜索关键词
 */
data class MovieRequest(
    override val type: MovieParserType = MovieParserType.Feed,
    val id: String = "",
    override val page: Int = 1,
    val tags: List<Tag> = emptyList(),
    val search: String = "",
) : ParserRequest {
    init {
        require(page >= 1) {
            "影视请求页码必须大于等于 1：$page"
        }
    }
}
