package com.ciyin.app.ui.screen.filedownloader

/**
 * 文件下载示例页面的一次性副作用。
 */
internal sealed interface FileDownloaderDemoEffect {

    /** 请求宿主返回上一页。 */
    data object NavigateBack : FileDownloaderDemoEffect
}
