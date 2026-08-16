package ciyin.video.player.ui.pip

import androidx.compose.ui.geometry.Rect
import ciyin.platform.Context
import ciyin.platform.logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.openani.mediamp.MediampPlayer
import org.openani.mediamp.avkit.AVKitMediampPlayer
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerLayer
import platform.AVKit.AVPictureInPictureController
import platform.Foundation.NSThread

/** 创建 iOS 画中画控制器。 */
actual fun createPipController(context: Context, player: MediampPlayer): PipController =
    IosPipController(player)

/**
 * iOS 画中画控制器。
 *
 * 使用 [AVPictureInPictureController] + delegate 回调驱动状态。
 * 延迟初始化：在首次 [enterPip] 时创建 [AVPictureInPictureController]，等 AVPlayerLayer 就绪。
 */
internal class IosPipController(
    /** 用于解析 AVPlayer 的 Mediamp 播放器。 */
    private val player: MediampPlayer,
) : PipController {

    /** iOS PiP 控制器日志。 */
    private val logger = logger<IosPipController>()

    /** 内部 PiP 状态。 */
    private val _isInPipMode = MutableStateFlow(false)

    /** 系统代理回调驱动的 PiP 状态。 */
    override val isInPipMode: StateFlow<Boolean> = _isInPipMode.asStateFlow()

    /** 当前设备是否支持 AVKit PiP。 */
    override val isPipSupported: Boolean =
        AVPictureInPictureController.isPictureInPictureSupported()

    /** 接收 AVKit PiP 生命周期回调的代理。 */
    private val delegate = IosPipControllerDelegate(_isInPipMode)

    /** 延迟创建的系统 PiP 控制器。 */
    private var pipController: AVPictureInPictureController? = null

    /** 系统 PiP 使用的播放器图层。 */
    private var playerLayer: AVPlayerLayer? = null

    /** 请求进入 iOS 系统 PiP。 */
    override fun enterPip(sourceRect: Rect): Boolean {
        if (!isPipSupported) return false

        val controller = getOrCreatePipController() ?: return false

        if (!controller.pictureInPicturePossible) {
            logger.w { "PiP not possible at this time" }
            return false
        }

        if (NSThread.isMainThread) {
            controller.startPictureInPicture()
        } else {
            platform.darwin.dispatch_async(platform.darwin.dispatch_get_main_queue()) {
                controller.startPictureInPicture()
            }
        }
        return true
    }

    /** 请求退出 iOS 系统 PiP。 */
    override fun exitPip() {
        val controller = pipController ?: return
        if (NSThread.isMainThread) {
            controller.stopPictureInPicture()
        } else {
            platform.darwin.dispatch_async(platform.darwin.dispatch_get_main_queue()) {
                controller.stopPictureInPicture()
            }
        }
    }

    /** 配置是否允许从内嵌播放自动进入 PiP。 */
    override fun setAutoEnterEnabled(enabled: Boolean) {
        val controller = getOrCreatePipController() ?: return
        controller.canStartPictureInPictureAutomaticallyFromInline = enabled
    }

    /** 释放 AVKit PiP 控制器与播放器图层。 */
    override fun release() {
        pipController?.delegate = null
        pipController = null
        playerLayer = null
        _isInPipMode.value = false
    }

    // ==================== 内部方法 ====================

    /** 获取或延迟创建 AVKit PiP 控制器。 */
    private fun getOrCreatePipController(): AVPictureInPictureController? {
        pipController?.let { return it }

        val avPlayer = getAVPlayer() ?: run {
            logger.w { "Cannot obtain AVPlayer from MediampPlayer" }
            return null
        }

        val layer = AVPlayerLayer.playerLayerWithPlayer(avPlayer)
        playerLayer = layer

        val controller = AVPictureInPictureController(playerLayer = layer)
        controller.delegate = delegate
        pipController = controller
        return controller
    }

    /** 从 Mediamp 播放器解析原生 AVPlayer。 */
    private fun getAVPlayer(): AVPlayer? {
        val avkitPlayer = player as? AVKitMediampPlayer ?: run {
            logger.w { "MediampPlayer is not AVKitMediampPlayer, cannot get AVPlayer" }
            return null
        }
        return avkitPlayer.impl
    }
}
