package ciyin.parser.site.picture

import ciyin.parser.core.picture.PictureParserType
import ciyin.parser.core.picture.model.PictureRequest
import ciyin.parser.site.PictureSiteId
import ciyin.parser.site.requestAndLog
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * `ZerochanParser` 的基础契约与请求流测试。
 *
 * 该测试类参考 `ciyin.parser.site.picture.DanbooruParserTest`，但目前仅覆盖：
 * - 初始化配置契约；
 * - 未注册类型（Home）的异常契约；
 * - 基础的帖子列表请求流。
 */
class ZerochanParserTest {

    /**
     * 验证 `ZerochanParser` 初始化后的站点配置契约。
     */
    @Test
    fun parser_configure_should_match_zerochan_site_contract() {
        val parser = ZerochanParser()
        val configure = parser.configure

        assertTrue(parser.enable)
        assertEquals(PictureSiteId.Zerochan, configure.id)
        assertEquals("zerochan", configure.id.site)
        assertEquals("https://www.zerochan.net", configure.baseUrl)
        assertNull(configure.request)
        assertTrue(configure.result.tags.isEmpty())
        assertTrue(configure.result.contents.isEmpty())
        assertEquals(0, configure.result.totalPages)
    }

    /**
     * 验证帖子列表请求流（使用空标签访问首页推荐列表）。
     */
    @Test
    fun zerochan_posts_request_flow() = runTest {
        ZerochanParser().requestAndLog(
            PictureRequest(
                type = PictureParserType.Posts,
                page = 1,
                tags = emptyList(),
            ),
        )
    }

    /**
     * 验证帖子请求流。
     */
    @Test
    fun zerochan_post_request_flow() = runTest {
        ZerochanParser().requestAndLog(
            PictureRequest(
                type = PictureParserType.Post,
                id = "4666101",
            ),
        )
    }

}

