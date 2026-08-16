package ciyin.video.player.ui.pip

import ciyin.platform.logger
import kotlinx.coroutines.flow.MutableStateFlow
import platform.AVKit.AVPictureInPictureController
import platform.AVKit.AVPictureInPictureControllerDelegateProtocol
import platform.Foundation.NSError
import platform.darwin.NSObject

/**
 * iOS 画中画代理实现。
 *
 * 实现 [AVPictureInPictureControllerDelegateProtocol]，将系统 PiP 事件桥接为 [StateFlow]。
 */
internal class IosPipControllerDelegate(
    /** 接收系统回调的 PiP 状态。 */
    private val isInPipMode: MutableStateFlow<Boolean>,
) : NSObject(), AVPictureInPictureControllerDelegateProtocol {

    /** iOS PiP 代理日志。 */
    private val logger = logger<IosPipControllerDelegate>()

    /** 接收即将进入 PiP 的系统回调。 */
    override fun pictureInPictureControllerWillStartPictureInPicture(
        pictureInPictureController: AVPictureInPictureController,
    ) {
        // PiP 即将开始
    }

    /** 接收已经进入 PiP 的系统回调。 */
    override fun pictureInPictureControllerDidStartPictureInPicture(
        pictureInPictureController: AVPictureInPictureController,
    ) {
        isInPipMode.value = true
    }

    /** 接收即将退出 PiP 的系统回调。 */
    override fun pictureInPictureControllerWillStopPictureInPicture(
        pictureInPictureController: AVPictureInPictureController,
    ) {
        // PiP 即将停止
    }

    /** 接收已经退出 PiP 的系统回调。 */
    override fun pictureInPictureControllerDidStopPictureInPicture(
        pictureInPictureController: AVPictureInPictureController,
    ) {
        isInPipMode.value = false
    }

    /** 接收 PiP 启动失败的系统回调。 */
    override fun pictureInPictureController(
        pictureInPictureController: AVPictureInPictureController,
        failedToStartPictureInPictureWithError: NSError,
    ) {
        logger.e { "PiP failed to start: ${failedToStartPictureInPictureWithError.localizedDescription}" }
        isInPipMode.value = false
    }
}
