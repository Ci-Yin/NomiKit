package com.ciyin.app.ui.screen.filedownloader

/**
 * 文件下载示例页面动作。
 */
internal sealed interface FileDownloaderDemoAction {

    /** 返回上一页。 */
    data object BackClick : FileDownloaderDemoAction

    /** @property value 由平台缓存目录生成的默认保存路径。 */
    data class DefaultSavePathLoaded(val value: String) : FileDownloaderDemoAction

    /** @property value 用户输入的下载地址。 */
    data class UrlChange(val value: String) : FileDownloaderDemoAction

    /** @property value 用户输入的文件保存路径。 */
    data class SavePathChange(val value: String) : FileDownloaderDemoAction

    /** @property value 是否启用断点续传。 */
    data class EnableResumeChange(val value: Boolean) : FileDownloaderDemoAction

    /** @property value 是否启用分块下载。 */
    data class EnableChunkedDownloadChange(val value: Boolean) : FileDownloaderDemoAction

    /** @property value 是否覆盖已有目标文件。 */
    data class OverwriteExistingChange(val value: Boolean) : FileDownloaderDemoAction

    /** 开始下载。 */
    data object StartClick : FileDownloaderDemoAction

    /** 暂停下载。 */
    data object PauseClick : FileDownloaderDemoAction

    /** 恢复下载。 */
    data object ResumeClick : FileDownloaderDemoAction

    /** 取消下载。 */
    data object CancelClick : FileDownloaderDemoAction

    /** 重新开始下载。 */
    data object RestartClick : FileDownloaderDemoAction
}
