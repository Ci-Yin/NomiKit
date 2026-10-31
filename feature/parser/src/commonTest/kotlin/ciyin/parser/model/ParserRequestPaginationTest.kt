package ciyin.parser.model

import ciyin.parser.core.comic.model.ComicRequest
import ciyin.parser.core.movie.model.MovieRequest
import ciyin.parser.core.picture.PictureParserType
import ciyin.parser.core.picture.model.PictureRequest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * 三类业务请求的页码契约测试。
 */
class ParserRequestPaginationTest {

    /** 三类业务请求默认从第 1 页开始，且不携带通用分页大小或游标字段。 */
    @Test
    fun requestsDefaultToFirstPage() {
        assertEquals(1, PictureRequest(type = PictureParserType.Feed).page)
        assertEquals(1, ComicRequest().page)
        assertEquals(1, MovieRequest().page)
    }

    /** 三类业务请求拒绝小于 1 的页码。 */
    @Test
    fun requestsRejectNonPositivePages() {
        assertFailsWith<IllegalArgumentException> {
            PictureRequest(type = PictureParserType.Feed, page = 0)
        }
        assertFailsWith<IllegalArgumentException> { ComicRequest(page = 0) }
        assertFailsWith<IllegalArgumentException> { MovieRequest(page = 0) }
    }
}
