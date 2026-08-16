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
 * `DanbooruParser` 的设备端契约与真实网络请求测试。
 *
 * 依赖真实 Danbooru 站点的请求只允许保留在设备测试源集，避免普通 desktop 测试受公网状态影响。
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

    /** 验证真实帖子列表请求能够返回合法内容。 */
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

    /** 验证真实帖子详情请求能够返回合法内容。 */
    @Test
    fun danbooru_post_request_flow() = runTest {
        DanbooruParser().requestAndLog(
            PictureRequest(
                type = PictureParserType.Post,
                id = "5993705",
            )
        )
    }

    /** 验证真实画集列表请求能够返回合法内容。 */
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

    /** 验证真实画集详情请求能够返回合法内容。 */
    @Test
    fun danbooru_pool_request_flow() = runTest {
        DanbooruParser().requestAndLog(
            PictureRequest(
                type = PictureParserType.Pool,
                id = "18813",
            )
        )
    }

    /** 验证真实热门列表请求能够返回合法内容。 */
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
