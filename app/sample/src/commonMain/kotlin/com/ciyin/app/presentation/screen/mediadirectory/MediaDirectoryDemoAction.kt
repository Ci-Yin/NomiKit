package com.ciyin.app.presentation.screen.mediadirectory

import ciyin.media.directory.MediaDirectoryAccessToken

/** 用户媒体目录示例的页面 Action 集合。 */
internal sealed interface MediaDirectoryDemoAction {
    /** 用户点击返回按钮。 */
    data object BackClick : MediaDirectoryDemoAction

    /** 用户点击"选择目录"按钮。 */
    data object PickDirectoryClick : MediaDirectoryDemoAction

    /** 用户点击"验证目录"按钮。 */
    data object ValidateClick : MediaDirectoryDemoAction

    /** 用户点击"提交示例文件"按钮。 */
    data object CommitSampleClick : MediaDirectoryDemoAction

    /** 用户点击"检查最近条目"按钮。 */
    data object ExistsClick : MediaDirectoryDemoAction

    /** 用户点击"删除最近条目"按钮。 */
    data object DeleteClick : MediaDirectoryDemoAction

    /** 选择目录完成，携带平台返回值。 */
    data class DirectoryPicked(
        val token: MediaDirectoryAccessToken,
        val displayName: String,
        val canonicalPath: String?,
    ) : MediaDirectoryDemoAction

    /** 选择目录被用户取消。 */
    data object DirectoryCancelled : MediaDirectoryDemoAction

    /** validate 成功。 */
    data object Validated : MediaDirectoryDemoAction

    /** validate 发现授权失效。 */
    data object ValidationRevoked : MediaDirectoryDemoAction

    /** commit 成功，携带条目。 */
    data class Committed(
        val fileName: String,
        val location: String,
        val mimeType: String,
        val size: Long,
        val exists: Boolean,
    ) : MediaDirectoryDemoAction

    /** exists 检查完成。 */
    data class ExistsLoaded(val exists: Boolean) : MediaDirectoryDemoAction

    /** 删除完成。 */
    data object Deleted : MediaDirectoryDemoAction

    /** 结构化错误。 */
    data class OperationFailed(
        val type: MediaDirectoryDemoErrorType,
    ) : MediaDirectoryDemoAction

    /** 清空最近错误。 */
    data object ClearError : MediaDirectoryDemoAction
}
