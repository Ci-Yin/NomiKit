package ciyin.video.player.ui

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.compose.ContentFrame
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import ciyin.video.player.player.ExoPlayerMediampPlayer
import org.openani.mediamp.MediampPlayer
import org.openani.mediamp.features.AspectRatioMode
import org.openani.mediamp.features.VideoAspectRatio

/** 在 Android 上渲染 ExoPlayer 视频画面。 */
@OptIn(UnstableApi::class)
@Composable
actual fun VideoPlayer(
    player: MediampPlayer,
    modifier: Modifier
) {
    val isPreviewing by rememberUpdatedState(LocalInspectionMode.current)

    if (isPreviewing) {
        Box(modifier)
    } else {
        ExoPlayerMediampPlayerSurface(player as ExoPlayerMediampPlayer, modifier)
    }
}

/** 将 ExoPlayer 的 Surface 接入 Compose 布局。 */
@OptIn(UnstableApi::class)
@Composable
private fun ExoPlayerMediampPlayerSurface(
    mediampPlayer: ExoPlayerMediampPlayer,
    modifier: Modifier = Modifier
) {
    val aspectRatioMode by mediampPlayer.features[VideoAspectRatio.Key]?.mode?.collectAsState()
        ?: return // Return early if VideoAspectRatio feature is not available
    ContentFrame(
        modifier = modifier,
        player = mediampPlayer.impl,
        surfaceType = SURFACE_TYPE_TEXTURE_VIEW,
        contentScale = when (aspectRatioMode) {
            AspectRatioMode.FIT -> ContentScale.Fit
            AspectRatioMode.STRETCH -> ContentScale.FillBounds
            AspectRatioMode.CROP -> ContentScale.Crop
        },
        keepContentOnReset = true,
    )
}
