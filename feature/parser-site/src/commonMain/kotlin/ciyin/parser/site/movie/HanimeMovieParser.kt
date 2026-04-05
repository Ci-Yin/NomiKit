package ciyin.parser.site.movie

import ciyin.parser.site.MovieSiteId
import ciyin.parser.util.MovieParserScope


/**
 *
 * Hanime Movie 解析器（新 DSL 版）。
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2026/3/8 15:30
 */
class HanimeMovieParser : HanimeMovieBaseParser() {
    override fun MovieParserScope.setup() = setup {
        id = MovieSiteId.Hanime
        baseUrl = "https://hanimeone.me"
    }
}