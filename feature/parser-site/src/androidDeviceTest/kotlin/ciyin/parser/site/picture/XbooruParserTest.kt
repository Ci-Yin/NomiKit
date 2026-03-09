package ciyin.parser.site.picture

import ciyin.parser.core.ParserEvent
import ciyin.parser.core.picture.PictureParserType
import ciyin.parser.core.picture.model.PictureRequest
import ciyin.parser.core.picture.model.PictureResult
import ciyin.parser.site.PictureSiteId
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * `XbooruParser` 的基础契约与请求流测试。
 */
class XbooruParserTest {

    /**
     * 验证 `XbooruParser` 初始化后的站点配置契约。
     */
    @Test
    fun parser_configure_should_match_xbooru_site_contract() {
        val parser = XbooruParser()
        val configure = parser.configure

        assertTrue(parser.enable)
        assertEquals(PictureSiteId.Xbooru, configure.id)
        assertEquals("xbooru", configure.id.site)
        assertEquals("https://xbooru.com", configure.baseUrl)
        assertNull(configure.request)
        assertTrue(configure.result.tags.isEmpty())
        assertTrue(configure.result.contents.isEmpty())
        assertEquals(0, configure.result.totalPages)
    }

    /**
     * 验证帖子列表请求流。
     */
    @Test
    fun xbooru_posts_request_flow() = runTest {
        XbooruParser().requestAndLog(
            PictureRequest(
                type = PictureParserType.Posts,
                page = 1,
                tags = listOf("ahoge"),
            ),
        )
    }

    /**
     * 验证帖子详情请求流。
     */
    @Test
    fun xbooru_post_request_flow() = runTest {
        XbooruParser().requestAndLog(
            PictureRequest(
                type = PictureParserType.Post,
                id = "1235280",
            ),
        )
    }

    /**
     * 执行请求并断言返回成功且内容非空。
     */
    private suspend fun XbooruParser.requestAndLog(request: PictureRequest): PictureResult {
        var finalResult: PictureResult? = null
        request(request).collect {
            when (it) {
                is ParserEvent.Failure -> error(it.errors)
                is ParserEvent.Success -> {
                    val result = it.result
                    assertTrue(result.contents.isNotEmpty())
                    println(
                        "xbooru type=${request.type} request=$request -> ok contents=${result.contents.size} totalPages=${result.totalPages}",
                    )
                    finalResult = result
                }
            }
        }
        return assertNotNull(finalResult)
    }
}

