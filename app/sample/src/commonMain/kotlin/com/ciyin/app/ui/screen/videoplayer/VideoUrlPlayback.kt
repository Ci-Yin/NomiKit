package com.ciyin.app.ui.screen.videoplayer

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.CancellationException

/** URL 播放提交结果。 */
@Immutable
internal sealed interface VideoUrlPlayResult {

    /** 已向播放器提交规范化后的地址。 */
    data class Started(val url: String) : VideoUrlPlayResult

    /** 输入去除首尾空白后为空。 */
    data object Empty : VideoUrlPlayResult

    /** 播放器拒绝地址或加载准备失败。 */
    data class Failed(val message: String) : VideoUrlPlayResult
}

/**
 * 去除 URL 首尾空白后提交给播放器，不限制协议或查询参数。
 *
 * 普通异常转换成失败结果，协程取消异常继续传播。
 */
internal suspend fun submitVideoUrl(
    input: String,
    play: suspend (String) -> Unit,
): VideoUrlPlayResult {
    val url = input.trim()
    if (url.isEmpty()) return VideoUrlPlayResult.Empty

    return try {
        play(url)
        VideoUrlPlayResult.Started(url)
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (error: Exception) {
        VideoUrlPlayResult.Failed(
            message = error.message ?: error::class.simpleName ?: error.toString(),
        )
    }
}
