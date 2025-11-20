package ciyin.system.utils.mime

import platform.UniformTypeIdentifiers.UTType

internal actual fun getPlatformMimeType(filename: String): String {
    return try {
        val extension = filename.substringAfterLast('.', "")
        if (extension.isEmpty()) return ""

        val utType = UTType.typeWithFilenameExtension(extension)
        utType?.preferredMIMEType ?: ""
    } catch (e: Exception) {
        ""
    }
}