package ciyin.video.player.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** 播放器控制器可见性请求测试。 */
class PlayerControllerStateTest {

    /** 进度请求临时显示独立进度条。 */
    @Test
    fun progressRequestControlsDetachedSlider() {
        val state = PlayerControllerState()
        val requester = Any()

        state.setRequestProgressBar(requester)
        assertEquals(ControllerVisibility.DetachedSliderOnly, state.visibility)

        state.cancelRequestProgressBarVisible(requester)
        assertEquals(ControllerVisibility.Invisible, state.visibility)
    }

    /** 常驻请求按对象去重并可取消。 */
    @Test
    fun alwaysOnRequestIsDeduplicated() {
        val state = PlayerControllerState()
        val requester = Any()

        state.setRequestAlwaysOn(requester, true)
        state.setRequestAlwaysOn(requester, true)
        assertTrue(state.alwaysOn)
        assertEquals(1, state.getAlwaysOnRequesters().size)

        state.setRequestAlwaysOn(requester, false)
        assertFalse(state.alwaysOn)
    }
}
