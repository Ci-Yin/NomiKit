package ciyin.parser.core.movie

import ciyin.parser.core.ParserType

/**
 * 番剧解析类型集合。
 */
sealed class MovieParserType(final override val id: Int) : ParserType {
    /** 首页。 */
    data object Home : MovieParserType(0)

    /** 番剧列表页。 */
    data object Movies : MovieParserType(1)

    /** 番剧详情页。 */
    data object Movie : MovieParserType(2)
}