package ciyin.parser.core.movie

import ciyin.parser.core.BaseParser
import ciyin.parser.core.movie.model.Movie
import ciyin.parser.core.movie.model.MovieRequest
import ciyin.parser.core.movie.model.MovieResult
import ciyin.parser.scope.ParserDsl
import ciyin.parser.scope.ParserScope
import kotlin.reflect.KClass

/**
 * 番剧解析父类骨架。
 */
abstract class MovieParser : BaseParser<MovieParserType, MovieRequest, MovieResult>() {

    override val defTResult: MovieResult get() = MovieResult()

    /**
     * 将 DSL 泛型类型映射为番剧解析类型对象。
     *
     * @param typeClass 类型运行时信息。
     * @return 番剧解析类型。
     */
    final override fun resolveType(typeClass: KClass<out MovieParserType>) = when (typeClass) {
        MovieParserType.Home::class -> MovieParserType.Home
        MovieParserType.Movies::class -> MovieParserType.Movies
        MovieParserType.Movie::class -> MovieParserType.Movie
        else -> error("未支持的 MovieParserType: $typeClass")
    }

    /**
     * 添加番剧信息修改。
     *
     * @param block 番剧信息修改。
     */
    @ParserDsl
    fun ParserScope<MovieParserType, MovieRequest, MovieResult>.onItemRevise(block: Movie.() -> Movie) {
        onResultRevise {
            copy(contents = contents.map(block))
        }
    }
}
