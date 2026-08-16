package com.ciyin.app.ui.screen.videoplayer

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.openani.mediamp.PlaybackState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/** 视频播放器示例 ViewModel 状态流转测试。 */
class VideoPlayerDemoViewModelTest {

    /** 播放动作规范化 URL 并发出加载副作用。 */
    @Test
    fun playEmitsTrimmedLoadEffect() = runTest {
        val viewModel = VideoPlayerDemoViewModel()
        val effect = async(start = CoroutineStart.UNDISPATCHED) {
            viewModel.sideEffects.first()
        }

        viewModel.dispatchAction(VideoPlayerDemoAction.UrlChange("  https://example.com/video.m3u8?a=1  "))
        viewModel.state.first { it.url.startsWith("  ") }
        viewModel.dispatchAction(VideoPlayerDemoAction.PlayClick)

        assertEquals(
            VideoPlayerDemoEffect.LoadUrl("https://example.com/video.m3u8?a=1"),
            effect.await(),
        )
        assertEquals(
            "https://example.com/video.m3u8?a=1",
            viewModel.state.first { it.url == "https://example.com/video.m3u8?a=1" }.url,
        )
    }

    /** 空输入保留在页面并产生校验错误。 */
    @Test
    fun blankUrlUpdatesValidationError() = runTest {
        val viewModel = VideoPlayerDemoViewModel(emptyUrlMessage = { "请输入视频地址" })

        viewModel.dispatchAction(VideoPlayerDemoAction.UrlChange(" \n "))
        viewModel.state.first { it.url == " \n " }
        viewModel.dispatchAction(VideoPlayerDemoAction.PlayClick)

        assertNotNull(viewModel.state.first { it.errorMessage != null }.errorMessage)
    }

    /** 返回动作发出导航副作用。 */
    @Test
    fun backEmitsNavigationEffect() = runTest {
        val viewModel = VideoPlayerDemoViewModel()
        viewModel.state.first()
        val effect = async(start = CoroutineStart.UNDISPATCHED) {
            viewModel.sideEffects.first()
        }

        viewModel.dispatchAction(VideoPlayerDemoAction.BackClick)

        assertEquals(VideoPlayerDemoEffect.NavigateBack, effect.await())
    }

    /** 重新提交 URL 时清除上一条媒体的播放状态。 */
    @Test
    fun playClearsStalePlaybackState() = runTest {
        val viewModel = VideoPlayerDemoViewModel()
        viewModel.state.first()
        viewModel.dispatchAction(VideoPlayerDemoAction.PlaybackChanged(PlaybackState.PLAYING))
        viewModel.dispatchAction(VideoPlayerDemoAction.MediaAvailabilityChanged(true))
        viewModel.state.first { it.hasMedia }
        val effect = async(start = CoroutineStart.UNDISPATCHED) {
            viewModel.sideEffects.first()
        }

        viewModel.dispatchAction(VideoPlayerDemoAction.PlayClick)
        assertEquals(VideoPlayerDemoEffect.LoadUrl(DefaultVideoUrl), effect.await())
        val state = viewModel.state.first { it.playbackState == null }

        assertNull(state.playbackState)
        assertFalse(state.hasMedia)
    }
}
