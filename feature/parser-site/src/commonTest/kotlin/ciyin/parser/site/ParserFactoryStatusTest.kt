package ciyin.parser.site

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * 站点枚举与解析器工厂契约测试。
 */
class ParserFactoryUnimplementedTest {

    /** 未实现的图片站点必须保留在枚举中，并由工厂抛出明确未实现错误。 */
    @Test
    fun unimplementedPictureSitesExposeExplicitFactoryErrors() {
        listOf(PictureSiteId.Rule34, PictureSiteId.Konachan)
            .forEach { site ->
                val error = assertFailsWith<IllegalStateException> { site.factory() }
                assertTrue(error.message.orEmpty().contains(site.site))
                assertTrue(error.message.orEmpty().contains("尚未实现解析器"))
            }
    }

    /** 未实现的漫画站点必须保留在枚举中，并由工厂抛出明确未实现错误。 */
    @Test
    fun unimplementedComicSitesExposeExplicitFactoryErrors() {
        listOf(
            ComicSiteId.Hentai,
            ComicSiteId.Nhentai,
            ComicSiteId.Mangacopy,
            ComicSiteId.Ehentai,
        ).forEach { site ->
            val error = assertFailsWith<IllegalStateException> { site.factory() }
            assertTrue(error.message.orEmpty().contains(site.site))
            assertTrue(error.message.orEmpty().contains("尚未实现解析器"))
        }
    }

    /** 未实现的影视站点必须保留在枚举中，并由工厂抛出明确未实现错误。 */
    @Test
    fun unimplementedMovieSitesExposeExplicitFactoryErrors() {
        listOf(MovieSiteId.Age, MovieSiteId.HanimeJav, MovieSiteId.Qkan8)
            .forEach { site ->
                val error = assertFailsWith<IllegalStateException> { site.factory() }
                assertTrue(error.message.orEmpty().contains(site.site))
                assertTrue(error.message.orEmpty().contains("尚未实现解析器"))
            }
    }
}
