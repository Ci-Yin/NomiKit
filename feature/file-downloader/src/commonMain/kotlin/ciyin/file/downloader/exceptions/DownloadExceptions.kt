package ciyin.file.downloader.exceptions

/**
 * 当目标文件已存在但不允许覆盖时抛出。
 *
 * @property path 已存在的目标路径。
 */
class DownloadFileExistsException(
    val path: String,
) : RuntimeException("目标文件已经存在: $path")

/**
 * 当尝试覆盖文件但失败时抛出。
 *
 * @property path 覆盖失败的目标路径。
 */
class DownloadOverwriteFailedException(
    val path: String,
    cause: Throwable? = null,
) : RuntimeException("未能覆盖现有文件: $path", cause)

/**
 * 当服务端返回的 HTTP 状态码不符合下载预期时抛出。
 *
 * @property statusCode HTTP 状态码。
 * @property url 下载地址。
 * @property details 额外错误细节。
 */
class DownloadHttpStatusException(
    val statusCode: Int,
    val url: String,
    val details: String? = null,
) : RuntimeException(
    buildString {
        append("下载请求返回异常状态码: ")
        append(statusCode)
        append(", url=")
        append(url)
        if (details.isNullOrBlank().not()) {
            append(", 详情=")
            append(details)
        }
    }
)

/**
 * 当断点续传返回的 Content-Range 起始偏移与本地临时文件大小不一致时抛出。
 *
 * @property expectedStart 期望的起始偏移（本地已下载字节数）。
 * @property actualStart 服务端返回的实际起始偏移。
 */
class DownloadContentRangeMismatchException(
    val expectedStart: Long,
    val actualStart: Long,
) : RuntimeException("断点续传偏移不一致，期望=$expectedStart，实际=$actualStart")

/**
 * 当分块下载中部分分块失败时抛出。
 *
 * @property failedChunks 失败的分块索引列表。
 * @property message 错误信息。
 */
class ChunkDownloadException(
    val failedChunks: List<Int>,
    override val message: String,
) : RuntimeException(message)
