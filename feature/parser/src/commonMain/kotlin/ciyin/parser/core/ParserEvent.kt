package ciyin.parser.core

import ciyin.parser.model.ParserResult


/**
 *
 * kotlin接口作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2026/3/7 3:12
 */
sealed interface ParserEvent<TResult : ParserResult> {
    data class Success<TResult : ParserResult>(val result: TResult) : ParserEvent<TResult>
    data class Failure<TResult : ParserResult>(val errors: List<Throwable>) : ParserEvent<TResult>
}