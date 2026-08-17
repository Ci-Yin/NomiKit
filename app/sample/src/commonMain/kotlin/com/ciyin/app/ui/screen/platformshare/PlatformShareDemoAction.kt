package com.ciyin.app.ui.screen.platformshare

import ciyin.platform.Context

/**
 * 系统分享示例页动作。
 */
internal sealed interface PlatformShareDemoAction {
    /**
     * 点击返回按钮。
     */
    data object BackClick : PlatformShareDemoAction

    /**
     * 分享文本。
     *
     * @property context 平台上下文。
     * @property title 分享内容标题。
     * @property content 待分享文本。
     */
    data class ShareText(
        val context: Context,
        val title: String,
        val content: String,
    ) : PlatformShareDemoAction

    /**
     * 分享单个文件。
     *
     * @property context 平台上下文。
     * @property title 分享内容标题。
     * @property content 示例文件内容。
     */
    data class ShareSingleFile(
        val context: Context,
        val title: String,
        val content: String,
    ) : PlatformShareDemoAction

    /**
     * 分享多个文件。
     *
     * @property context 平台上下文。
     * @property title 分享内容标题。
     * @property firstContent 第一个示例文件内容。
     * @property secondContent 第二个示例文件内容。
     */
    data class ShareMultipleFiles(
        val context: Context,
        val title: String,
        val firstContent: String,
        val secondContent: String,
    ) : PlatformShareDemoAction

    /**
     * 分享调用完成。
     *
     * @property operation 已执行的分享操作。
     * @property result 平台分享结果。
     */
    data class ShareCompleted(
        val operation: PlatformShareDemoOperation,
        val result: ciyin.platform.share.PlatformShareResult,
    ) : PlatformShareDemoAction

    /**
     * 分享调用失败。
     *
     * @property operation 已执行的分享操作。
     * @property reason 技术失败原因。
     * @property message 技术失败详情。
     */
    data class ShareFailed(
        val operation: PlatformShareDemoOperation,
        val reason: ciyin.platform.share.PlatformShareFailureReason,
        val message: String,
    ) : PlatformShareDemoAction
}
