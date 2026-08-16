package com.ciyin.app.ui.screen.filedownloader

import ciyin.file.downloader.core.DownloadState
import ciyin.file.downloader.model.DownloadConfig

/**
 * 将页面输入映射为下载器配置。
 *
 * @return 可直接交给 `FileDownloader.download` 的配置。
 */
internal fun FileDownloaderDemoUiState.toDownloadConfig(): DownloadConfig = DownloadConfig(
    url = url.trim(),
    savePath = savePath.trim(),
    enableResume = enableResume,
    enableChunkedDownload = enableChunkedDownload,
    overwriteExisting = overwriteExisting,
)

/**
 * 将下载器状态合并进页面状态。
 *
 * @param downloadState 下载器最新状态。
 * @return 合并后的不可变页面状态。
 */
internal fun FileDownloaderDemoUiState.withDownloadState(
    downloadState: DownloadState,
): FileDownloaderDemoUiState = when (downloadState) {
    DownloadState.Idle -> copy(
        phase = FileDownloaderDemoPhase.Idle,
        progress = null,
        downloadedBytes = 0L,
        totalBytes = 0L,
        speedBytesPerSecond = 0L,
        completedPath = null,
        errorMessage = null,
    )

    DownloadState.Start -> copy(
        phase = FileDownloaderDemoPhase.Starting,
        progress = null,
        downloadedBytes = 0L,
        totalBytes = 0L,
        speedBytesPerSecond = 0L,
        completedPath = null,
        errorMessage = null,
    )

    is DownloadState.Downloading -> copy(
        phase = FileDownloaderDemoPhase.Downloading,
        progress = downloadState.progress,
        downloadedBytes = downloadState.downloaded,
        totalBytes = downloadState.total,
        speedBytesPerSecond = downloadState.speed,
        completedPath = null,
        errorMessage = null,
    )

    DownloadState.Paused -> copy(phase = FileDownloaderDemoPhase.Paused)
    DownloadState.Resumed -> copy(phase = FileDownloaderDemoPhase.Resumed)
    DownloadState.Cancelled -> copy(
        phase = FileDownloaderDemoPhase.Cancelled,
        speedBytesPerSecond = 0L,
    )

    is DownloadState.Complete -> copy(
        phase = FileDownloaderDemoPhase.Complete,
        progress = 1f,
        speedBytesPerSecond = 0L,
        completedPath = downloadState.filePath.toString(),
        errorMessage = null,
    )

    is DownloadState.Error -> copy(
        phase = FileDownloaderDemoPhase.Error,
        speedBytesPerSecond = 0L,
        completedPath = null,
        errorMessage = downloadState.message ?: downloadState.exception.message ?: "下载失败",
    )
}

/**
 * 将字节数格式化为紧凑的二进制单位文本。
 *
 * @return B、KiB、MiB 或 GiB 文本。
 */
internal fun Long.formatDownloadBytes(): String {
    val value = coerceAtLeast(0L)
    val units = listOf("B", "KiB", "MiB", "GiB")
    var unitIndex = 0
    var divisor = 1L
    while (unitIndex < units.lastIndex && value / divisor >= 1024L) {
        divisor *= 1024L
        unitIndex += 1
    }
    if (unitIndex == 0) return "$value ${units[unitIndex]}"
    val whole = value / divisor
    val fraction = (value % divisor) * 10L / divisor
    return "$whole.$fraction ${units[unitIndex]}"
}
