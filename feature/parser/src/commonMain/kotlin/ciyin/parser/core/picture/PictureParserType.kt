package ciyin.parser.core.picture

import ciyin.parser.core.ParserType

/**
 * 图站解析类型集合。
 */
sealed class PictureParserType(final override val id: Int) : ParserType {
    /** 首页。 */
    data object Home : PictureParserType(0)

    /** 列表页。 */
    data object Posts : PictureParserType(1)

    /** 详情页。 */
    data object Post : PictureParserType(2)

    /** 画册列表页。 */
    data object Pools : PictureParserType(3)

    /** 画册详情页。 */
    data object Pool : PictureParserType(4)

    /** 热门页。 */
    data object Popular : PictureParserType(5)
}