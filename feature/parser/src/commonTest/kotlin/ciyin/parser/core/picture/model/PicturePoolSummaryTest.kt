package ciyin.parser.core.picture.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * 图片画集摘要模型契约测试。
 */
class PicturePoolSummaryTest {

    /** 缺失图片数量必须保持未知语义，不能伪造成零。 */
    @Test
    fun missingPostCountRemainsNull() {
        val summary = PoolSummary(
            poolId = 42,
            title = "sample_pool",
            postCount = null,
            url = "https://example.test/pools/42",
        )
        val picture = Picture(poolSummary = summary)

        assertEquals(42, picture.poolSummary?.poolId)
        assertEquals("sample_pool", picture.poolSummary?.title)
        assertNull(picture.poolSummary?.postCount)
        assertEquals("https://example.test/pools/42", picture.poolSummary?.url)
    }
}
