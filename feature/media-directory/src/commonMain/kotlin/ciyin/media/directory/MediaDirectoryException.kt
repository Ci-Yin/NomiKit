package ciyin.media.directory

/**
 * 将 [MediaDirectoryError] 以异常形式暴露给挂起 API 调用方。
 *
 * @property error 结构化技术错误。
 */
class MediaDirectoryException(
    val error: MediaDirectoryError,
) : RuntimeException(error.toString(), null) {
    init {
        val cause = (error as? MediaDirectoryError.Io)?.cause
        if (cause != null) addSuppressed(cause)
    }
}

/** 以 [MediaDirectoryException] 抛出当前技术错误。 */
internal fun MediaDirectoryError.raise(): Nothing = throw MediaDirectoryException(this)
