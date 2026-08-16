package com.ciyin.app.ui.screen.filedownloader

/** 示例默认下载地址，指向 NomiKit master 分支 README。 */
internal const val DefaultDownloadUrl =
    "https://raw.githubusercontent.com/Ci-Yin/NomiKit/master/README.md"

/** 示例下载文件名。 */
internal const val DefaultDownloadFileName = "nomikit-readme.md"

/**
 * 文件下载示例的展示阶段。
 *
 * @property displayName 面向用户展示的阶段名称。
 */
internal enum class FileDownloaderDemoPhase(
    val displayName: String,
) {
    /** 尚未开始下载。 */
    Idle("空闲"),

    /** 正在初始化下载。 */
    Starting("正在开始"),

    /** 正在接收并写入数据。 */
    Downloading("下载中"),

    /** 下载已暂停。 */
    Paused("已暂停"),

    /** 下载已恢复。 */
    Resumed("正在恢复"),

    /** 下载已取消。 */
    Cancelled("已取消"),

    /** 下载已完成。 */
    Complete("已完成"),

    /** 下载发生错误。 */
    Error("失败"),
}
