package ciyin.video.player.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ciyin.video.player.player.VlcDownloadSpeedMediampPlayer
import org.openani.mediamp.MediampPlayer
import org.openani.mediamp.vlc.VlcMediampPlayer
import org.openani.mediamp.vlc.compose.VlcMediampPlayerSurface

/** 渲染 Desktop VLC 播放器画面。 */
@Composable
actual fun VideoPlayer(
    player: MediampPlayer,
    modifier: Modifier,
) {
    val vlcPlayer = when (player) {
        is VlcDownloadSpeedMediampPlayer -> player.delegate
        is VlcMediampPlayer -> player
        else -> error("Desktop VideoPlayer requires a VLC Mediamp player")
    }
    VlcMediampPlayerSurface(vlcPlayer, modifier = modifier)
}
