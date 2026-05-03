package ciyin.platform.io

import ciyin.io.File
import ciyin.io.copyTo
import ciyin.platform.Context
import ciyin.platform.files
import ciyin.platform.time.currentTimeMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual suspend fun String.copyUriToTempFile(context: Context): File =
    withContext(Dispatchers.Default) {
        val uriString = this@copyUriToTempFile.trim()
        require(!uriString.startsWith("content:", ignoreCase = true)) {
            "Native 平台不支持 Android content:// URI"
        }

        val srcPath = resolveNativeLocalPath(uriString)
        val src = File(srcPath)
        check(src.exists() && src.isFile) {
            "源文件不存在或不是文件: $srcPath"
        }

        val cacheDir = context.files.cacheDir
        cacheDir.mkdirs()

        val fileName = src.name.takeIf { it.isNotBlank() }
            ?: "temp_${currentTimeMillis()}"
        val dest = File(cacheDir.path, fileName)
        src.copyTo(dest, overwrite = true)
        dest
    }

private fun resolveNativeLocalPath(uriOrPath: String): String =
    if (uriOrPath.startsWith("file:", ignoreCase = true)) {
        extractPathFromFileUri(uriOrPath)
    } else {
        uriOrPath
    }

/**
 * 解析 `file:` URI 中的路径（无需 Java [java.net.URI]，便于 Kotlin/Native 共用）。
 *
 * 覆盖常见形态：`file:///abs`、`file://host/path`、`file:/path`。
 */
private fun extractPathFromFileUri(fileUri: String): String {
    var rest = fileUri.trim().substringAfter("file:", "").substringAfter("FILE:", "")
    if (rest.startsWith("//")) {
        rest = rest.drop(2)
        val slash = rest.indexOf('/')
        rest = if (slash >= 0) rest.substring(slash) else "/"
    }
    return decodePercentEncodedPath(rest)
}

/**
 * 按字节还原 `%HH` 序列并与字面 UTF-8 字符混合解码为字符串（适用于路径中的非 ASCII）。
 */
private fun decodePercentEncodedPath(encodedPath: String): String {
    val bytes = mutableListOf<Byte>()
    var i = 0
    while (i < encodedPath.length) {
        if (encodedPath[i] == '%' && i + 2 < encodedPath.length) {
            val hex = encodedPath.substring(i + 1, i + 3).toIntOrNull(16)
            if (hex != null) {
                bytes.add(hex.toByte())
                i += 3
                continue
            }
        }
        val ch = encodedPath[i]
        ch.toString().encodeToByteArray().forEach { bytes.add(it) }
        i++
    }
    return bytes.toByteArray().decodeToString()
}
