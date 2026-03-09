package ciyin.parser.core.comic.model

import ciyin.parser.core.ParserType
import ciyin.parser.core.comic.ComicParserType
import ciyin.parser.model.ParserRequest
import ciyin.parser.model.Tag

/**
 * 漫画解析请求模型。
 */
data class ComicRequest(
    override val type: ParserType = ComicParserType.Home,
    val id: String = "",
    val chapterId: String = "",
    val page: Int = 0,
    val tags: List<Tag> = emptyList(),
) : ParserRequest