package ciyin.media.directory

import ciyin.koin.configuration.koinAutoConfiguration
import ciyin.koin.configuration.onMissInstances
import ciyin.platform.Context

/**
 * 用户媒体目录模块 KoinBoot 自动配置。
 *
 * 仅在宿主尚未声明 [MediaDirectorySession] 时生效；装配时会从 Koin 中读取可能存在的
 * [DirectoryPickerLauncher]。Android 未提供 launcher 时仍可构造会话，只有 [requestAccess] 会延迟返回 Unsupported。
 *
 * 业务 Koin 注册示例：
 * ```kotlin
 * runKoinBoot {
 *     MediaDirectoryBootInitializer()
 *     modules(module {
 *         single<DirectoryPickerLauncher> { rememberActivityDirectoryPickerLauncher() }
 *     })
 * }
 * ```
 */
internal val MediaDirectoryAutoConfiguration = koinAutoConfiguration {
    module {
        onMissInstances<MediaDirectorySession> {
            single {
                createMediaDirectorySession(
                    context = get<Context>(),
                    pickerLauncher = getOrNull(),
                )
            }
        }
    }
}
