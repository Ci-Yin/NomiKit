package ciyin.media.library

import ciyin.io.AccessDeniedException
import ciyin.io.File
import ciyin.io.NoSuchFileException
import ciyin.io.SystemFileSystem
import kotlinx.coroutines.CancellationException
import okio.FileNotFoundException

/** 已完成公共校验的发布请求路径信息。 */
internal data class ValidatedMediaPublishRequest(
    /** 归一化后的相对目录段。 */
    val relativeDirectorySegments: List<String>,
    /** 校验时读取的严格源文件大小。 */
    val sourceSize: Long,
)

/** 校验发布请求并返回可供平台实现使用的归一化信息。 */
internal fun MediaPublishRequest.validate(): ValidatedMediaPublishRequest {
    validateDisplayName(displayName)
    if (mimeType.isBlank()) {
        MediaLibraryError.Unsupported("MIME 类型不能为空").raise()
    }
    val relativeDirectorySegments = validateRelativeDirectory(relativeDirectory)
    return ValidatedMediaPublishRequest(
        relativeDirectorySegments = relativeDirectorySegments,
        sourceSize = source.strictSize(),
    )
}

/** 严格读取普通文件大小，读取失败时抛出结构化 I/O 错误。 */
internal fun File.strictSize(): Long = try {
    val metadata = SystemFileSystem.metadata(toPath())
    if (!metadata.isRegularFile) {
        MediaLibraryError.NotFound("源文件不存在或不是普通文件").raise()
    }
    metadata.size
        ?: MediaLibraryError.Io("系统未返回源文件大小").raise()
} catch (exception: MediaLibraryException) {
    throw exception
} catch (exception: CancellationException) {
    throw exception
} catch (exception: Throwable) {
    when (exception) {
        is AccessDeniedException -> MediaLibraryError.PermissionDenied("没有读取源文件的权限").raise()
        is NoSuchFileException,
        is FileNotFoundException,
        -> MediaLibraryError.NotFound("源文件不存在或不是普通文件").raise()
        else -> MediaLibraryError.Io("读取源文件元数据失败", exception).raise()
    }
}

/** 校验发布后的显示名称只包含一个安全路径组件。 */
private fun validateDisplayName(displayName: String) {
    if (
        displayName.isBlank() ||
        displayName == "." ||
        displayName == ".." ||
        displayName.any { it == '/' || it == '\\' || it == ':' || it == '\u0000' }
    ) {
        MediaLibraryError.Unsupported("显示名称必须是非空的单个文件名").raise()
    }
}

/** 校验并拆分不会逃逸目标媒体分类的相对目录。 */
private fun validateRelativeDirectory(relativeDirectory: String?): List<String> {
    if (relativeDirectory == null) return emptyList()
    if (relativeDirectory.isBlank()) {
        MediaLibraryError.Unsupported("相对目录不能为空白字符串").raise()
    }
    val normalized = relativeDirectory.replace('\\', '/')
    if (normalized.startsWith('/') || normalized.endsWith('/')) {
        MediaLibraryError.Unsupported("相对目录必须位于目标媒体分类内").raise()
    }
    val segments = normalized.split('/')
    if (
        segments.any { segment ->
            segment.isBlank() || segment == "." || segment == ".." ||
                segment.contains(':') || segment.contains('\u0000')
        }
    ) {
        MediaLibraryError.Unsupported("相对目录包含非法路径段").raise()
    }
    return segments
}
