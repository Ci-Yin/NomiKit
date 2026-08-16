package com.ciyin.app.ui.screen.videoplayer

import androidx.compose.runtime.Composable
import ciyin.video.player.VlcPlayerInitializationResult
import ciyin.video.player.rememberVlcMediampPlayer
import org.openani.mediamp.MediampPlayer

/** 记住 Desktop VLC 播放器；原生运行库不可用时返回空值。 */
@Composable
internal actual fun rememberVideoPlayerDemoPlayer(): MediampPlayer? =
    when (val result = rememberVlcMediampPlayer()) {
        is VlcPlayerInitializationResult.Ready -> result.player
        is VlcPlayerInitializationResult.Unavailable -> null
    }
