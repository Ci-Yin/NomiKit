package ciyin.parser.core.comic.model

import ciyin.parser.model.Pagination
import ciyin.parser.model.ParserResult
import ciyin.parser.model.Tag

/**
 * 漫画解析结果。
 *
 * @property tags 解析得到的标签
 * @property contents 漫画内容
 * @property pagination 页码分页状态
 */
data class ComicResult(
    override val pagination: Pagination = Pagination(),
    override val tags: List<Tag> = emptyList(),
    val contents: List<Comic> = emptyList(),
) : ParserResult {
    override fun withPagination(pagination: Pagination): ComicResult = copy(
        pagination = pagination,
    )
}
