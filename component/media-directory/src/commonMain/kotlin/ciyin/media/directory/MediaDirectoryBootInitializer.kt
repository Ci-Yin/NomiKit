package ciyin

import ciyin.koin.KoinBootInitializer
import ciyin.media.directory.MediaDirectoryAutoConfiguration

/**
 * 用户媒体目录模块 KoinBoot 初始化入口。
 *
 * 使用示例：
 * ```kotlin
 * runKoinBoot {
 *     MediaDirectoryBootInitializer()
 * }
 * ```
 *
 * 注意：本初始化器只携带平台无关的自动配置；Android 宿主可以不在 Koin
 * 中声明 [ciyin.media.directory.DirectoryPickerLauncher]，此时只有
 * `MediaDirectorySession.requestAccess` 会抛 Unsupported；页面级 AccessFactory 仍可直接提供授权结果。
 *
 * Desktop / iOS 平台可不提供 launcher（Desktop 使用内置 JFileChooser，
 * iOS 由宿主通过实际注入的 launcher 调用）。
 */
@Suppress("unused")
val MediaDirectoryBootInitializer: KoinBootInitializer = {
    autoConfigurations(MediaDirectoryAutoConfiguration)
}
