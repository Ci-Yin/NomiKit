package ciyin.file.downloader

import ciyin.file.downloader.exceptions.ChunkDownloadException
import ciyin.file.downloader.model.DownloadConfig
import okio.Path.Companion.toPath
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * [ChunkCalculator] 的单元测试。
 */
class ChunkCalculatorTest {

    // region calculateChunks 测试

    /** 总大小为零时不应生成分块。 */
    @Test
    fun calculateChunks_totalSizeZero_returnsEmptyList() {
        val chunks = ChunkCalculator.calculateChunks(
            totalSize = 0L,
            chunkSize = 4 * 1024 * 1024L,
            chunkDir = "/tmp/chunks".toPath(),
        )
        assertTrue(chunks.isEmpty())
    }

    /** 总大小为负数时不应生成分块。 */
    @Test
    fun calculateChunks_totalSizeNegative_returnsEmptyList() {
        val chunks = ChunkCalculator.calculateChunks(
            totalSize = -1L,
            chunkSize = 4 * 1024 * 1024L,
            chunkDir = "/tmp/chunks".toPath(),
        )
        assertTrue(chunks.isEmpty())
    }

    /** 文件大小整除分块大小时应生成等长分块。 */
    @Test
    fun calculateChunks_exactDivision_returnsCorrectChunks() {
        val chunkSize = 100L
        val totalSize = 300L // 整除：300 / 100 = 3 个分块

        val chunks = ChunkCalculator.calculateChunks(
            totalSize = totalSize,
            chunkSize = chunkSize,
            chunkDir = "/tmp/chunks".toPath(),
        )

        assertEquals(3, chunks.size)

        // 分块 0: [0, 99], size=100
        assertEquals(0, chunks[0].index)
        assertEquals(0L, chunks[0].start)
        assertEquals(99L, chunks[0].end)
        assertEquals(100L, chunks[0].size)

        // 分块 1: [100, 199], size=100
        assertEquals(1, chunks[1].index)
        assertEquals(100L, chunks[1].start)
        assertEquals(199L, chunks[1].end)
        assertEquals(100L, chunks[1].size)

        // 分块 2: [200, 299], size=100
        assertEquals(2, chunks[2].index)
        assertEquals(200L, chunks[2].start)
        assertEquals(299L, chunks[2].end)
        assertEquals(100L, chunks[2].size)
    }

    /** 仅余一个字节时末块范围应准确。 */
    @Test
    fun calculateChunks_remainderOneByte_lastChunkHasCorrectSize() {
        val chunkSize = 100L
        val totalSize = 201L // 余 1 字节：201 / 100 = 2 个完整分块 + 1 字节

        val chunks = ChunkCalculator.calculateChunks(
            totalSize = totalSize,
            chunkSize = chunkSize,
            chunkDir = "/tmp/chunks".toPath(),
        )

        assertEquals(3, chunks.size)

        // 分块 0: [0, 99], size=100
        assertEquals(0, chunks[0].index)
        assertEquals(0L, chunks[0].start)
        assertEquals(99L, chunks[0].end)
        assertEquals(100L, chunks[0].size)

        // 分块 1: [100, 199], size=100
        assertEquals(1, chunks[1].index)
        assertEquals(100L, chunks[1].start)
        assertEquals(199L, chunks[1].end)
        assertEquals(100L, chunks[1].size)

        // 分块 2: [200, 200], size=1 (只剩 1 字节)
        assertEquals(2, chunks[2].index)
        assertEquals(200L, chunks[2].start)
        assertEquals(200L, chunks[2].end)
        assertEquals(1L, chunks[2].size)
    }

    /** 文件小于分块大小时应只生成一个分块。 */
    @Test
    fun calculateChunks_totalSizeLessThanChunkSize_returnsSingleChunk() {
        val chunkSize = 100L
        val totalSize = 50L // 文件小于分块大小

        val chunks = ChunkCalculator.calculateChunks(
            totalSize = totalSize,
            chunkSize = chunkSize,
            chunkDir = "/tmp/chunks".toPath(),
        )

        assertEquals(1, chunks.size)
        assertEquals(0, chunks[0].index)
        assertEquals(0L, chunks[0].start)
        assertEquals(49L, chunks[0].end)
        assertEquals(50L, chunks[0].size)
    }

    /** 单字节文件应生成一个单字节分块。 */
    @Test
    fun calculateChunks_singleByte_returnsSingleChunk() {
        val chunkSize = 100L
        val totalSize = 1L // 只有 1 字节

        val chunks = ChunkCalculator.calculateChunks(
            totalSize = totalSize,
            chunkSize = chunkSize,
            chunkDir = "/tmp/chunks".toPath(),
        )

        assertEquals(1, chunks.size)
        assertEquals(0, chunks[0].index)
        assertEquals(0L, chunks[0].start)
        assertEquals(0L, chunks[0].end)
        assertEquals(1L, chunks[0].size)
    }

    /** 文件与分块大小相等时应只生成一个分块。 */
    @Test
    fun calculateChunks_totalSizeEqualsChunkSize_returnsSingleChunk() {
        val chunkSize = 100L
        val totalSize = 100L // 刚好等于分块大小

        val chunks = ChunkCalculator.calculateChunks(
            totalSize = totalSize,
            chunkSize = chunkSize,
            chunkDir = "/tmp/chunks".toPath(),
        )

        assertEquals(1, chunks.size)
        assertEquals(0, chunks[0].index)
        assertEquals(0L, chunks[0].start)
        assertEquals(99L, chunks[0].end)
        assertEquals(100L, chunks[0].size)
    }

    /** 分块临时路径应按索引稳定生成。 */
    @Test
    fun calculateChunks_chunkPathsAreCorrect() {
        val chunkDir = "/tmp/test_chunks".toPath()

        val chunks = ChunkCalculator.calculateChunks(
            totalSize = 250L,
            chunkSize = 100L,
            chunkDir = chunkDir,
        )

        assertEquals(3, chunks.size)
        assertEquals("/tmp/test_chunks/chunk_0".toPath(), chunks[0].tempPath)
        assertEquals("/tmp/test_chunks/chunk_1".toPath(), chunks[1].tempPath)
        assertEquals("/tmp/test_chunks/chunk_2".toPath(), chunks[2].tempPath)
    }

    // endregion

    // region shouldUseChunkedDownload 测试

    /** 所有分块条件满足时应启用分块下载。 */
    @Test
    fun shouldUseChunkedDownload_allConditionsMet_returnsTrue() {
        val remoteInfo = RemoteFileInfo(
            totalSize = 10 * 1024 * 1024L, // 10MB
            supportsRange = true,
        )
        val config = DownloadConfig(
            url = "https://example.com/file.zip",
            savePath = "/tmp/file.zip",
            enableChunkedDownload = true,
            chunkSize = 4 * 1024 * 1024L, // 4MB
        )

        assertTrue(ChunkCalculator.shouldUseChunkedDownload(remoteInfo, config))
    }

    /** 配置关闭分块下载时不应启用。 */
    @Test
    fun shouldUseChunkedDownload_chunkedDisabled_returnsFalse() {
        val remoteInfo = RemoteFileInfo(
            totalSize = 10 * 1024 * 1024L,
            supportsRange = true,
        )
        val config = DownloadConfig(
            url = "https://example.com/file.zip",
            savePath = "/tmp/file.zip",
            enableChunkedDownload = false, // 未启用
            chunkSize = 4 * 1024 * 1024L,
        )

        assertFalse(ChunkCalculator.shouldUseChunkedDownload(remoteInfo, config))
    }

    /** 远程文件大小未知时不应启用分块下载。 */
    @Test
    fun shouldUseChunkedDownload_unknownTotalSize_returnsFalse() {
        val remoteInfo = RemoteFileInfo(
            totalSize = -1L, // 未知大小
            supportsRange = true,
        )
        val config = DownloadConfig(
            url = "https://example.com/file.zip",
            savePath = "/tmp/file.zip",
            enableChunkedDownload = true,
            chunkSize = 4 * 1024 * 1024L,
        )

        assertFalse(ChunkCalculator.shouldUseChunkedDownload(remoteInfo, config))
    }

    /** 远程文件大小为零时不应启用分块下载。 */
    @Test
    fun shouldUseChunkedDownload_zeroTotalSize_returnsFalse() {
        val remoteInfo = RemoteFileInfo(
            totalSize = 0L, // 大小为 0
            supportsRange = true,
        )
        val config = DownloadConfig(
            url = "https://example.com/file.zip",
            savePath = "/tmp/file.zip",
            enableChunkedDownload = true,
            chunkSize = 4 * 1024 * 1024L,
        )

        assertFalse(ChunkCalculator.shouldUseChunkedDownload(remoteInfo, config))
    }

    /** 服务端不支持 Range 时不应启用分块下载。 */
    @Test
    fun shouldUseChunkedDownload_rangeNotSupported_returnsFalse() {
        val remoteInfo = RemoteFileInfo(
            totalSize = 10 * 1024 * 1024L,
            supportsRange = false, // 不支持 Range
        )
        val config = DownloadConfig(
            url = "https://example.com/file.zip",
            savePath = "/tmp/file.zip",
            enableChunkedDownload = true,
            chunkSize = 4 * 1024 * 1024L,
        )

        assertFalse(ChunkCalculator.shouldUseChunkedDownload(remoteInfo, config))
    }

    /** 文件小于单块大小时不应启用分块下载。 */
    @Test
    fun shouldUseChunkedDownload_fileSizeLessThanChunkSize_returnsFalse() {
        val remoteInfo = RemoteFileInfo(
            totalSize = 2 * 1024 * 1024L, // 2MB
            supportsRange = true,
        )
        val config = DownloadConfig(
            url = "https://example.com/file.zip",
            savePath = "/tmp/file.zip",
            enableChunkedDownload = true,
            chunkSize = 4 * 1024 * 1024L, // 分块大小 4MB > 文件大小 2MB
        )

        assertFalse(ChunkCalculator.shouldUseChunkedDownload(remoteInfo, config))
    }

    /** 文件等于单块大小时允许启用分块下载。 */
    @Test
    fun shouldUseChunkedDownload_fileSizeEqualsChunkSize_returnsTrue() {
        val remoteInfo = RemoteFileInfo(
            totalSize = 4 * 1024 * 1024L, // 4MB
            supportsRange = true,
        )
        val config = DownloadConfig(
            url = "https://example.com/file.zip",
            savePath = "/tmp/file.zip",
            enableChunkedDownload = true,
            chunkSize = 4 * 1024 * 1024L, // 刚好等于
        )

        assertTrue(ChunkCalculator.shouldUseChunkedDownload(remoteInfo, config))
    }

    // endregion

    // region validateChunkResults 测试

    /** 所有分块成功时校验不应抛出异常。 */
    @Test
    fun validateChunkResults_allChunksSucceeded_doesNotThrow() {
        ChunkCalculator.validateChunkResults(listOf(true, true, true))
    }

    /** 分块失败时异常应包含失败索引。 */
    @Test
    fun validateChunkResults_someChunksFailed_throwsWithFailedIndices() {
        val exception = assertFailsWith<ChunkDownloadException> {
            ChunkCalculator.validateChunkResults(listOf(true, false, true, false))
        }

        assertEquals(listOf(1, 3), exception.failedChunks)
        assertEquals("分块下载失败: [1, 3]", exception.message)
    }

    // endregion
}
