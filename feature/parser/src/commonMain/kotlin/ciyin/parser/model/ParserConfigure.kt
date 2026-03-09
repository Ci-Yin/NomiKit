package ciyin.parser.model

import ciyin.parser.core.ParserId

data class ParserConfigure<TRequest : ParserRequest, TResult : ParserResult>(
    val id: ParserId,
    val baseUrl: String,
    val request: TRequest?,
    val result: TResult,
)
