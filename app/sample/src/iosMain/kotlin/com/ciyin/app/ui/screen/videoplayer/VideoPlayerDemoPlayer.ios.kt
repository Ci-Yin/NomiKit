package com.ciyin.app.ui.screen.videoplayer

import androidx.compose.runtime.Composable
import org.openani.mediamp.MediampPlayer
import org.openani.mediamp.compose.rememberMediampPlayer

/** 记住 iOS 默认 Mediamp AVKit 播放器实例。 */
@Composable
internal actual fun rememberVideoPlayerDemoPlayer(): MediampPlayer? = rememberMediampPlayer()
