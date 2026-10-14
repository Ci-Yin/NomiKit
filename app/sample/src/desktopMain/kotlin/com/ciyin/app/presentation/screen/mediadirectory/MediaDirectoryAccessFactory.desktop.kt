package com.ciyin.app.presentation.screen.mediadirectory

import androidx.compose.runtime.Composable

/**
 * Desktop 端 [rememberMediaDirectoryAccessFactory] 的实际实现。
 *
 * Desktop 平台下用户媒体目录由 [ciyin.media.directory.MediaDirectorySession] 的内置
 * `JFileChooser` 直接支持，无需 Composable 侧预先准备 Access。返回 `null` 让
 * [MediaDirectoryDemoViewModel] 走 `session.requestAccess()` 主路径。
 */
@Composable
internal actual fun rememberMediaDirectoryAccessFactory(): MediaDirectoryAccessFactory? = null
