package com.ciyin.app.ui.screen.videoplayer

import androidx.compose.runtime.Immutable
import org.openani.mediamp.PlaybackState

/** 示例默认使用的 Shaka Demo HLS 地址。 */
internal const val DefaultVideoUrl =
    "https://storage.googleapis.com/shaka-demo-assets/angel-one-hls/hls.m3u8"

/** 视频播放器示例页的不可变 UI 状态。 */
@Immutable
internal data class VideoPlayerDemoUiState(
    /** 可编辑的视频地址。 */
    val url: String = DefaultVideoUrl,
    /** 平台播放器的当前播放状态。 */
    val playbackState: PlaybackState? = null,
    /** 播放器是否已经持有媒体。 */
    val hasMedia: Boolean = false,
    /** 当前明确展示给用户的错误。 */
    val errorMessage: String? = null,
)
