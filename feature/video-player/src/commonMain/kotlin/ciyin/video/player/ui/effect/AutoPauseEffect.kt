package ciyin.video.player.ui.effect


import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.lifecycle.Lifecycle
import ciyin.video.player.ui.internal.rememberLatestTask
import ciyin.ui.foundation.effects.OnLifecycleEvent
import org.openani.mediamp.MediampPlayer

/**
 * 切后台自动暂停
 */
@Composable
fun AutoPauseEffect(player: MediampPlayer) {
    var pausedVideo by rememberSaveable { mutableStateOf(true) } // live after configuration change
    if (LocalInspectionMode.current) return

    val autoPauseTasker = rememberLatestTask()
    OnLifecycleEvent {
        if (it == Lifecycle.Event.ON_STOP) {
            if (player.playbackState.value.isPlaying) {
                pausedVideo = true
                autoPauseTasker.launch {
                    // #160, 切换全屏时视频会暂停半秒
                    // > 这其实是之前写切后台自动暂停导致的，检测了 lifecycle 事件，切全屏和切后台是一样的事件。延迟一下就可以了
                    player.pause() // 正在播放时, 切到后台自动暂停
                }
            } else {
                // 如果不是正在播放, 则不操作暂停, 当下次切回前台时, 也不要恢复播放
                pausedVideo = false
            }
        } else if (it == Lifecycle.Event.ON_START && pausedVideo) {
            autoPauseTasker.launch {
                player.resume() // 切回前台自动恢复, 当且仅当之前是自动暂停的
            }
            pausedVideo = false
        }
    }
}
