package ciyin.parser.model

import ciyin.parser.core.ParserType

/**
 * 所有解析请求的通用父接口。
 */
interface ParserRequest {
    val type: ParserType
}

