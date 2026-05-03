package ciyin.platform.io

import ciyin.io.File
import ciyin.io.copyTo
import ciyin.platform.Context
import ciyin.platform.files
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URI

actual suspend fun String.copyUriToTempFile(context: Context): File =
    withContext(Dispatchers.IO) {
        val uriString = this@copyUriToTempFile.trim()
        require(!uriString.startsWith("content:", ignoreCase = true)) {
            "桌面端不支持 Android content:// URI"
        }

        val srcPath = resolveDesktopLocalPath(uriString)
        val src = File(srcPath)
        check(src.exists() && src.isFile) {
            "源文件不存在或不是文件: $srcPath"
        }

        val cacheDir = context.files.cacheDir
        cacheDir.mkdirs()

        val fileName = src.name.takeIf { it.isNotBlank() }
            ?: "temp_${System.currentTimeMillis()}"
        val dest = File(cacheDir.path, fileName)
        src.copyTo(dest, overwrite = true)
        dest
    }

private fun resolveDesktopLocalPath(uriOrPath: String): String =
    when {
        uriOrPath.startsWith("file:", ignoreCase = true) -> {
            val uri = URI(uriOrPath)
            require(uri.scheme.equals("file", ignoreCase = true)) {
                "仅支持 file:// 本地文件 URI"
            }
            java.io.File(uri).absolutePath
        }

        else -> uriOrPath
    }
