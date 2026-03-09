package ciyin.parser.site.comic

import ciyin.parser.core.comic.ComicParserType
import ciyin.parser.core.comic.model.ComicRequest
import ciyin.parser.core.comic.model.ComicResult
import ciyin.parser.scope.ParserScope
import ciyin.parser.site.ComicSiteId


/**
 *
 * Hanime Comic 解析器（新 DSL 版）。
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2026/3/8 15:30
 */
class HanimeComicParser : HanimeComicBaseParser() {
    override fun ParserScope<ComicParserType, ComicRequest, ComicResult>.setup() = setup {
        id = ComicSiteId.Hanime
        baseUrl = "https://hanimeone.me"
    }
}