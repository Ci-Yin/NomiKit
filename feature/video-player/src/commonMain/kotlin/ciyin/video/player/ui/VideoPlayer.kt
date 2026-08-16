package ciyin.video.player.ui


import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.openani.mediamp.MediampPlayer

/** 渲染不含控制栏的原生视频画面，尺寸由 [modifier] 决定。 */
@Composable
expect fun VideoPlayer(
    player: MediampPlayer,
    modifier: Modifier,
)
