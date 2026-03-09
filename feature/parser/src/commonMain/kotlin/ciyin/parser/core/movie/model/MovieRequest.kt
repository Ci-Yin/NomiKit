package ciyin.parser.core.movie.model

import ciyin.parser.core.ParserType
import ciyin.parser.core.movie.MovieParserType
import ciyin.parser.model.ParserRequest
import ciyin.parser.model.Tag

/**
 * 番剧解析请求模型。
 */
data class MovieRequest(
    override val type: ParserType = MovieParserType.Home,
    val id: String = "",
    val page: Int = 0,
    val tags: List<Tag> = emptyList(),
    val search: String = "",
) : ParserRequest