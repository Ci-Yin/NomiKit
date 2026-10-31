package ciyin.parser.core.comic

import ciyin.parser.core.ParserType

/**
 * 漫画解析业务动作集合。
 */
sealed class ComicParserType(final override val id: Int) : ParserType {
    /** 漫画列表、搜索和筛选。 */
    data object Feed : ComicParserType(1)

    /** 漫画详情和章节目录。 */
    data object Detail : ComicParserType(2)

    /** 单个章节正文。 */
    data object Chapter : ComicParserType(3)
}
