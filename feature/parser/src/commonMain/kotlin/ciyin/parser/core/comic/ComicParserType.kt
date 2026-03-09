package ciyin.parser.core.comic

import ciyin.parser.core.ParserType

/**
 * 漫画解析类型集合。
 */
sealed class ComicParserType(final override val id: Int) : ParserType {
    /** 首页。 */
    data object Home : ComicParserType(0)

    /** 漫画列表页。 */
    data object Comics : ComicParserType(1)

    /** 漫画详情页。 */
    data object Comic : ComicParserType(2)

    /** 章节页。 */
    data object Chapter : ComicParserType(3)
}