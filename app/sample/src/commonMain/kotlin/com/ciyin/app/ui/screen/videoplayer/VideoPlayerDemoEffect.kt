package com.ciyin.app.ui.screen.videoplayer

import androidx.compose.runtime.Immutable

/** 视频播放器示例页的一次性副作用。 */
@Immutable
internal sealed interface VideoPlayerDemoEffect {

    /** 请求 Screen 向平台播放器加载 URL。 */
    data class LoadUrl(val url: String) : VideoPlayerDemoEffect

    /** 请求宿主返回上一页。 */
    data object NavigateBack : VideoPlayerDemoEffect
}
