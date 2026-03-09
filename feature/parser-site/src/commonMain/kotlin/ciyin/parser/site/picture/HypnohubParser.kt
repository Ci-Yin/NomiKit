package ciyin.parser.site.picture

import ciyin.parser.core.picture.PictureParserType
import ciyin.parser.core.picture.model.PictureRequest
import ciyin.parser.core.picture.model.PictureResult
import ciyin.parser.scope.ParserScope
import ciyin.parser.site.PictureSiteId


/**
 *
 * Hypnohub 解析器（新 DSL 版）。
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2026/3/8 6:21
 */
class HypnohubParser : BaseBooruParser() {

    override fun ParserScope<PictureParserType, PictureRequest, PictureResult>.setup() {
        id = PictureSiteId.Hypnohub
        baseUrl = "https://hypnohub.net"
        superSetup()
    }

}