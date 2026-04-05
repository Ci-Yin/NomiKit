package ciyin.parser.core.movie

import ciyin.parser.core.BaseParser
import ciyin.parser.core.movie.model.Movie
import ciyin.parser.core.movie.model.MovieRequest
import ciyin.parser.core.movie.model.MovieResult
import ciyin.parser.scope.ParserDsl
import ciyin.parser.util.MovieParserScope

/**
 * 番剧解析父类骨架。
 */
abstract class MovieParser : BaseParser<MovieParserType, MovieRequest, MovieResult>() {

    override val defTResult: MovieResult get() = MovieResult()

    /**
     * 添加番剧信息修改。
     *
     * @param block 番剧信息修改。
     */
    @ParserDsl
    fun MovieParserScope.onItemRevise(block: Movie.() -> Movie) {
        onResultRevise {
            copy(contents = contents.map(block))
        }
    }
}
