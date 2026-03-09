package ciyin.parser.core.picture.model

import ciyin.parser.model.ParserResult
import ciyin.parser.model.Tag

/**
 * 图站解析结果。
 */
data class PictureResult(
    override val tags: List<Tag> = emptyList(),
    val contents: List<Picture> = emptyList(),
    override val totalPages: Int = 0,
) : ParserResult