package ciyin.video.player.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ciyin.video.player.player.AvKitDownloadSpeedMediampPlayer
import org.openani.mediamp.MediampPlayer
import org.openani.mediamp.avkit.AVKitMediampPlayer
import org.openani.mediamp.compose.MediampPlayerSurface

/** 渲染 iOS AVKit 播放器画面。 */
@Composable
actual fun VideoPlayer(
    player: MediampPlayer,
    modifier: Modifier
) {
    val avKitPlayer = when (player) {
        is AvKitDownloadSpeedMediampPlayer -> player.delegate
        is AVKitMediampPlayer -> player
        else -> error("iOS VideoPlayer requires an AVKit Mediamp player")
    }
    MediampPlayerSurface(avKitPlayer, modifier)
}
