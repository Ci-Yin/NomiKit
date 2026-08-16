package ciyin.video.player.ui.pip

import ciyin.platform.Context
import org.openani.mediamp.MediampPlayer

/**
 * 创建平台对应的 [PipController] 实例。
 *
 * @param context 平台上下文
 * @param player 播放器实例（部分平台需要底层播放器引用）
 */
expect fun createPipController(context: Context, player: MediampPlayer): PipController
