package ciyin.file.downloader.core

import ciyin.file.downloader.model.QueueTask

/**
 * 表示下载任务在 `DownloadManager` 中的生命周期状态。
 */
sealed interface DownloadTaskState {

    /**
     * 任务已创建，但尚未开始或排队。
     * @property taskId 分配给新任务的唯一标识符。
     */
    data class Create(val taskId: Int) : DownloadTaskState

    /**
     * 任务已添加到下载队列，正在等待执行。
     * @property queueTask 包含任务详情的队列项。
     */
    data class AddToQueue(val queueTask: QueueTask) : DownloadTaskState

    /**
     * 任务已从下载队列中移除。
     * @property queueTask 被移除的任务的详情。
     */
    data class RemoveQueue(val queueTask: QueueTask) : DownloadTaskState

    /**
     * 所有任务都已从队列中清空。
     */
    data object ClearQueue : DownloadTaskState

    /**
     * 任务开始下载。
     * @property queueTask 正在下载的任务。
     */
    data class Start(val queueTask: QueueTask) : DownloadTaskState

    /**
     * 任务的下载状态。
     * @property queueTask 正在下载的任务。
     * @property downloadState 当前下载状态。
     */
    data class DownloadState(val queueTask: QueueTask, val downloadState: ciyin.file.downloader.core.DownloadState) :
        DownloadTaskState

    /**
     * 任务已被暂停。
     * @property queueTask 被暂停的任务。
     */
    data class Pause(val queueTask: QueueTask) : DownloadTaskState

    /**
     * 任务已从暂停状态恢复。
     * @property queueTask 被恢复的任务。
     */
    data class Resume(val queueTask: QueueTask) : DownloadTaskState

    /**
     * 任务已被取消。
     * @property queueTask 被取消的任务。
     */
    data class Cancel(val queueTask: QueueTask) : DownloadTaskState

    /**
     * 所有任务已完成。
     */
    data object AllTasksCompleted : DownloadTaskState
}
