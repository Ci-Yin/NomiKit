package ciyin.parser.core.movie.model

import ciyin.parser.model.ParserResult
import ciyin.parser.model.Tag

/**
 * 番剧解析结果。
 */
data class MovieResult(
    override val totalPages: Int = 0,
    override val tags: List<Tag> = emptyList(),
    val contents: List<Movie> = emptyList(),
) : ParserResult