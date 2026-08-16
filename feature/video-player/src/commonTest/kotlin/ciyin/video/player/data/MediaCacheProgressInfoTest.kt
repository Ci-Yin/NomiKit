package ciyin.video.player.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/** 媒体缓存进度模型测试。 */
class MediaCacheProgressInfoTest {

    /** 空缓存进度没有区块。 */
    @Test
    fun emptyProgressHasNoChunks() {
        assertTrue(MediaCacheProgressInfo.Empty.isEmpty())
        assertEquals(0, MediaCacheProgressInfo.Empty.size)
    }

    /** 区块权重与状态数量必须相同。 */
    @Test
    fun rejectsMismatchedChunkCounts() {
        assertFailsWith<IllegalArgumentException> {
            MediaCacheProgressInfo(
                chunkWeights = listOf(1f),
                chunkStates = emptyList(),
            )
        }
    }
}
