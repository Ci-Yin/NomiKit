package ciyin.parser.core.comic.model

import ciyin.parser.model.ParserResult
import ciyin.parser.model.Tag

/**
 * 漫画解析结果。
 */
data class ComicResult(
    override val totalPages: Int = 0,
    override val tags: List<Tag> = emptyList(),
    val contents: List<Comic> = emptyList(),
) : ParserResult