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
 * `SafebooruParser` 的基础契约与请求流测试。
 */
class SafebooruParserTest {

    /**
     * 验证 `SafebooruParser` 初始化后的站点配置契约。
     */
    @Test
    fun parser_configure_should_match_safebooru_site_contract() {
        val parser = SafebooruParser()
        val configure = parser.configure

        assertTrue(parser.enable)
        assertEquals(PictureSiteId.Safebooru, configure.id)
        assertEquals("safebooru", configure.id.site)
        assertEquals("https://safebooru.org", configure.baseUrl)
        assertNull(configure.request)
        assertTrue(configure.result.tags.isEmpty())
        assertTrue(configure.result.contents.isEmpty())
        assertEquals(0, configure.result.totalPages)
    }

    /**
     * 验证帖子列表请求流。
     */
    @Test
    fun safebooru_posts_request_flow() = runTest {
        SafebooruParser().requestAndLog(
            PictureRequest(
                type = PictureParserType.Posts,
                page = 1,
                tags = listOf("1girl"),
            )
        )
    }

    /**
     * 验证帖子详情请求流。
     */
    @Test
    fun safebooru_post_request_flow() = runTest {
        SafebooruParser().requestAndLog(
            PictureRequest(
                type = PictureParserType.Post,
                id = "1",
            )
        )
    }

}
