package com.ciyin.app.ui.screen.videoplayer

import com.ciyin.app.sample.Res
import com.ciyin.app.sample.video_player_demo_empty_url
import com.freeletics.flowredux2.FlowReduxBuilder
import com.freeletics.flowredux2.FlowReduxStateMachineFactory
import com.freeletics.flowredux2.initializeWith
import ciyin.ui.foundation.viewmodel.StateMachineMviViewModel
import org.jetbrains.compose.resources.getString

/**
 * 使用 FlowRedux2 管理 URL 草稿、播放反馈与导航副作用。
 *
 * @param emptyUrlMessage 提供空 URL 的资源化错误文案。
 */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
internal class VideoPlayerDemoViewModel(
    /** 提供空 URL 的资源化错误文案。 */
    private val emptyUrlMessage: suspend () -> String = {
        getString(Res.string.video_player_demo_empty_url)
    },
) :
    StateMachineMviViewModel<VideoPlayerDemoUiState, VideoPlayerDemoAction, VideoPlayerDemoEffect>() {

    /** 初始化视频播放器示例状态。 */
    override fun FlowReduxStateMachineFactory<VideoPlayerDemoUiState, VideoPlayerDemoAction>.initialize() {
        initializeWith { VideoPlayerDemoUiState() }
    }

    /** 声明示例页的状态流转。 */
    override fun FlowReduxBuilder<VideoPlayerDemoUiState, VideoPlayerDemoAction>.spec() {
        inState<VideoPlayerDemoUiState> {

            // URL 编辑只更新草稿并清除旧错误。
            on<VideoPlayerDemoAction.UrlChange> { action ->
                mutate { copy(url = action.value, errorMessage = null) }
            }

            // 提交时仅拒绝空输入，并将规范化 URL 交给 Screen 加载。
            on<VideoPlayerDemoAction.PlayClick> {
                val url = snapshot.url.trim()
                if (url.isEmpty()) {
                    val message = emptyUrlMessage()
                    mutate { copy(errorMessage = message) }
                } else {
                    poseEffect(VideoPlayerDemoEffect.LoadUrl(url))
                    mutate {
                        copy(
                            url = url,
                            playbackState = null,
                            hasMedia = false,
                            errorMessage = null,
                        )
                    }
                }
            }

            // 返回由页面宿主执行。
            onActionEffect<VideoPlayerDemoAction.BackClick> {
                poseEffect(VideoPlayerDemoEffect.NavigateBack)
            }

            // 同步播放器真实状态。
            on<VideoPlayerDemoAction.PlaybackChanged> { action ->
                mutate { copy(playbackState = action.value) }
            }

            // 同步播放器媒体存在性。
            on<VideoPlayerDemoAction.MediaAvailabilityChanged> { action ->
                mutate { copy(hasMedia = action.hasMedia) }
            }

            // 显示平台播放器返回的明确错误。
            on<VideoPlayerDemoAction.PlaybackFailed> { action ->
                mutate { copy(errorMessage = action.message) }
            }
        }
    }
}
