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
 * `DanbooruParser` 的基础契约测试。
 *
 * 该测试类只覆盖不会依赖网络的初始化行为，
 * 作为后续新增站点解析器测试的模板。
 */
class DanbooruParserTest {

    /**
     * 验证 `DanbooruParser` 初始化后的站点配置契约。
     */
    @Test
    fun parser_configure_should_match_danbooru_site_contract() {
        val parser = DanbooruParser()
        val configure = parser.configure

        assertTrue(parser.enable)
        assertEquals(PictureSiteId.Danbooru, configure.id)
        assertEquals("danbooru", configure.id.site)
        assertEquals("https://danbooru.donmai.us", configure.baseUrl)
        assertNull(configure.request)
        assertTrue(configure.result.tags.isEmpty())
        assertTrue(configure.result.contents.isEmpty())
        assertEquals(0, configure.result.totalPages)
    }

    @Test
    fun danbooru_posts_request_flow() = runTest {
        DanbooruParser().requestAndLog(
            PictureRequest(
                type = PictureParserType.Posts,
                page = 1,
                tags = listOf("qys3"),
            ),
        )
    }

    @Test
    fun danbooru_post_request_flow() = runTest {
        DanbooruParser().requestAndLog(
            PictureRequest(
                type = PictureParserType.Post,
                id = "5993705",
            )
        )
    }

    @Test
    fun danbooru_pools_request_flow() = runTest {
        DanbooruParser().requestAndLog(
            PictureRequest(
                type = PictureParserType.Pools,
                page = 1,
                search = "",
            )
        )
    }

    @Test
    fun danbooru_pool_request_flow() = runTest {
        DanbooruParser().requestAndLog(
            PictureRequest(
                type = PictureParserType.Pool,
                id = "18813",
            )
        )
    }

    @Test
    fun danbooru_popular_request_flow() = runTest {
        DanbooruParser().requestAndLog(
            PictureRequest(
                type = PictureParserType.Popular,
                page = 1,
                scale = "month",
            )
        )
    }

}
