package ciyin.parser.site.picture

import ciyin.parser.site.PictureSiteId
import ciyin.parser.util.PictureParserScope


/**
 *
 * Xbooru 解析器（新 DSL 版）。
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2026/3/8 7:24
 * @param baseUrlOverride 可选基础地址，用于镜像端点与离线请求链契约测试。
 */
class XbooruParser(baseUrlOverride: String? = null) : BaseBooruParser(baseUrlOverride) {

    /** 注册站点身份并复用 Booru 请求协议。 */
    override fun PictureParserScope.setup() {
        id = PictureSiteId.Xbooru
        baseUrl = "https://xbooru.com"
        superSetup()
    }

}