package ciyin.video.player.ui.pip

import androidx.compose.ui.geometry.Rect
import kotlinx.coroutines.flow.StateFlow

/**
 * 画中画控制器接口。
 *
 * 由 ViewModel 持有并管理生命周期，负责与平台 PiP API 交互。
 */
interface PipController {

    /** 系统回调驱动的 PiP 模式状态。 */
    val isInPipMode: StateFlow<Boolean>

    /** 当前平台/设备是否支持画中画。 */
    val isPipSupported: Boolean

    /**
     * 请求进入画中画模式。
     *
     * @param sourceRect 动画源矩形（用于过渡动画），默认 [Rect.Zero]
     * @return 是否成功发起请求
     */
    fun enterPip(sourceRect: Rect = Rect.Zero): Boolean

    /** 请求退出画中画模式。 */
    fun exitPip()

    /**
     * 设置是否在用户离开应用时自动进入画中画。
     *
     * @param enabled 是否启用
     */
    fun setAutoEnterEnabled(enabled: Boolean)

    /** 释放资源（取消注册监听、清理引用）。 */
    fun release()
}
