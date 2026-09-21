package ciyin.media.directory

import ciyin.io.AccessDeniedException
import ciyin.io.File
import ciyin.io.NoSuchFileException
import ciyin.io.SystemFileSystem
import kotlinx.coroutines.CancellationException
import okio.FileNotFoundException

/** 已完成公共校验的提交请求路径信息。 */
internal data class ValidatedCommitRequest(
    /** 校验时读取的严格源文件大小。 */
    val sourceSize: Long,
)

/**
 * 校验 [MediaDirectorySession.commit] 请求并返回可供平台实现使用的归一化信息。
 *
 * @throws MediaDirectoryException 当 [fileName] 或 [mimeType] 非法时抛出。
 */
internal fun validateCommitRequest(
    fileName: String,
    mimeType: String,
    source: File
): ValidatedCommitRequest {
    validateFileName(fileName)
    if (mimeType.isBlank()) {
        MediaDirectoryError.Unsupported("MIME 类型不能为空").raise()
    }
    return ValidatedCommitRequest(sourceSize = source.strictSize())
}

/** 校验提交后的文件名为单个安全路径组件。 */
private fun validateFileName(fileName: String) {
    if (
        fileName.isBlank() ||
        fileName == "." ||
        fileName == ".." ||
        fileName.any { it == '/' || it == '\\' || it == ':' || it == '\u0000' }
    ) {
        MediaDirectoryError.Unsupported("文件名必须是非空的单个文件名").raise()
    }
}

/** 严格读取普通文件大小，读取失败时抛出结构化 IO 错误。 */
internal fun File.strictSize(): Long = try {
    val metadata = SystemFileSystem.metadata(toPath())
    if (!metadata.isRegularFile) {
        MediaDirectoryError.NotFound(toString()).raise()
    }
    metadata.size ?: MediaDirectoryError.Io("系统未返回源文件大小").raise()
} catch (exception: MediaDirectoryException) {
    throw exception
} catch (exception: CancellationException) {
    throw exception
} catch (exception: Throwable) {
    when (exception) {
        is AccessDeniedException -> MediaDirectoryError.PermissionDenied("没有读取源文件的权限")
            .raise()

        is NoSuchFileException,
        is FileNotFoundException,
            -> MediaDirectoryError.NotFound(toString()).raise()

        else -> MediaDirectoryError.Io("读取源文件元数据失败", exception).raise()
    }
}
