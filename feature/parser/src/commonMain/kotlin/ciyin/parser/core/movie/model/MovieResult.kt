package ciyin.parser.core.movie.model

import ciyin.parser.model.Pagination
import ciyin.parser.model.ParserResult
import ciyin.parser.model.Tag

/**
 * 影视解析结果。
 *
 * @property tags 解析得到的标签
 * @property contents 影视内容
 * @property pagination 页码分页状态
 */
data class MovieResult(
    override val pagination: Pagination = Pagination(),
    override val tags: List<Tag> = emptyList(),
    val contents: List<Movie> = emptyList(),
) : ParserResult {
    override fun withPagination(pagination: Pagination): MovieResult = copy(
        pagination = pagination,
    )
}
