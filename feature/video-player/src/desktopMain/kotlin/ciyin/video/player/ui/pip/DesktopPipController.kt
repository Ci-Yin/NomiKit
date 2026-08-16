package ciyin.video.player.ui.pip

import androidx.compose.ui.geometry.Rect
import ciyin.platform.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.openani.mediamp.MediampPlayer

/** 创建 Desktop 端暂不支持 PiP 的控制器。 */
actual fun createPipController(context: Context, player: MediampPlayer): PipController =
    NoOpPipController

/**
 * 桌面端画中画控制器（无操作实现）。
 *
 * 桌面平台不支持画中画，所有操作为空。
 */
object NoOpPipController : PipController {
    /** Desktop 永远不会进入系统 PiP。 */
    override val isInPipMode: StateFlow<Boolean> = MutableStateFlow(false)

    /** Desktop 当前不支持系统 PiP。 */
    override val isPipSupported: Boolean = false

    /** Desktop 进入 PiP 请求固定失败。 */
    override fun enterPip(sourceRect: Rect): Boolean = false

    /** Desktop 无需退出 PiP。 */
    override fun exitPip() {}

    /** Desktop 忽略自动进入配置。 */
    override fun setAutoEnterEnabled(enabled: Boolean) {}

    /** Desktop 无需释放 PiP 资源。 */
    override fun release() {}
}
