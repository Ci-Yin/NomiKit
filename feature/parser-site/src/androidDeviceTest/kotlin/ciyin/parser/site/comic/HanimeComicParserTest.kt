package ciyin.parser.site.comic

import ciyin.parser.core.comic.ComicParserType
import ciyin.parser.core.comic.model.ComicRequest
import ciyin.parser.site.ComicSiteId
import ciyin.parser.site.requestAndLog
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * `HanimeComicParser` / `HanimeComicBaseParser` 的基础契约与请求流测试。
 *
 * 参考 `picture.DanbooruParserTest` 的风格：
 * - 校验解析器初始化时的站点配置；
 * - 运行基础的 Comics 请求流并断言结果非空。
 */
class HanimeComicParserTest {


    /**
     * 验证 `HanimeComicParser` 初始化后的站点配置契约。
     */
    @Test
    fun parser_configure_should_match_hanime_comic_site_contract() {

        val parser = HanimeComicParser()
        val configure = parser.configure

        assertTrue(parser.enable)
        assertEquals(ComicSiteId.Hanime, configure.id)
        assertEquals("hanime1", configure.id.site)
        assertEquals("https://hanimeone.me", configure.baseUrl)
        assertNull(configure.request)
        assertTrue(configure.result.tags.isEmpty())
        assertTrue(configure.result.contents.isEmpty())
        assertEquals(0, configure.result.totalPages)
    }

    /**
     * 验证漫画列表请求流（Comics）。
     *
     * 这里沿用项目内「基础流测试」的做法，仅在有网络环境下保证
     * 能够返回至少 1 条内容。
     */
    @Test
    fun hanime_comics_request_flow() = runTest {
        HanimeComicParser().requestAndLog(
            ComicRequest(
                type = ComicParserType.Comics,
                page = 1,
            )
        )
    }

    @Test
    fun hanime_comic_request_flow() = runTest {
        HanimeComicParser().requestAndLog(
            ComicRequest(
                type = ComicParserType.Comic,
                id = "94553",
            )
        )
    }
}
