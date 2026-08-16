package ciyin.file.downloader.model

/**
 * 下载队列中的一个项目。
 *
 * @property taskId 任务的唯一标识符。
 * @property config 下载任务的配置。
 * @property priority 任务的优先级，数值越大优先级越高。
 */
data class QueueTask(
    val taskId: Int,
    val config: DownloadConfig,
    val priority: Int
)