package ciyin.parser.model

/**
 * 多解析器聚合结果。
 */
data class MultiParserResult(
    override val totalPages: Int,
    override val tags: List<Tag>,
) : ParserResult

