package com.ciyin.app.ui.screen.platformshare

import androidx.compose.runtime.Immutable
import ciyin.platform.share.PlatformShareFailureReason
import ciyin.platform.share.PlatformShareResult

/**
 * 系统分享示例页状态。
 *
 * @property activeOperation 正在执行的分享操作。
 * @property lastOperation 最近执行的分享操作。
 * @property result 最近一次分享结果。
 * @property failureReason 最近一次技术失败原因。
 * @property failureMessage 最近一次技术失败详情。
 */
@Immutable
internal data class PlatformShareDemoUiState(
    val activeOperation: PlatformShareDemoOperation? = null,
    val lastOperation: PlatformShareDemoOperation? = null,
    val result: PlatformShareResult? = null,
    val failureReason: PlatformShareFailureReason? = null,
    val failureMessage: String? = null,
) {
    /** 当前是否正在发起系统分享。 */
    val isBusy: Boolean
        get() = activeOperation != null
}
