package com.ciyin.app.presentation.screen.mediadirectory

import androidx.compose.runtime.Composable

/**
 * iOS 端 [rememberMediaDirectoryAccessFactory] 的实际实现。
 *
 * 当前 iOS 用户媒体目录为占位实现，参见
 * [ciyin.media.directory.IosMediaDirectorySession]。返回 `null` 让
 * [MediaDirectoryDemoViewModel] 走现有 `requestAccess()` 路径，由会话自己抛 Unsupported。
 */
@Composable
internal actual fun rememberMediaDirectoryAccessFactory(): MediaDirectoryAccessFactory? = null
