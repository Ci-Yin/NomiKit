package ciyin.parser.site.picture

import ciyin.parser.site.PictureSiteId
import ciyin.parser.util.PictureParserScope


/**
 *
 * Hypnohub 解析器（新 DSL 版）。
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2026/3/8 6:21
 */
class HypnohubParser : BaseBooruParser() {

    override fun PictureParserScope.setup() {
        id = PictureSiteId.Hypnohub
        baseUrl = "https://hypnohub.net"
        superSetup()
    }

}