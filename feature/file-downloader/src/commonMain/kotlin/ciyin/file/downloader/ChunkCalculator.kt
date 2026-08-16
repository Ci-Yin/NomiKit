package ciyin.file.downloader

import ciyin.file.downloader.exceptions.ChunkDownloadException
import ciyin.file.downloader.model.DownloadConfig
import ciyin.file.downloader.util.toChunkFilePath
import okio.Path
import kotlin.math.min

/**
 * 远程文件信息。
 *
 * @property totalSize 文件总大小（字节），-1 表示未知。
 * @property supportsRange 是否支持 Range 请求。
 */
internal data class RemoteFileInfo(
    val totalSize: Long,
    val supportsRange: Boolean,
)

/**
 * 分块信息。
 *
 * @property index 分块索引。
 * @property start 起始字节偏移。
 * @property end 结束字节偏移（包含）。
 * @property size 分块大小。
 * @property tempPath 分块临时文件路径。
 * @property downloadedBytes 已下载字节数。
 */
internal data class ChunkInfo(
    val index: Int,
    val start: Long,
    val end: Long,
    val size: Long,
    val tempPath: Path,
    var downloadedBytes: Long = 0L,
)

/**
 * 分块计算器，负责分块策略的计算。
 */
internal object ChunkCalculator {

    /**
     * 计算分块列表。
     *
     * @param totalSize 文件总大小。
     * @param chunkSize 每个分块大小。
     * @param chunkDir 分块临时目录路径。
     * @return 分块信息列表。当 [totalSize] <= 0 时返回空列表。
     */
    fun calculateChunks(totalSize: Long, chunkSize: Long, chunkDir: Path): List<ChunkInfo> {
        if (totalSize <= 0) return emptyList()

        val chunks = mutableListOf<ChunkInfo>()
        var index = 0
        var start = 0L

        while (start < totalSize) {
            val end = min(start + chunkSize - 1, totalSize - 1)
            chunks.add(
                ChunkInfo(
                    index = index,
                    start = start,
                    end = end,
                    size = end - start + 1,
                    tempPath = chunkDir.toChunkFilePath(index),
                )
            )
            index++
            start = end + 1
        }

        return chunks
    }

    /**
     * 判断是否应使用分块下载。
     *
     * 需要同时满足以下条件：
     * 1. 配置启用了分块下载
     * 2. 远程文件大小已知（> 0）
     * 3. 服务器支持 Range 请求
     * 4. 文件大小不小于分块大小
     *
     * @param remoteInfo 远程文件信息。
     * @param config 下载配置。
     * @return `true` 表示应使用分块下载。
     */
    fun shouldUseChunkedDownload(
        remoteInfo: RemoteFileInfo,
        config: DownloadConfig,
    ): Boolean {
        return config.enableChunkedDownload &&
                remoteInfo.totalSize > 0 &&
                remoteInfo.supportsRange &&
                remoteInfo.totalSize >= config.chunkSize
    }

    /**
     * 校验分块下载结果。
     *
     * @param results 按分块索引顺序排列的下载结果。
     * @throws ChunkDownloadException 当存在下载失败的分块时抛出。
     */
    fun validateChunkResults(results: List<Boolean>) {
        val failedIndices = results.mapIndexedNotNull { index, success ->
            if (success) null else index
        }

        if (failedIndices.isNotEmpty()) {
            throw ChunkDownloadException(
                failedChunks = failedIndices,
                message = "分块下载失败: $failedIndices",
            )
        }
    }
}
