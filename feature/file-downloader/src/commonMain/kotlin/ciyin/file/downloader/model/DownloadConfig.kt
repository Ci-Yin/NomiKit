package ciyin.file.downloader.model
import ciyin.file.downloader.core.FileDownloader
/**
 * 下载任务的配置信息。
 *
 * @property url 文件的下载地址。
 * @property savePath 文件的保存路径。
 * @property id 任务的唯一标识符，默认由 url 和 savePath 的哈希码生成。
 * @property headers 下载请求的自定义 HTTP 头。
 * @property timeout 连接和读取的超时时间（毫秒）。
 * @property bufferSize 下载时使用的缓冲区大小（字节）。
 * @property enableResume 是否启用断点续传。**注意：分块下载模式（[enableChunkedDownload]）下此配置会被忽略，分块模式不支持断点续传。**
 * @property enableDownloadProgress 是否启用下载进度监听。
 * @property maxRetries 下载失败时的最大重试次数。
 * @property retryDelayMs 每次重试之间的延迟时间（毫秒）。
 * @property speedLimitBytesPerSecond 下载速度限制（字节/秒），0 表示不限速。
 * @property progressUpdateInterval 进度更新回调的最小间隔时间（毫秒）。
 * @property overwriteExisting 下载时若目标文件已存在，是否允许覆盖（默认 false），[FileDownloader.restart] 会无视该规则。
 * @property enableChunkedDownload 是否启用分块下载，默认关闭。启用后将文件拆分为多个分块并行下载，提升网络不稳定场景下的下载成功率。**注意：分块模式不支持断点续传，且需要服务器支持 Range 请求。**
 * @property chunkSize 分块下载时每个分块的大小（字节），默认 4MB。仅在 [enableChunkedDownload] 为 `true` 时生效。
 * @property maxConcurrentChunks 分块下载时的最大并发分块数，默认 3。仅在 [enableChunkedDownload] 为 `true` 时生效。
 * @property chunkMaxRetries 单个分块的最大重试次数，默认 3。仅在 [enableChunkedDownload] 为 `true` 时生效。
 * @property chunkRetryDelayMs 单个分块重试延迟（毫秒），默认 1000。仅在 [enableChunkedDownload] 为 `true` 时生效。
 */
data class DownloadConfig(
    val url: String,
    val savePath: String,
    val id: Int = (url + savePath).hashCode(),
    val headers: Map<String, String> = emptyMap(),
    val timeout: Long = 30000L,
    val bufferSize: Int = 8192,
    val enableResume: Boolean = true,
    val enableDownloadProgress: Boolean = true,
    val maxRetries: Int = 3,
    val retryDelayMs: Long = 1000L,
    val speedLimitBytesPerSecond: Long = 0L,
    val progressUpdateInterval: Long = 100L,
    val overwriteExisting: Boolean = false,
    val enableChunkedDownload: Boolean = false,
    val chunkSize: Long = 4 * 1024 * 1024L,
    val maxConcurrentChunks: Int = 3,
    val chunkMaxRetries: Int = 3,
    val chunkRetryDelayMs: Long = 1000L,
)
