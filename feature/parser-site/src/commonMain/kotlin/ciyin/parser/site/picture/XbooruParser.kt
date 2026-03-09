package ciyin.parser.site.picture

import ciyin.parser.core.picture.PictureParserType
import ciyin.parser.core.picture.model.PictureRequest
import ciyin.parser.core.picture.model.PictureResult
import ciyin.parser.scope.ParserScope
import ciyin.parser.site.PictureSiteId


/**
 *
 * Xbooru 解析器（新 DSL 版）。
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2026/3/8 7:24
 */
class XbooruParser : BaseBooruParser() {

    override fun ParserScope<PictureParserType, PictureRequest, PictureResult>.setup() {
        id = PictureSiteId.Xbooru
        baseUrl = "https://xbooru.com"
        superSetup()
    }

}