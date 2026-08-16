package ciyin.file.downloader.core

import okio.Path

/**
 * 下载任务状态定义。
 *
 * 该状态模型用于描述下载生命周期中的各阶段，
 * 供上层状态管理与 UI 渲染消费。
 */
sealed interface DownloadState {
    /**
     * 空闲状态，表示当前尚未开始下载。
     */
    object Idle : DownloadState

    /**
     * 启动状态，表示下载流程已开始初始化。
     */
    object Start : DownloadState

    /**
     * 下载中状态。
     *
     * @property progress 当前进度，取值范围通常为 `0f..1f`。
     * @property downloaded 当前已下载字节数。
     * @property total 文件总字节数。
     * @property speed 当前下载速度，单位为字节每秒（B/s）。
     */
    data class Downloading(
        val progress: Float,
        val downloaded: Long,
        val total: Long,
        val speed: Long
    ) : DownloadState

    /**
     * 暂停状态，表示下载被主动暂停。
     */
    object Paused : DownloadState

    /**
     * 恢复状态，表示下载从暂停中恢复。
     */
    object Resumed : DownloadState

    /**
     * 取消状态，表示下载已被取消且不再继续。
     */
    object Cancelled : DownloadState

    /**
     * 完成状态。
     *
     * @property filePath 下载完成后文件的本地路径。
     */
    data class Complete(val filePath: Path) : DownloadState

    /**
     * 失败状态。
     *
     * @property message 可选错误描述信息。
     * @property exception 导致下载失败的异常对象。
     */
    data class Error(val message: String?, val exception: Throwable) : DownloadState
}
