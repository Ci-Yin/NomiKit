package ciyin.parser.core.picture.model

import ciyin.parser.model.ParserResult
import ciyin.parser.model.Pagination
import ciyin.parser.model.Tag

/**
 * 图片解析结果。
 *
 * @property tags 解析得到的标签
 * @property contents 图片内容
 * @property pagination 页码分页状态
 */
data class PictureResult(
    override val tags: List<Tag> = emptyList(),
    val contents: List<Picture> = emptyList(),
    override val pagination: Pagination = Pagination(),
) : ParserResult {
    override fun withPagination(pagination: Pagination): PictureResult = copy(
        pagination = pagination,
    )
}
