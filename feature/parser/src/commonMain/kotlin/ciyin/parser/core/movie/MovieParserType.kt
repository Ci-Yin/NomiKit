package ciyin.parser.core.movie

import ciyin.parser.core.ParserType

/**
 * 影视解析业务动作集合。
 */
sealed class MovieParserType(final override val id: Int) : ParserType {
    /** 视频列表、搜索和筛选。 */
    data object Feed : MovieParserType(1)

    /** 视频详情、播放源和相关内容。 */
    data object Detail : MovieParserType(2)
}
