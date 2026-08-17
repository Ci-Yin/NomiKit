package ciyin.platform.share

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Android 系统分享实现。
 *
 * @param context Android 上下文
 * @param payload 系统分享内容
 */
actual suspend fun sharePlatformContent(
    context: ciyin.platform.Context,
    payload: PlatformSharePayload,
): PlatformShareResult = withContext(Dispatchers.Main) {
    val shareIntent = context.buildShareIntent(payload)
    val chooserIntent = Intent.createChooser(shareIntent, null).apply {
        if (context !is Activity) {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    try {
        context.startActivity(chooserIntent)
        PlatformShareResult.Opened
    } catch (_: ActivityNotFoundException) {
        PlatformShareResult.Unsupported
    } catch (exception: SecurityException) {
        throw PlatformShareException(
            reason = PlatformShareFailureReason.PermissionDenied,
            message = "Android 系统拒绝启动分享面板或授予 URI 读取权限",
            cause = exception,
        )
    } catch (exception: Exception) {
        throw PlatformShareException(
            reason = PlatformShareFailureReason.LaunchFailed,
            message = "Android 系统分享面板启动失败",
            cause = exception,
        )
    }
}

/** 根据分享载荷构建 Android Intent。 */
private fun Context.buildShareIntent(payload: PlatformSharePayload): Intent = when (payload) {
    is PlatformSharePayload.Text -> Intent(Intent.ACTION_SEND).apply {
        type = TEXT_MIME_TYPE
        putExtra(Intent.EXTRA_TEXT, payload.value.validatedPlatformShareText())
        payload.validatedPlatformShareTitleOrNull()?.let {
            putExtra(Intent.EXTRA_TITLE, it)
        }
    }

    is PlatformSharePayload.File -> buildSingleFileShareIntent(payload)
    is PlatformSharePayload.Files -> buildMultipleFileShareIntent(payload)
}

/** 构建单文件分享 Intent。 */
private fun Context.buildSingleFileShareIntent(payload: PlatformSharePayload.File): Intent {
    val resolvedFile = resolveShareFile(payload.value)
    return Intent(Intent.ACTION_SEND).apply {
        type = resolvedFile.mimeType
        putExtra(Intent.EXTRA_STREAM, resolvedFile.uri)
        val title = payload.validatedPlatformShareTitleOrNull() ?: resolvedFile.displayName
        title?.let { putExtra(Intent.EXTRA_TITLE, it) }
        clipData = ClipData.newUri(
            contentResolver,
            resolvedFile.displayName ?: resolvedFile.uri.lastPathSegment.orEmpty(),
            resolvedFile.uri,
        )
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
}

/** 构建多文件分享 Intent。 */
private fun Context.buildMultipleFileShareIntent(payload: PlatformSharePayload.Files): Intent {
    val files = payload.values
    if (files.isEmpty()) {
        throw PlatformShareException(
            reason = PlatformShareFailureReason.InvalidPayload,
            message = "多文件分享列表不能为空",
        )
    }

    val resolvedFiles = files.map(::resolveShareFile)
    val streamUris = ArrayList(resolvedFiles.map(AndroidShareFile::uri))
    val firstFile = resolvedFiles.first()
    val sharedClipData = ClipData.newUri(
        contentResolver,
        firstFile.displayName ?: firstFile.uri.lastPathSegment.orEmpty(),
        firstFile.uri,
    ).apply {
        resolvedFiles.drop(1).forEach { addItem(ClipData.Item(it.uri)) }
    }

    return Intent(Intent.ACTION_SEND_MULTIPLE).apply {
        type = resolvedFiles.map(AndroidShareFile::mimeType).resolveCommonPlatformShareMimeType()
        putParcelableArrayListExtra(Intent.EXTRA_STREAM, streamUris)
        putExtra(
            Intent.EXTRA_MIME_TYPES,
            resolvedFiles.map(AndroidShareFile::mimeType).distinct().toTypedArray(),
        )
        val title = payload.validatedPlatformShareTitleOrNull() ?: firstFile.displayName
        title?.let { putExtra(Intent.EXTRA_TITLE, it) }
        clipData = sharedClipData
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
}

/** 将跨平台文件描述解析为 Android 可授权的 content URI。 */
private fun Context.resolveShareFile(file: PlatformShareFile): AndroidShareFile {
    val mimeType = file.mimeType.validatedPlatformShareMimeType()

    val uri = when (val source = file.source) {
        is PlatformShareFileSource.Uri -> resolveShareUri(source.value)
        is PlatformShareFileSource.LocalFile -> resolveLocalFileUri(source.value)
    }
    return AndroidShareFile(
        uri = uri,
        mimeType = mimeType,
        displayName = file.displayName,
    )
}

/** 校验并解析调用方提供的 Android URI。 */
private fun Context.resolveShareUri(value: String): Uri {
    val uriValue = value.trim()
    if (uriValue.isEmpty()) {
        throw PlatformShareException(
            reason = PlatformShareFailureReason.InvalidUri,
            message = "分享 URI 不能为空",
        )
    }

    val uri = Uri.parse(uriValue)
    return when (uri.scheme?.lowercase()) {
        "content", "android.resource" -> uri.also(::requireReadableUri)
        "file" -> {
            val path = uri.path
                ?: throw PlatformShareException(
                    reason = PlatformShareFailureReason.InvalidUri,
                    message = "file URI 不包含本地路径: $uriValue",
                )
            resolveLocalFileUri(ciyin.io.File(path))
        }

        else -> throw PlatformShareException(
            reason = PlatformShareFailureReason.InvalidUri,
            message = "Android 文件分享只支持 content、android.resource 或 file URI: $uriValue",
        )
    }
}

/** 确认 URI 内容可由当前进程读取。 */
private fun Context.requireReadableUri(uri: Uri) {
    try {
        val descriptor = contentResolver.openFileDescriptor(uri, "r")
            ?: throw PlatformShareException(
                reason = PlatformShareFailureReason.UriUnavailable,
                message = "分享 URI 无法打开: $uri",
            )
        descriptor.use { }
    } catch (exception: PlatformShareException) {
        throw exception
    } catch (exception: SecurityException) {
        throw PlatformShareException(
            reason = PlatformShareFailureReason.PermissionDenied,
            message = "当前进程没有分享 URI 的读取权限: $uri",
            cause = exception,
        )
    } catch (exception: Exception) {
        throw PlatformShareException(
            reason = PlatformShareFailureReason.UriUnavailable,
            message = "分享 URI 不存在或无法读取: $uri",
            cause = exception,
        )
    }
}

/** 将本地文件转换为模块 FileProvider 的 content URI。 */
private fun Context.resolveLocalFileUri(file: ciyin.io.File): Uri {
    if (!file.exists() || !file.isFile) {
        throw PlatformShareException(
            reason = PlatformShareFailureReason.FileUnavailable,
            message = "分享文件不存在或不是普通文件: ${file.path}",
        )
    }

    return try {
        FileProvider.getUriForFile(
            this,
            "$packageName$FILE_PROVIDER_AUTHORITY_SUFFIX",
            java.io.File(file.path),
        )
    } catch (exception: IllegalArgumentException) {
        throw PlatformShareException(
            reason = PlatformShareFailureReason.PermissionDenied,
            message = "分享文件不在 FileProvider 允许的 cache、files 或 external-files 目录内: ${file.path}",
            cause = exception,
        )
    } catch (exception: SecurityException) {
        throw PlatformShareException(
            reason = PlatformShareFailureReason.PermissionDenied,
            message = "无法为分享文件创建可授权的 content URI: ${file.path}",
            cause = exception,
        )
    }
}

/**
 * Android 已解析分享文件。
 *
 * @property uri 可向目标应用授权的 URI
 * @property mimeType 文件 MIME 类型
 * @property displayName 分享面板可使用的展示名称
 */
private data class AndroidShareFile(
    val uri: Uri,
    val mimeType: String,
    val displayName: String?,
)

/** 纯文本 MIME 类型。 */
private const val TEXT_MIME_TYPE: String = "text/plain"

/** 模块 FileProvider authority 后缀。 */
private const val FILE_PROVIDER_AUTHORITY_SUFFIX: String = ".ciyin.platform.share.fileprovider"
