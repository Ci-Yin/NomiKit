package com.ciyin.app.ui.screen.videoplayer

import androidx.compose.runtime.Immutable
import org.openani.mediamp.PlaybackState

/** 视频播放器示例页的用户动作与播放器状态回灌事件。 */
@Immutable
internal sealed interface VideoPlayerDemoAction {

    /** 修改 URL 草稿。 */
    data class UrlChange(val value: String) : VideoPlayerDemoAction

    /** 提交当前 URL。 */
    data object PlayClick : VideoPlayerDemoAction

    /** 请求返回上一页。 */
    data object BackClick : VideoPlayerDemoAction

    /** 回灌播放器的真实播放状态。 */
    data class PlaybackChanged(val value: PlaybackState) : VideoPlayerDemoAction

    /** 回灌播放器是否已经持有媒体。 */
    data class MediaAvailabilityChanged(val hasMedia: Boolean) : VideoPlayerDemoAction

    /** 回灌播放器加载失败原因。 */
    data class PlaybackFailed(val message: String) : VideoPlayerDemoAction
}
