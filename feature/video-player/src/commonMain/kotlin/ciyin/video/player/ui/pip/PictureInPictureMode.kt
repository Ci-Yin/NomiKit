package ciyin.video.player.ui.pip

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * 画中画（PiP）模式状态。
 *
 * 说明：该状态用于在 UI 层感知系统 PiP 模式变化（进入/退出），从而实现：
 * - 进入 PiP 时清屏、关闭弹幕显示；
 * - 退出 PiP 时恢复用户原有的弹幕开关状态。
 *
 */
object PictureInPictureMode {
    /** 内部 PiP 状态。 */
    private val _isInPictureInPictureMode = MutableStateFlow(false)

    /** 对外只读的 PiP 状态。 */
    val isInPictureInPictureMode: StateFlow<Boolean> = _isInPictureInPictureMode

    /** 更新系统 PiP 状态。 */
    fun update(isInPictureInPictureMode: Boolean) {
        _isInPictureInPictureMode.value = isInPictureInPictureMode
    }
}
