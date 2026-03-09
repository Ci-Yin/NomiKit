package ciyin.parser.model

/**
 * 所有解析结果的通用父接口。
 */
interface ParserResult {
    val tags: List<Tag>
    val totalPages: Int
}

