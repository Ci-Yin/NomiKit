package ciyin.parser.model

/**
 * 多解析器聚合结果中的公共信息。
 *
 * @property pagination 多站点归并后的页码分页状态
 * @property tags 多站点去重后的标签
 */
data class MultiParserResult(
    override val pagination: Pagination,
    override val tags: List<Tag>,
) : ParserResult {
    override fun withPagination(pagination: Pagination): MultiParserResult = copy(
        pagination = pagination,
    )
}
