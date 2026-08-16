package com.ciyin.app.ui.screen.videoplayer

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

/** URL 播放提交契约测试。 */
class VideoUrlPlaybackTest {

    /** 提交时去除首尾空白并保留查询参数。 */
    @Test
    fun trimsUrlWithoutChangingParameters() = runTest {
        var submittedUrl: String? = null

        val result = submitVideoUrl(
            input = "  https://media.example.com/video.mp4?token=a%20b&part=1  ",
            play = { submittedUrl = it },
        )

        val expected = "https://media.example.com/video.mp4?token=a%20b&part=1"
        assertEquals(VideoUrlPlayResult.Started(expected), result)
        assertEquals(expected, submittedUrl)
    }

    /** 空白输入不会调用播放器。 */
    @Test
    fun rejectsBlankUrlBeforePlayback() = runTest {
        var wasCalled = false

        val result = submitVideoUrl("  \n\t ") { wasCalled = true }

        assertEquals(VideoUrlPlayResult.Empty, result)
        assertFalse(wasCalled)
    }

    /** 普通播放器异常会映射为明确失败。 */
    @Test
    fun mapsPlaybackExceptionToFailure() = runTest {
        val result = submitVideoUrl("https://media.example.com/unsupported") {
            throw IllegalStateException("不支持的媒体格式")
        }

        assertEquals(VideoUrlPlayResult.Failed("不支持的媒体格式"), result)
    }

    /** 没有消息的异常回退到异常类型。 */
    @Test
    fun fallsBackToExceptionType() = runTest {
        val result = submitVideoUrl("https://media.example.com/unsupported") {
            throw IllegalStateException()
        }

        assertEquals(VideoUrlPlayResult.Failed("IllegalStateException"), result)
    }

    /** 协程取消异常保持传播。 */
    @Test
    fun propagatesCancellation() = runTest {
        assertFailsWith<CancellationException> {
            submitVideoUrl("https://media.example.com/video.mp4") {
                throw CancellationException("页面退出")
            }
        }
    }
}
