package ciyin.system.utils.mime


/**
 *
 * kotlin文件作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2025/11/1 14:54
 */

/**
 * 获取文件MIME类型（根据扩展名）
 *
 * @param defaultMime 默认MIME类型，当无法识别时返回
 * @return MIME类型字符串
 *
 * 示例:
 * ```
 * "photo.jpg".getMime() // "image/jpeg"
 * "document.pdf".getMime() // "application/pdf"
 * "unknown.xyz".getMime() // "application/octet-stream"
 * ```
 */
fun CharSequence.getMime(defaultMime: String = DEFAULT_MIME_TYPE): String {
    // 清理文件名：去除前后空格和 URL 查询参数
    val filename = this.toString().trim()
        .substringBefore('?')  // 移除 URL 查询参数
        .substringBefore('#')  // 移除 URL fragment

    val extension = filename.substringAfterLast('.', "").lowercase()

    // 1. 先检查自定义类型（最高优先级）
    if (extension.isNotEmpty() && MimeTypeManager.hasCustomType(extension)) {
        return MimeTypeManager.getMimeType(filename, defaultMime)
    }

    // 2. 检查内置类型（第二优先级）
    if (extension.isNotEmpty()) {
        val builtInMime = MimeTypeManager.builtInMimeTypes[extension]
        if (builtInMime != null) {
            return builtInMime
        }
    }

    // 3. 使用平台原生API（第三优先级）
    val platformMime = getPlatformMimeType(filename)
    if (platformMime.isNotEmpty()) {
        return platformMime
    }

    // 4. 返回默认值
    return defaultMime
}

/**
 * 获取扩展名（根据MIME类型）
 */
fun String.getExtensionFromMime(): String? {
    return MimeTypeManager.getExtension(this)
}

/**
 * 检查是否为图片文件
 */
fun CharSequence.isImageFile(): Boolean {
    return MimeTypeManager.isImage(this.getMime())
}

/**
 * 检查是否为视频文件
 */
fun CharSequence.isVideoFile(): Boolean {
    return MimeTypeManager.isVideo(this.getMime())
}

/**
 * 检查是否为音频文件
 */
fun CharSequence.isAudioFile(): Boolean {
    return MimeTypeManager.isAudio(this.getMime())
}

/**
 * 检查是否为文档文件
 */
fun CharSequence.isDocumentFile(): Boolean {
    val mime = this.getMime()
    return mime.startsWith("application/pdf") ||
            mime.startsWith("application/msword") ||
            mime.startsWith("application/vnd.openxmlformats") ||
            mime.startsWith("application/vnd.oasis.opendocument")
}

/**
 * 检查是否为压缩文件
 */
fun CharSequence.isArchiveFile(): Boolean {
    val mime = this.getMime()
    return mime in setOf(
        "application/zip",
        "application/x-rar-compressed",
        "application/x-7z-compressed",
        "application/x-tar",
        "application/gzip"
    )
}

/**
 * 平台特定的MIME类型获取
 */
internal expect fun getPlatformMimeType(filename: String): String

/**
 * 默认MIME类型
 */
const val DEFAULT_MIME_TYPE = "application/octet-stream"