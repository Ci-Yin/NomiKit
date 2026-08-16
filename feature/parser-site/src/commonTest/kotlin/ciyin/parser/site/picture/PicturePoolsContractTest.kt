package ciyin.parser.site.picture

import ciyin.parser.core.picture.model.PictureRequest
import ciyin.parser.core.picture.PictureParserType
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Danbooru 与 Yande 画集摘要及请求隔离契约测试。
 */
class PicturePoolsContractTest {

    /** Danbooru 以 JSON pool 为权威并按 pool id 合并 HTML 首图。 */
    @Test
    fun danbooruPoolsMergeJsonSummariesWithHtmlCoversByPoolId() {
        val pictures = parseDanbooruPools(
            html = """
                <div class="posts-container">
                  <article class="post-preview" data-id="9002">
                    <a class="post-preview-link" href="/pools/102"><img src="https://img/cover-102.jpg" width="300" height="200"></a>
                  </article>
                  <article class="post-preview" data-id="9001">
                    <a class="post-preview-link" href="/pools/101"><img src="https://img/cover-101.jpg" width="200" height="300"></a>
                  </article>
                </div>
            """.trimIndent(),
            json = """
                [
                  {"id":101,"name":"first_pool","post_count":12},
                  {"id":102,"name":"second_pool"},
                  {"id":103,"name":"empty_cover","post_count":4}
                ]
            """.trimIndent(),
            baseUrl = "https://danbooru.donmai.us",
        )

        assertEquals(listOf(101, 102, 103), pictures.map { it.poolSummary?.poolId })
        assertEquals(listOf("first_pool", "second_pool", "empty_cover"), pictures.map { it.poolSummary?.title })
        assertEquals(12, pictures[0].poolSummary?.postCount)
        assertNull(pictures[1].poolSummary?.postCount)
        assertEquals("https://img/cover-101.jpg", pictures[0].thumbnailUrl)
        assertEquals("https://img/cover-102.jpg", pictures[1].thumbnailUrl)
        assertEquals("", pictures[2].thumbnailUrl)
        assertTrue(pictures.all { it.site == "danbooru" })
    }

    /** Yande 一项对应一个 JSON pool，缺失首图和数量都保持显式未知。 */
    @Test
    fun yandePoolsKeepEveryJsonSummaryWhenCoverIsMissing() {
        val pictures = parseYandePools(
            html = """
                <html><body><script>
                  Post.register({"id":7001,"preview_url":"https://img/yande-1.jpg","sample_url":"https://img/yande-1-s.jpg","file_url":"https://img/yande-1-o.jpg","width":640,"height":480,"rating":"s"});
                </script></body></html>
            """.trimIndent(),
            json = """
                [
                  {"id":201,"name":"yande_first","post_count":8},
                  {"id":202,"name":"yande_without_cover"}
                ]
            """.trimIndent(),
            baseUrl = "https://yande.re",
        )

        assertEquals(listOf(201, 202), pictures.map { it.poolSummary?.poolId })
        assertEquals(8, pictures[0].poolSummary?.postCount)
        assertNull(pictures[1].poolSummary?.postCount)
        assertEquals("https://img/yande-1.jpg", pictures[0].thumbnailUrl)
        assertEquals("", pictures[1].thumbnailUrl)
        assertTrue(pictures.all { it.site == "yande" })
    }

    /** 三种列表类型必须构建互不串用的请求地址。 */
    @Test
    fun pictureRequestPlansKeepPostsPopularAndPoolsSeparated() {
        val requests = listOf(
            PictureRequest(type = PictureParserType.Posts, page = 2),
            PictureRequest(type = PictureParserType.Popular, page = 2, scale = "week"),
            PictureRequest(type = PictureParserType.Pools, page = 2),
        )

        val danbooru = requests.map(::danbooruPictureRequestPlan)
        val yande = requests.map(::yandePictureRequestPlan)

        assertEquals(listOf("/posts", "/explore/posts/popular", "/pools/gallery"), danbooru.map { it.htmlPath })
        assertEquals(listOf("/post", "/post/popular_by_week", "/pool"), yande.map { it.htmlPath })
        assertEquals("week", danbooru[1].parameters["scale"])
        assertTrue(yande[1].parameters.keys.containsAll(listOf("year", "month", "day")))
    }

    /** 两站热门都只接受明确支持的日周月范围。 */
    @Test
    fun popularRangeRejectsUnsupportedScaleExplicitly() {
        listOf("day", "week", "month").forEach { scale ->
            val request = PictureRequest(type = PictureParserType.Popular, scale = scale)
            assertTrue(danbooruPictureRequestPlan(request).htmlPath.contains("popular"))
            assertTrue(yandePictureRequestPlan(request).htmlPath.endsWith("popular_by_$scale"))
        }

        val unsupported = PictureRequest(type = PictureParserType.Popular, scale = "year")
        assertFailsWith<IllegalArgumentException> { danbooruPictureRequestPlan(unsupported) }
        assertFailsWith<IllegalArgumentException> { yandePictureRequestPlan(unsupported) }
    }

    /** Yande 热门第一页使用今天，后续页按日周月各自周期推进。 */
    @Test
    fun yandePopularPaginationUsesRangePeriodFromToday() {
        val today = LocalDate(year = 2026, month = 3, day = 31)

        assertEquals(
            "2026-3-31",
            yandePictureRequestPlan(
                request = PictureRequest(type = PictureParserType.Popular, page = 1, scale = "day"),
                today = today,
            ).dateValue(),
        )
        assertEquals(
            "2026-3-29",
            yandePictureRequestPlan(
                request = PictureRequest(type = PictureParserType.Popular, page = 3, scale = "day"),
                today = today,
            ).dateValue(),
        )
        assertEquals(
            "2026-3-24",
            yandePictureRequestPlan(
                request = PictureRequest(type = PictureParserType.Popular, page = 2, scale = "week"),
                today = today,
            ).dateValue(),
        )
        assertEquals(
            "2026-2-28",
            yandePictureRequestPlan(
                request = PictureRequest(type = PictureParserType.Popular, page = 2, scale = "month"),
                today = today,
            ).dateValue(),
        )
    }

    /** 返回请求计划中的年月日参数。 */
    private fun PictureSiteRequestPlan.dateValue(): String =
        "${parameters.getValue("year")}-${parameters.getValue("month")}-${parameters.getValue("day")}"
}
