package ciyin.parser.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

/**
 * 分页状态模型契约测试。
 */
class PaginationTest {

    /** 默认分页从第 1 页开始，未知总页数和下一页状态保持为空。 */
    @Test
    fun defaultsRepresentUnknownListBounds() {
        val pagination = Pagination()

        assertEquals(1, pagination.page)
        assertNull(pagination.totalPages)
        assertNull(pagination.hasNext)
    }

    /** 分页页码和已知总页数必须使用正数。 */
    @Test
    fun invalidPageBoundsAreRejected() {
        assertFailsWith<IllegalArgumentException> { Pagination(page = 0) }
        assertFailsWith<IllegalArgumentException> { Pagination(totalPages = 0) }
    }
}
