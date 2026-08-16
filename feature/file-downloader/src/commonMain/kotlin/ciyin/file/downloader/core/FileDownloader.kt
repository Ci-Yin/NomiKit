package ciyin.file.downloader.core

import ciyin.file.downloader.model.DownloadConfig
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow

/**
 * 跨平台文件下载器
 */
interface FileDownloader {
    /**
     * 下载状态流
     */
    val state: StateFlow<DownloadState>

    /**
     * 恢复空闲状态
     */
    fun idle()

    /**
     * 开始下载
     */
    fun download(config: DownloadConfig): Job

    /**
     * 重新开始下载
     */
    fun restart(): Job

    /**
     * 暂停下载
     */
    fun pause()

    /**
     * 恢复下载
     */
    fun resume(): Job

    /**
     * 取消下载
     */
    fun cancel()

    /**
     * 清理临时文件
     */
    fun cleanup()
}
