package com.ciyin.app.ui.screen.filedownloader

import androidx.compose.runtime.Immutable

/**
 * 文件下载示例页面状态。
 *
 * @property url 下载地址。
 * @property savePath 文件保存路径。
 * @property phase 当前下载阶段。
 * @property progress 下载进度，未知时为 `null`。
 * @property downloadedBytes 已下载字节数。
 * @property totalBytes 总字节数，未知时为 `0`。
 * @property speedBytesPerSecond 当前每秒下载字节数。
 * @property enableResume 是否启用断点续传。
 * @property enableChunkedDownload 是否启用分块下载。
 * @property overwriteExisting 是否覆盖已有目标文件。
 * @property completedPath 下载完成后的文件路径。
 * @property errorMessage 最近一次错误信息。
 */
@Immutable
internal data class FileDownloaderDemoUiState(
    val url: String = DefaultDownloadUrl,
    val savePath: String = "",
    val phase: FileDownloaderDemoPhase = FileDownloaderDemoPhase.Idle,
    val progress: Float? = null,
    val downloadedBytes: Long = 0L,
    val totalBytes: Long = 0L,
    val speedBytesPerSecond: Long = 0L,
    val enableResume: Boolean = true,
    val enableChunkedDownload: Boolean = false,
    val overwriteExisting: Boolean = false,
    val completedPath: String? = null,
    val errorMessage: String? = null,
) {
    /** 当前是否允许编辑下载配置。 */
    val canEditConfig: Boolean
        get() = phase !in setOf(
            FileDownloaderDemoPhase.Starting,
            FileDownloaderDemoPhase.Downloading,
            FileDownloaderDemoPhase.Resumed,
            FileDownloaderDemoPhase.Paused,
        )

    /** 当前是否允许开始一个新任务。 */
    val canStart: Boolean
        get() = canEditConfig && url.isNotBlank() && savePath.isNotBlank()

    /** 当前是否允许暂停任务。 */
    val canPause: Boolean
        get() = phase == FileDownloaderDemoPhase.Downloading ||
                phase == FileDownloaderDemoPhase.Resumed

    /** 当前是否允许恢复任务。 */
    val canResume: Boolean
        get() = phase == FileDownloaderDemoPhase.Paused

    /** 当前是否允许取消任务。 */
    val canCancel: Boolean
        get() = phase in setOf(
            FileDownloaderDemoPhase.Starting,
            FileDownloaderDemoPhase.Downloading,
            FileDownloaderDemoPhase.Paused,
            FileDownloaderDemoPhase.Resumed,
        )

    /** 当前是否允许重新开始最近任务。 */
    val canRestart: Boolean
        get() = phase in setOf(
            FileDownloaderDemoPhase.Cancelled,
            FileDownloaderDemoPhase.Complete,
            FileDownloaderDemoPhase.Error,
        )
}
