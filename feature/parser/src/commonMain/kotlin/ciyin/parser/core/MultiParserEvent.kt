package ciyin.parser.core

import ciyin.parser.model.ParserResult


/**
 *
 * kotlin接口作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2026/3/7 3:12
 */
sealed interface MultiParserEvent<TResult : ParserResult> {
    data class Success<TResult : ParserResult>(val result: TResult) : MultiParserEvent<TResult>
    data class Failure<TResult : ParserResult>(val errors: Map<ParserId, Throwable>) :
        MultiParserEvent<TResult>
}