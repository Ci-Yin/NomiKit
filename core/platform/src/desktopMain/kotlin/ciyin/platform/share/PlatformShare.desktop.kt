package ciyin.platform.share

import ciyin.platform.Context
import ciyin.platform.Platform
import ciyin.platform.currentPlatform
import java.net.URI
import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Desktop 系统分享实现。
 *
 * Windows 使用系统 Share Sheet；macOS 与 Linux 明确返回不支持。
 *
 * @param context Desktop 上下文
 * @param payload 系统分享内容
 */
@Suppress("UNUSED_PARAMETER")
actual suspend fun sharePlatformContent(
    context: Context,
    payload: PlatformSharePayload,
): PlatformShareResult = shareDesktopPlatformContent(
    platform = currentPlatform(),
    payload = payload,
    windowsLauncher = windowsPlatformShareLauncher,
)

/**
 * 按 Desktop 平台选择系统分享实现。
 *
 * @param platform 当前 Desktop 平台
 * @param payload 系统分享内容
 * @param windowsLauncher Windows 系统分享启动器
 */
internal suspend fun shareDesktopPlatformContent(
    platform: Platform,
    payload: PlatformSharePayload,
    windowsLauncher: WindowsShareLauncher,
): PlatformShareResult {
    if (platform !is Platform.Windows) return PlatformShareResult.Unsupported
    return windowsLauncher.share(payload.toWindowsSharePayload())
}

/** Windows 系统分享启动器。 */
internal fun interface WindowsShareLauncher {
    /** 打开 Windows 系统分享面板。 */
    suspend fun share(payload: WindowsSharePayload): PlatformShareResult
}

/** 已完成平台无关校验的 Windows 分享载荷。 */
internal sealed interface WindowsSharePayload {
    /** Windows DataPackage 必需的标题。 */
    val title: String

    /** Windows 文本分享载荷。 */
    data class Text(
        override val title: String,
        val value: String,
    ) : WindowsSharePayload

    /** Windows 文件分享载荷。 */
    data class Files(
        override val title: String,
        val values: List<WindowsShareFile>,
    ) : WindowsSharePayload
}

/**
 * Windows 可分享文件。
 *
 * @property path 绝对规范文件路径
 * @property unavailableReason 后续 WinRT 解析失败时使用的技术原因
 */
internal data class WindowsShareFile(
    val path: Path,
    val unavailableReason: PlatformShareFailureReason,
)

/** 将公共分享载荷校验并转换为 Windows 载荷。 */
internal fun PlatformSharePayload.toWindowsSharePayload(): WindowsSharePayload {
    val explicitTitle = validatedPlatformShareTitleOrNull()
    return when (this) {
        is PlatformSharePayload.Text -> {
            val text = value.validatedPlatformShareText()
            WindowsSharePayload.Text(
                title = explicitTitle ?: text.derivedWindowsTextTitle(),
                value = text,
            )
        }

        is PlatformSharePayload.File -> {
            val file = value.toWindowsShareFile()
            WindowsSharePayload.Files(
                title = explicitTitle ?: value.derivedWindowsFileTitle(file.path),
                values = listOf(file),
            )
        }

        is PlatformSharePayload.Files -> {
            if (values.isEmpty()) {
                throw PlatformShareException(
                    reason = PlatformShareFailureReason.InvalidPayload,
                    message = "多文件分享列表不能为空",
                )
            }
            val files = values.map(PlatformShareFile::toWindowsShareFile)
            WindowsSharePayload.Files(
                title = explicitTitle ?: values.first().derivedWindowsFileTitle(files.first().path),
                values = files,
            )
        }
    }
}

/** 从分享文本的第一个非空行派生 Windows 标题。 */
private fun String.derivedWindowsTextTitle(): String =
    lineSequence()
        .map(String::trim)
        .first(String::isNotEmpty)

/** 从展示名称或实际文件名派生 Windows 标题。 */
private fun PlatformShareFile.derivedWindowsFileTitle(path: Path): String =
    displayName?.trim()?.takeIf(String::isNotEmpty)
        ?: path.fileName?.toString()?.takeIf(String::isNotEmpty)
        ?: throw PlatformShareException(
            reason = PlatformShareFailureReason.InvalidPayload,
            message = "无法从分享文件派生系统分享标题: $path",
        )

/** 将公共文件来源转换为 Windows 绝对文件路径。 */
private fun PlatformShareFile.toWindowsShareFile(): WindowsShareFile {
    mimeType.validatedPlatformShareMimeType()
    val resolvedFile = when (val currentSource = source) {
        is PlatformShareFileSource.LocalFile -> WindowsShareFile(
            path = currentSource.value.path.toAbsoluteWindowsSharePath(
                reason = PlatformShareFailureReason.FileUnavailable,
            ),
            unavailableReason = PlatformShareFailureReason.FileUnavailable,
        )

        is PlatformShareFileSource.Uri -> WindowsShareFile(
            path = currentSource.value.toWindowsFileUriPath(),
            unavailableReason = PlatformShareFailureReason.UriUnavailable,
        )
    }
    resolvedFile.requireAvailableWindowsShareFile()
    return resolvedFile
}

/** 将路径字符串转换为 Windows 分享使用的绝对规范路径。 */
private fun String.toAbsoluteWindowsSharePath(reason: PlatformShareFailureReason): Path = try {
    Paths.get(this).toAbsolutePath().normalize()
} catch (exception: InvalidPathException) {
    throw PlatformShareException(
        reason = reason,
        message = "分享文件路径无效: $this",
        cause = exception,
    )
} catch (exception: SecurityException) {
    throw PlatformShareException(
        reason = PlatformShareFailureReason.PermissionDenied,
        message = "当前进程无权解析分享文件路径: $this",
        cause = exception,
    )
}

/** 校验并转换 Windows 支持的 file URI。 */
private fun String.toWindowsFileUriPath(): Path {
    val value = trim()
    if (value.isEmpty()) {
        throw PlatformShareException(
            reason = PlatformShareFailureReason.InvalidUri,
            message = "Windows 分享 URI 不能为空",
        )
    }
    val uri = try {
        URI(value)
    } catch (exception: Exception) {
        throw PlatformShareException(
            reason = PlatformShareFailureReason.InvalidUri,
            message = "Windows 分享 URI 格式无效: $value",
            cause = exception,
        )
    }
    if (!uri.scheme.equals(FILE_URI_SCHEME, ignoreCase = true)) {
        throw PlatformShareException(
            reason = PlatformShareFailureReason.InvalidUri,
            message = "Windows 文件分享只支持 file URI: $value",
        )
    }
    return try {
        Paths.get(uri).toAbsolutePath().normalize()
    } catch (exception: Exception) {
        throw PlatformShareException(
            reason = PlatformShareFailureReason.InvalidUri,
            message = "Windows file URI 无法转换为本地路径: $value",
            cause = exception,
        )
    }
}

/** 确认 Windows 分享文件存在、为普通文件且可读。 */
private fun WindowsShareFile.requireAvailableWindowsShareFile() {
    try {
        if (!Files.exists(path) || !Files.isRegularFile(path) || !Files.isReadable(path)) {
            throw PlatformShareException(
                reason = unavailableReason,
                message = "Windows 分享文件不存在、不是普通文件或无法读取: $path",
            )
        }
        if (path.fileName.toString().substringAfterLast('.', "").lowercase() in UNSUPPORTED_STORAGE_FILE_EXTENSIONS) {
            throw PlatformShareException(
                reason = unavailableReason,
                message = "Windows StorageFile 不支持此文件类型: $path",
            )
        }
    } catch (exception: PlatformShareException) {
        throw exception
    } catch (exception: SecurityException) {
        throw PlatformShareException(
            reason = PlatformShareFailureReason.PermissionDenied,
            message = "当前进程无权读取 Windows 分享文件: $path",
            cause = exception,
        )
    }
}

/** Windows 本地文件 URI 协议。 */
private const val FILE_URI_SCHEME: String = "file"

/** Windows StorageFile 无法表示的文件扩展名。 */
private val UNSUPPORTED_STORAGE_FILE_EXTENSIONS: Set<String> = setOf("lnk", "url", "wsh")
