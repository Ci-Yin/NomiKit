package ciyin.parser.site.movie

import ciyin.parser.core.movie.MovieParserType
import ciyin.parser.core.movie.model.MovieRequest
import ciyin.parser.core.movie.model.MovieResult
import ciyin.parser.scope.ParserScope
import ciyin.parser.site.MovieSiteId


/**
 *
 * Hanime Movie 解析器（新 DSL 版）。
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2026/3/8 15:30
 */
class HanimeMovieParser : HanimeMovieBaseParser() {
    override fun ParserScope<MovieParserType, MovieRequest, MovieResult>.setup() = setup {
        id = MovieSiteId.Hanime
        baseUrl = "https://hanimeone.me"
    }
}