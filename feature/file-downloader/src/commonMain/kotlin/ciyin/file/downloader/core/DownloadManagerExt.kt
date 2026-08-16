package ciyin.file.downloader.core

import ciyin.file.downloader.model.DownloadConfig
import kotlinx.coroutines.flow.StateFlow

/**
 * 启动一个下载任务。
 *
 * @param config 下载任务的配置。
 */
fun DownloadManager.start(config: DownloadConfig) {
    start(config.id)
}

/**
 * 重新启动一个下载任务。
 *
 * @param config 下载任务的配置。
 */
fun DownloadManager.restart(config: DownloadConfig) {
    restart(config.id)
}


/**
 * 从队列中移除一个任务。
 *
 * @param config 要移除的任务的配置。
 */
fun DownloadManager.removeFromQueue(config: DownloadConfig) {
    removeFromQueue(config.id)
}

/**
 * 暂停一个正在进行的下载任务。
 *
 * @param config 任务的配置。
 */
fun DownloadManager.pause(config: DownloadConfig) {
    pause(config.id)
}

/**
 * 恢复一个已暂停的下载任务。
 *
 * @param config 任务的配置。
 */
fun DownloadManager.resume(config: DownloadConfig) {
    resume(config.id)
}

/**
 * 取消一个下载任务。
 * 这会停止下载并从管理器中移除该任务。
 *
 * @param config 任务的配置。
 */
fun DownloadManager.cancel(config: DownloadConfig) {
    cancel(config.id)
}

/**
 * 获取指定任务的下载状态。
 *
 * @param config 任务的配置。
 * @return 一个包含下载状态的 `StateFlow`，如果任务不存在则返回 `null`。
 */
fun DownloadManager.getDownloadState(config: DownloadConfig): StateFlow<DownloadState>? {
    return getDownloadState(config.id)
}