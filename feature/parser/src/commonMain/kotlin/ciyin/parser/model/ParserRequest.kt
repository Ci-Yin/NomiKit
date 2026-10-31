package ciyin.parser.model

import ciyin.parser.core.ParserType

/**
 * 所有解析请求的通用父接口。
 */
interface ParserRequest {
    /** 解析业务动作。 */
    val type: ParserType

    /** 当前请求页码，从 1 开始。 */
    val page: Int
}

