package ciyin.media.directory

import ciyin.platform.Context

/**
 * iOS 用户目录实现占位。
 *
 * 真实实现需要绑定 `UIDocumentPickerViewController` 与 security-scoped bookmark。
 * 由于这部分依赖 UI 控制器与 cinterop 细节，本模块公共层只暴露"宿主必须自己实现
 * 真实 IO 逻辑"的契约：
 *
 * - 宿主在使用本模块前必须实现 [DirectoryPickerLauncher] 并把它注入到
 *   `createMediaDirectorySession(context, launcher)`；
 * - 若宿主未注入 launcher，本 actual 会抛 [MediaDirectoryError.Unsupported]，
 *   方便业务层在编译期就发现遗漏。
 *
 * WALIKALI 接入阶段会新增一个真实的 iOS actual（使用 `UIDocumentPickerViewController`
 * + bookmark），这里保留 stub 占位以便公共 API 与 Android / Desktop 同步可编译。
 */
internal class IosMediaDirectorySession(
    /** 宿主注入的目录选择器。 */
    private val pickerLauncher: DirectoryPickerLauncher,
) : MediaDirectorySession {

    override suspend fun requestAccess(): MediaDirectoryAccess =
        MediaDirectoryError.Unsupported("iOS 用户目录功能在当前 NomiKit 版本下为占位实现，请通过宿主 KoinBoot 注入真实实现")
            .raise()

    override suspend fun validate(accessToken: MediaDirectoryAccessToken): Unit =
        MediaDirectoryError.Unsupported("iOS 用户目录功能在当前 NomiKit 版本下为占位实现").raise()

    override suspend fun commit(
        accessToken: MediaDirectoryAccessToken,
        source: ciyin.io.File,
        fileName: String,
        mimeType: String,
    ): MediaDirectoryEntry =
        MediaDirectoryError.Unsupported("iOS 用户目录功能在当前 NomiKit 版本下为占位实现").raise()

    override suspend fun delete(
        accessToken: MediaDirectoryAccessToken,
        location: String,
    ): Unit =
        MediaDirectoryError.Unsupported("iOS 用户目录功能在当前 NomiKit 版本下为占位实现").raise()

    override suspend fun exists(
        accessToken: MediaDirectoryAccessToken,
        location: String,
    ): Boolean = false
}

/** iOS 平台入口。 */
actual fun createMediaDirectorySession(
    context: Context,
    pickerLauncher: DirectoryPickerLauncher?,
): MediaDirectorySession = IosMediaDirectorySession(
    pickerLauncher = pickerLauncher
        ?: object : DirectoryPickerLauncher {
            override suspend fun pick(): String? {
                MediaDirectoryError.Unsupported(
                    "iOS 必须由宿主提供绑定 UIViewController 的 DirectoryPickerLauncher",
                ).raise()
            }
        },
)
