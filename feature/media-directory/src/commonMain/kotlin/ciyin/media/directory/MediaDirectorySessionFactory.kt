package ciyin.media.directory

import ciyin.platform.Context

/**
 * 创建平台 [MediaDirectorySession] 实现。
 *
 * - Android：[pickerLauncher] 可选；提供时由宿主（通常是 Activity）启动 `ACTION_OPEN_DOCUMENT_TREE`，
 *   未提供时仅 [requestAccess] 不可用，已取得 Tree URI 后的读写操作仍可执行。
 * - Desktop：可传 `null` 使用内置 `JFileChooser`；传入时优先使用宿主注入。
 * - iOS：由 actual 提供内部默认注入；调用方可不传。
 */
expect fun createMediaDirectorySession(
    context: Context,
    pickerLauncher: DirectoryPickerLauncher?,
): MediaDirectorySession
