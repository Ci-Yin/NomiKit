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
 * `HypnohubParser` 的基础契约与请求流测试。
 */
class HypnohubParserTest {

    /**
     * 验证 `HypnohubParser` 初始化后的站点配置契约。
     */
    @Test
    fun parser_configure_should_match_hypnohub_site_contract() {
        val parser = HypnohubParser()
        val configure = parser.configure

        assertTrue(parser.enable)
        assertEquals(PictureSiteId.Hypnohub, configure.id)
        assertEquals("hypnohub", configure.id.site)
        assertEquals("https://hypnohub.net", configure.baseUrl)
        assertNull(configure.request)
        assertTrue(configure.result.tags.isEmpty())
        assertTrue(configure.result.contents.isEmpty())
        assertEquals(0, configure.result.totalPages)
    }

    /**
     * 验证帖子列表请求流。
     */
    @Test
    fun hypnohub_posts_request_flow() = runTest {
        HypnohubParser().requestAndLog(
            PictureRequest(
                type = PictureParserType.Posts,
                page = 1,
                // Hypnohub 与 Safebooru 同源，使用一个常见 tag 进行简单流测试。
                tags = listOf("ahoge"),
            ),
        )
    }

    /**
     * 验证帖子详情请求流。
     */
    @Test
    fun hypnohub_post_request_flow() = runTest {
        HypnohubParser().requestAndLog(
            PictureRequest(
                type = PictureParserType.Post,
                id = "1",
            ),
        )
    }

}

