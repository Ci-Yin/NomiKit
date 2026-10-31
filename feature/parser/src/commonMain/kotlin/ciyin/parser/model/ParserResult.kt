package ciyin.parser.model

/**
 * 所有解析结果的通用父接口。
 */
interface ParserResult {
    /** 解析结果中的标签。 */
    val tags: List<Tag>

    /** 解析结果的分页状态。 */
    val pagination: Pagination

    /**
     * 使用新的分页状态创建结果。
     *
     * 解析框架用该方法统一写入请求页码，领域结果保留自身的具体类型。
     *
     * @param pagination 新的分页状态
     * @return 使用新分页状态的结果
     */
    fun withPagination(pagination: Pagination): ParserResult
}

