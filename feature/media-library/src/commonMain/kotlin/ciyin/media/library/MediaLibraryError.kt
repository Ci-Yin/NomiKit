package ciyin.media.library

/** 系统媒体库技术错误。 */
sealed interface MediaLibraryError {
    /** 源文件不存在或平台引用已经失效。 */
    data class NotFound(
        /** 可供诊断的非敏感错误说明。 */
        val message: String? = null,
    ) : MediaLibraryError

    /** 目标命名空间中已存在相同媒体。 */
    data class AlreadyExists(
        /** 可供诊断的非敏感错误说明。 */
        val message: String? = null,
    ) : MediaLibraryError

    /** 当前应用没有平台要求的媒体库权限。 */
    data class PermissionDenied(
        /** 可供诊断的非敏感错误说明。 */
        val message: String? = null,
    ) : MediaLibraryError

    /** 系统媒体库或目标卷空间不足。 */
    data class NoSpace(
        /** 可供诊断的非敏感错误说明。 */
        val message: String? = null,
    ) : MediaLibraryError

    /** 当前平台或目标分类不支持该操作。 */
    data class Unsupported(
        /** 不支持原因。 */
        val message: String,
    ) : MediaLibraryError

    /** 未归类的底层文件或平台 I/O 失败。 */
    data class Io(
        /** 可供诊断的非敏感错误说明。 */
        val message: String? = null,
        /** 原始底层异常。 */
        val cause: Throwable? = null,
    ) : MediaLibraryError
}

/** 将 [MediaLibraryError] 以异常形式暴露给挂起 API 调用方。 */
class MediaLibraryException(
    /** 结构化技术错误。 */
    val error: MediaLibraryError,
) : RuntimeException(error.message(), (error as? MediaLibraryError.Io)?.cause)

/** 返回技术错误的可读说明。 */
private fun MediaLibraryError.message(): String = when (this) {
    is MediaLibraryError.NotFound -> message ?: "媒体或源文件不存在"
    is MediaLibraryError.AlreadyExists -> message ?: "目标媒体已经存在"
    is MediaLibraryError.PermissionDenied -> message ?: "没有系统媒体库权限"
    is MediaLibraryError.NoSpace -> message ?: "系统媒体库空间不足"
    is MediaLibraryError.Unsupported -> message
    is MediaLibraryError.Io -> message ?: "系统媒体库操作失败"
}

/** 以 [MediaLibraryException] 抛出当前技术错误。 */
internal fun MediaLibraryError.raise(): Nothing = throw MediaLibraryException(this)
