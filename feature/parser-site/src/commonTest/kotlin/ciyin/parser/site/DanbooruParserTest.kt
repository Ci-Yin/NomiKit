package ciyin.parser.site

import ciyin.parser.site.picture.DanbooruParser
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

}
