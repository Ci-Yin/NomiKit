package ciyin.parser.site.picture

import ciyin.parser.core.ParserEvent
import ciyin.parser.core.picture.PictureParserType
import ciyin.parser.core.picture.model.PictureRequest
import ciyin.parser.core.picture.model.PictureRankingScale
import ciyin.parser.core.picture.model.PictureResult
import ciyin.parser.site.PictureSiteId
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * `YandeParser` 的基础契约与请求流测试。
 */
class YandeParserTest {

    /**
     * 验证 `YandeParser` 初始化后的站点配置契约。
     */
    @Test
    fun parser_configure_should_match_yande_site_contract() {
        val parser = YandeParser()
        val configure = parser.configure

        assertTrue(parser.enable)
        assertEquals(PictureSiteId.Yande, configure.id)
        assertEquals("yande", configure.id.site)
        assertEquals("https://yande.re", configure.baseUrl)
        assertNull(configure.request)
        assertTrue(configure.result.tags.isEmpty())
        assertTrue(configure.result.contents.isEmpty())
        assertEquals(1, configure.result.pagination.page)
        assertNull(configure.result.pagination.totalPages)
    }

    /**
     * 验证帖子列表请求流。
     */
    @Test
    fun yande_posts_request_flow() = runTest {
        YandeParser().requestAndLog(
            PictureRequest(
                type = PictureParserType.Feed,
                page = 1,
                tags = listOf("rating:s"),
            )
        )
    }

    /**
     * 验证帖子详情请求流。
     */
    @Test
    fun yande_post_request_flow() = runTest {
        YandeParser().requestAndLog(
            PictureRequest(
                type = PictureParserType.Detail,
                id = "1256075",
            )
        )
    }

    /**
     * 验证画集列表请求流。
     */
    @Test
    fun yande_pools_request_flow() = runTest {
        YandeParser().requestAndLog(
            PictureRequest(
                type = PictureParserType.CollectionList,
                page = 1,
                search = "",
            )
        )
    }

    /**
     * 验证画集详情请求流。
     */
    @Test
    fun yande_pool_request_flow() = runTest {
        YandeParser().requestAndLog(
            PictureRequest(
                type = PictureParserType.Collection,
                id = "99337",
            ),
        )
    }

    /**
     * 验证热门榜单请求流。
     */
    @Test
    fun yande_popular_request_flow() = runTest {
        YandeParser().requestAndLog(
            PictureRequest(
                type = PictureParserType.Ranking,
                page = 1,
                scale = PictureRankingScale.Day,
            ),
        )
    }

    /**
     * 执行请求并断言返回成功且内容非空。
     */
    private suspend fun YandeParser.requestAndLog(request: PictureRequest): PictureResult {
        var finalResult: PictureResult? = null
        request(request).collect {
            when (it) {
                is ParserEvent.Failure -> error(it.errors)
                is ParserEvent.Success -> {
                    val result = it.result
                    assertTrue(result.contents.isNotEmpty())
                    println(
                        "yande type=${request.type} request=$request -> ok contents=${result.contents.size} pagination=${result.pagination}"
                    )
                    finalResult = result
                }
            }
        }
        return assertNotNull(finalResult)
    }
}
