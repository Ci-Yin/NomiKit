package com.ciyin.app.ui.screen.videoplayer

import androidx.compose.runtime.Composable
import org.openani.mediamp.MediampPlayer

/** 记住当前平台可用的视频播放器实例。 */
@Composable
internal expect fun rememberVideoPlayerDemoPlayer(): MediampPlayer?
