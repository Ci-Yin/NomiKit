package ciyin.system.utils.mime

//internal actual fun getPlatformMimeType(filename: String): String {
//    return try {
//        val extension = filename.substringAfterLast('.', "")
//        if (extension.isNotEmpty()) {
//            MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase()) ?: ""
//        } else {
//            ""
//        }
//    } catch (e: Exception) {
//        ""
//    }
//}