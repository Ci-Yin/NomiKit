package ciyin.parser.core.picture

import ciyin.parser.core.ParserType

/**
 * 图片解析业务动作集合。
 *
 * 类型描述稳定的业务动作，不直接对应某个站点的页面名称。
 */
sealed class PictureParserType(final override val id: Int) : ParserType {
    /** 帖子列表、搜索和标签筛选。 */
    data object Feed : PictureParserType(1)

    /** 帖子详情。 */
    data object Detail : PictureParserType(2)

    /** 画集列表和画集搜索。 */
    data object CollectionList : PictureParserType(3)

    /** 画集详情及其中的帖子列表。 */
    data object Collection : PictureParserType(4)

    /** 热门和榜单。 */
    data object Ranking : PictureParserType(5)
}
