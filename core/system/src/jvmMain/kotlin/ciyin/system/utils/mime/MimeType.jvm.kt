package ciyin.system.utils.mime

import java.net.URLConnection

internal actual fun getPlatformMimeType(filename: String): String {
    return try {
        URLConnection.getFileNameMap().getContentTypeFor(filename) ?: ""
    } catch (e: Exception) {
        ""
    }
}