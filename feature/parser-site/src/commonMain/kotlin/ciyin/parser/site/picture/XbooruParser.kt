package ciyin.parser.site.picture

import ciyin.parser.site.PictureSiteId
import ciyin.parser.util.PictureParserScope


/**
 *
 * Xbooru 解析器（新 DSL 版）。
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2026/3/8 7:24
 */
class XbooruParser : BaseBooruParser() {

    override fun PictureParserScope.setup() {
        id = PictureSiteId.Xbooru
        baseUrl = "https://xbooru.com"
        superSetup()
    }

}