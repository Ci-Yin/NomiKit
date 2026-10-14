package com.ciyin.app.presentation.screen.mediadirectory

import androidx.compose.runtime.Composable
import ciyin.media.directory.MediaDirectoryAccess

/**
 * 平台无关的"启动目录选择器并返回 Access"工厂。
 *
 * 返回 `null` 表示取消；返回 [MediaDirectoryAccess] 时，调用方应已对授权
 * 令牌完成了相应平台所需的持久化授权（SAF `takePersistableUriPermission` 等）。
 */
internal typealias MediaDirectoryAccessFactory = suspend () -> MediaDirectoryAccess?

/**
 * 在 Compose 层构造当前平台的 [MediaDirectoryAccessFactory]。
 *
 * - Android：通过 `rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree())`
 *   启动 SAF 目录选择器，对结果 `Uri` 调用 `takePersistableUriPermission` 并解析 displayName，
 *   然后返回带授权令牌的 [MediaDirectoryAccess]。
 * - Desktop：返回 `null`（对话内置的 JFileChooser 已由
 *   [ciyin.media.directory.MediaDirectorySession.requestAccess] 在 Koin 默认会话中支持）。
 * - iOS：返回 `null`（等待宿主接入真实 `UIDocumentPickerViewController`）。
 *
 * 工厂是 `suspend` 的，可以在任何 dispatcher 调用，包括 `viewModelScope`。
 *
 * @return 平台相关的可挂起 Access 工厂；调用方负责在 ViewModel 初始化前获取并传入。
 */
@Composable
internal expect fun rememberMediaDirectoryAccessFactory(): MediaDirectoryAccessFactory?
