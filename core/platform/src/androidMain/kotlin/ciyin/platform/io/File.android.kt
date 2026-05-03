package ciyin.platform.io

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import ciyin.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileOutputStream


actual suspend fun String.copyUriToTempFile(
    context: Context
): File = withContext(Dispatchers.IO) {
    val uri = Uri.parse(this@copyUriToTempFile)
    val contentResolver = context.contentResolver
    val fileName = runCatching {
        // 尝试从 ContentResolver 查询 displayName
        var name: String? = null
        val cursor =
            contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                name = it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
            }
        }
        name ?: uri.lastPathSegment?.substringAfterLast("/")
        ?: "temp_${System.currentTimeMillis()}"
    }.getOrDefault("temp_${System.currentTimeMillis()}")

    val tempFile = java.io.File(context.cacheDir, fileName)
    contentResolver.openInputStream(uri)?.use { input ->
        FileOutputStream(tempFile).use { input.copyTo(it) }
    }

    File(tempFile.absolutePath)
}