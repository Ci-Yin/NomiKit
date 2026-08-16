package com.ciyin.app.ui.screen.videoplayer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import ciyin.platform.LocalContext
import ciyin.video.player.player.ExoPlayerMediampPlayer
import org.openani.mediamp.MediampPlayer

/** 记住并在组合销毁时关闭 Android ExoPlayer 实例。 */
@Composable
internal actual fun rememberVideoPlayerDemoPlayer(): MediampPlayer? {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val player = remember(context, scope) {
        ExoPlayerMediampPlayer(
            context = context,
            parentCoroutineContext = scope.coroutineContext,
        )
    }
    DisposableEffect(player) {
        onDispose(player::close)
    }
    return player
}
