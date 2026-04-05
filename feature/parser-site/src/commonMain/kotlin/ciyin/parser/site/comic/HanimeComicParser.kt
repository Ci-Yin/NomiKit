package ciyin.parser.site.comic

import ciyin.parser.site.ComicSiteId
import ciyin.parser.util.ComicParserScope


/**
 *
 * Hanime Comic 解析器（新 DSL 版）。
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2026/3/8 15:30
 */
class HanimeComicParser : HanimeComicBaseParser() {
    override fun ComicParserScope.setup() = setup {
        id = ComicSiteId.Hanime
        baseUrl = "https://hanimeone.me"
    }
}