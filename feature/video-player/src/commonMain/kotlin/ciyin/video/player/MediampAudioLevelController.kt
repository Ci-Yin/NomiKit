package ciyin.video.player

import ciyin.video.player.ui.gesture.LevelController
import org.openani.mediamp.features.AudioLevelController
import org.openani.mediamp.features.toggleMute

/** 将 Mediamp 音量能力适配为播放器手势使用的层级控制器。 */
class MediampAudioLevelController(
    /** Mediamp 原生音量能力。 */
    private val controller: AudioLevelController,
    /** 音量或静音状态变化回调。 */
    private val onVolumeStateChanged: (level: Float, mute: Boolean) -> Unit,
) : LevelController {
    /** 当前音量。 */
    override val level: Float get() = controller.volume.value

    /** 音量变化流。 */
    val levelFlow = controller.volume

    /** 静音状态流。 */
    val muteFlow = controller.isMute

    /** 音量可调范围。 */
    override val range: ClosedRange<Float> = 0f..controller.maxVolume

    /** 更新音量并通知调用方。 */
    override fun setLevel(level: Float) {
        val newLevel = level.coerceIn(range)
        controller.setVolume(newLevel)
        onVolumeStateChanged(newLevel, controller.isMute.value)
    }

    /** 切换静音状态并通知调用方。 */
    fun toggleMute() {
        val targetIsMute = !muteFlow.value
        controller.toggleMute()
        onVolumeStateChanged(level, targetIsMute)
    }
}
