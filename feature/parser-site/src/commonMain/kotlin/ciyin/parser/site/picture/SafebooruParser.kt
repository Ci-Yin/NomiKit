package ciyin.parser.site.picture

import ciyin.parser.site.PictureSiteId
import ciyin.parser.util.PictureParserScope


/**
 *
 * Safebooru 解析器（新 DSL 版）。
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2026/3/8 5:32
 */
open class SafebooruParser : BaseBooruParser() {

    /** 配置 Safebooru 站点的 DSL 请求与响应逻辑。*/
    override fun PictureParserScope.setup() {
        id = PictureSiteId.Safebooru
        baseUrl = "https://safebooru.org"
        superSetup()
    }

}