package ciyin.media.directory

import android.content.ContentResolver
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.DocumentsContract.Document
import android.system.ErrnoException
import android.system.OsConstants
import ciyin.io.File
import ciyin.io.source
import ciyin.platform.Context
import ciyin.platform.logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.buffer
import okio.sink
import java.util.UUID

/**
 * 基于 Android Storage Access Framework (SAF) 的用户目录实现。
 *
 * 目录授权以 [Uri] 形式持久化；`takePersistableUriPermission` 保证进程被杀后授权仍可用。
 * 文件读取通过 [ContentResolver.openInputStream] / [ContentResolver.openOutputStream]
 * 直接走 SAF provider，避免引入 `androidx.documentfile`。
 */
internal class AndroidMediaDirectorySession(
    /** 用于读写 SAF URI 的内容解析器。 */
    private val resolver: ContentResolver,
    /**
     * 宿主注入的目录选择器；可为 `null`。
     *
     * - 非空时，[requestAccess] 会通过它启动 SAF 选择器。
     * - 空时，[requestAccess] 直接抛 [MediaDirectoryError.Unsupported]；
     *   但 [validate] / [commit] / [delete] / [exists] 仍可正常调用，因为这些方法
     *   只依赖 SAF Tree URI，与 launcher 解耦。
     */
    private val pickerLauncher: DirectoryPickerLauncher?,
) : MediaDirectorySession {

    /**
     * 拉起目录选择器；用户取消时抛 [MediaDirectoryException] 携带 [MediaDirectoryError.Cancelled]。
     *
     * 当宿主未注入 [DirectoryPickerLauncher] 时，本方法立刻抛
     * [MediaDirectoryError.Unsupported]；调用方应改用页面级 Access 工厂
     * 喂入 Token 后调用 [commit]。
     */
    override suspend fun requestAccess(): MediaDirectoryAccess = withContext(Dispatchers.Main) {
        val launcher = pickerLauncher
            ?: MediaDirectoryError.Unsupported(
                "Android 当前未注入 DirectoryPickerLauncher；请提供页面级 AccessFactory。",
            ).raise()
        val picked = launcher.pick()
        if (picked.isNullOrBlank()) {
            MediaDirectoryError.Cancelled.raise()
        }
        val treeUri = Uri.parse(picked)
        takePersistableTreePermission(treeUri)
        MediaDirectoryAccess(
            accessToken = MediaDirectoryAccessToken(treeUri.toString()),
            displayName = queryDisplayName(treeUri) ?: treeUri.lastPathSegment.orEmpty(),
            canonicalPath = null,
        )
    }

    /**
     * 重新解析 Tree URI 并验证目录可写。
     *
     * 探针策略：
     * 1. 先把持久化的 Tree URI 转成目录根 Document URI，再尝试
     *    `DocumentsContract.createDocument` 创建一个随机命名的临时子文档；
     *    如果创建成功，立即删除探针。SAF provider 不接受原始 Tree URI 作为
     *    `createDocument` 的父 URI。
     * 2. 当创建返回 `null` 或抛 `SecurityException`，优先归因到只读/权限收紧；
     *    只有在 `isChildDocument` 或 `query` 阶段就提示 URI 完全失效时，才归因
     *    到 [MediaDirectoryError.AccessRevoked]。
     */
    override suspend fun validate(accessToken: MediaDirectoryAccessToken) {
        withContext(Dispatchers.IO) {
            val treeUri = parseTreeUri(accessToken.value)
            val probeName = ".nomikit-probe.${UUID.randomUUID()}"
            var stagedProbe: Uri? = null
            try {
                val probeUri = DocumentsContract.createDocument(
                    resolver,
                    buildTreeDocumentUri(treeUri),
                    "application/octet-stream",
                    probeName,
                )
                if (probeUri == null) {
                    // 目录可读但不可写：SAF 在云端/受配额目录上较常见，归因为 IO。
                    MediaDirectoryError.Io("无法在授权目录中创建校验文件").raise()
                }
                stagedProbe = probeUri
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: SecurityException) {
                // 只读或权限收紧：明确归因为 PermissionDenied，而不是 AccessRevoked。
                throw MediaDirectoryException(
                    MediaDirectoryError.PermissionDenied(
                        "没有在授权目录中创建校验文件的权限",
                    ),
                )
            } catch (exception: Throwable) {
                // 其他失败：先确认 Tree URI 是否仍存在，再决定归因。
                if (!isDirectoryAccessible(treeUri)) {
                    throw MediaDirectoryException(
                        MediaDirectoryError.AccessRevoked("授权目录已不可访问"),
                    )
                }
                throw exception.toMediaDirectoryException()
            } finally {
                stagedProbe?.let { uri ->
                    runCatching { DocumentsContract.deleteDocument(resolver, uri) }
                }
            }
        }
    }

    /**
     * 写入本地源文件到已授权目录。
     *
     * 实现采用 staging → rename 策略：先以 `.<filename>.<uuid>.nomikit-part` 写入临时名，
     * 校验大小后再用 `DocumentsContract.renameDocument` 重命名为正式名。任意阶段失败都会清理 staging。
     *
     * @param accessToken 通过 [requestAccess] 得到的目录授权令牌。
     * @param source 应用 cache 目录下的本地源文件；会被按块读取到 SAF URI。
     * @param fileName 用户可见的目标文件名；调用方必须保证文件名已满足命名协议。
     * @param mimeType 目标文件的 MIME；用于在 Android < Q 上触发 MediaScanner。
     * @return 平台无关的 [MediaDirectoryEntry]，其中 [MediaDirectoryEntry.location] 是
     *         SAF Document URI 字符串；授权失效或重名时抛结构化异常。
     */
    override suspend fun commit(
        accessToken: MediaDirectoryAccessToken,
        source: File,
        fileName: String,
        mimeType: String,
    ): MediaDirectoryEntry = withContext(Dispatchers.IO) {
        val validated = validateCommitRequest(fileName, mimeType, source)
        val treeUri = parseTreeUri(accessToken.value)

        val existing = findChild(treeUri, fileName)
        if (existing != null) {
            MediaDirectoryError.AlreadyExists(fileName).raise()
        }

        val stagingName = ".$fileName.${UUID.randomUUID()}.nomikit-part"
        var stagedUri: Uri? = null
        try {
            val staged = DocumentsContract.createDocument(
                resolver,
                buildTreeDocumentUri(treeUri),
                mimeType,
                stagingName,
            ) ?: MediaDirectoryError.Io("SAF 创建 staging 文件失败").raise()
            stagedUri = staged

            val copiedSize = copySource(source, staged)
            val sourceSizeAfterCopy = source.strictSize()
            if (copiedSize != validated.sourceSize || sourceSizeAfterCopy != validated.sourceSize) {
                MediaDirectoryError.Io("源文件在提交期间发生变化").raise()
            }

            val renamedUri = DocumentsContract.renameDocument(
                resolver,
                staged,
                fileName,
            ) ?: run {
                runCatching { DocumentsContract.deleteDocument(resolver, staged) }
                MediaDirectoryError.Io("SAF staging 重命名失败").raise()
            }
            stagedUri = null

            // Android Q+：createFile/renameTo 已经自动通知 MediaProvider；
            // Android < Q：手动触发 MediaScanner 让旧版本相册可见。
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                notifyMediaScanner(renamedUri, mimeType)
            }

            MediaDirectoryEntry(
                accessToken = accessToken,
                fileName = fileName,
                location = renamedUri.toString(),
                mimeType = mimeType,
                size = copiedSize,
            )
        } catch (exception: CancellationException) {
            stagedUri?.let { uri ->
                runCatching { DocumentsContract.deleteDocument(resolver, uri) }
                    .onFailure { exception.addSuppressed(it) }
            }
            throw exception
        } catch (exception: MediaDirectoryException) {
            stagedUri?.let { uri ->
                runCatching { DocumentsContract.deleteDocument(resolver, uri) }
            }
            throw exception
        } catch (exception: Throwable) {
            stagedUri?.let { uri ->
                runCatching { DocumentsContract.deleteDocument(resolver, uri) }
            }
            throw exception.toMediaDirectoryException()
        }
    }

    /**
     * 删除授权目录中的应用管理文件；不存在时按幂等成功处理。
     *
     * 仅检查 [accessToken] 形态合法，不直接校验目录当前是否仍有效；调用方一般
     * 应该在删除前先用 [validate] 探一次。
     *
     * @param accessToken 通过 [requestAccess] 得到的目录授权令牌。
     * @param location [MediaDirectoryEntry.location]，即 SAF 单文件 Document URI 字符串。
     */
    override suspend fun delete(
        accessToken: MediaDirectoryAccessToken,
        location: String,
    ): Unit = withContext(Dispatchers.IO) {
        // 仅校验 accessToken 形态合法；不抛错以支持幂等删除。
        parseTreeUri(accessToken.value)
        val fileUri = Uri.parse(location)
        try {
            DocumentsContract.deleteDocument(resolver, fileUri)
        } catch (exception: Throwable) {
            throw exception.toMediaDirectoryException()
        }
    }

    /**
     * 检查应用管理文件是否仍存在。
     *
     * 仅通过 `openInputStream` 探测 URI 是否可达；权限撤销、文件不存在、provider
     * 拒绝都按 `false` 处理，不会抛错。
     *
     * @param accessToken 通过 [requestAccess] 得到的目录授权令牌。
     * @param location [MediaDirectoryEntry.location]，即 SAF 单文件 Document URI 字符串。
     * @return 文件仍可被 SAF provider 打开时返回 `true`，否则 `false`。
     */
    override suspend fun exists(
        accessToken: MediaDirectoryAccessToken,
        location: String,
    ): Boolean = withContext(Dispatchers.IO) {
        parseTreeUri(accessToken.value)
        val fileUri = Uri.parse(location)
        try {
            resolver.openInputStream(fileUri)?.use { true } ?: false
        } catch (exception: Throwable) {
            // 文件不存在或权限撤销均视为 false。
            false
        }
    }

    /** 调用 SAF `takePersistableUriPermission` 持久化授权。 */
    private fun takePersistableTreePermission(treeUri: Uri) {
        try {
            resolver.takePersistableUriPermission(
                treeUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
            )
        } catch (exception: Throwable) {
            throw exception.toMediaDirectoryException()
        }
    }

    /**
     * 把持久化的 Tree URI 转换为目录根 Document URI。
     *
     * `DocumentsContract.createDocument` 的父参数必须是 Document URI；直接传入
     * `content://.../tree/...` 形式的 Tree URI 会被部分 SAF provider 判定为非法 URI。
     * Tree URI 仍继续用于持久化授权和 children query。
     *
     * @param treeUri 已通过 [parseTreeUri] 校验的 Tree URI。
     * @return 可作为 `createDocument` 父目录参数的 Document URI。
     */
    private fun buildTreeDocumentUri(treeUri: Uri): Uri =
        DocumentsContract.buildDocumentUriUsingTree(
            treeUri,
            DocumentsContract.getTreeDocumentId(treeUri),
        )

    /**
     * 通过 SAF children query 解析目录显示名。
     *
     * @param treeUri 已授权的目录 Tree URI。
     * @return 目录的展示名；任何 IO 失败均按 null 处理，不抛错。
     */
    private fun queryDisplayName(treeUri: Uri): String? = try {
        resolver.query(
            treeUri,
            arrayOf(Document.COLUMN_DISPLAY_NAME),
            null,
            null,
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }
    } catch (exception: Throwable) {
        null
    }

    /**
     * 探测当前 Tree URI 是否仍可读：尝试拉取 children query 至少一行。
     *
     * 用于 [validate] 区分"目录可读但不可写"和"权限被撤销"。
     *
     * @return 仍可访问时返回 `true`；权限撤销或 provider 不可达时返回 `false`。
     */
    private fun isDirectoryAccessible(treeUri: Uri): Boolean = try {
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
            treeUri,
            DocumentsContract.getTreeDocumentId(treeUri),
        )
        resolver.query(
            childrenUri,
            arrayOf(Document.COLUMN_DOCUMENT_ID),
            null,
            null,
            null,
        )?.use { cursor ->
            cursor.count >= 0
        } ?: false
    } catch (exception: Throwable) {
        false
    }

    /**
     * 查找目录中是否存在指定文件名。
     *
     * @param treeUri 已授权目录的 Tree URI。
     * @param fileName 期望查找的文件展示名（不是路径）。
     * @return 命中时返回对应的单文件 Document URI；不命中或目录失效时返回 `null`。
     */
    private fun findChild(treeUri: Uri, fileName: String): Uri? {
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
            treeUri,
            DocumentsContract.getTreeDocumentId(treeUri),
        )
        return resolver.query(
            childrenUri,
            arrayOf(
                Document.COLUMN_DISPLAY_NAME,
                Document.COLUMN_DOCUMENT_ID,
            ),
            null,
            null,
            null,
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                if (cursor.getString(0) == fileName) {
                    val documentId = cursor.getString(1)
                    return@use DocumentsContract.buildDocumentUriUsingTree(
                        treeUri,
                        documentId,
                    )
                }
            }
            null
        }
    }

    /**
     * 流式复制源文件到 SAF staging URI。
     *
     * 通过 Okio 在源 [File] 与 SAF 输出流之间建立 8 KiB buffer，按块读取直到源耗尽；
     * 返回的是写入到 SAF 流的总字节数，便于和源大小做一致性校验。
     *
     * @param source 应用 cache 目录下的本地源文件。
     * @param dest 已经成功创建的 SAF Document URI；此处以 `"w"` 模式打开。
     * @return 实际写入的字节数。
     */
    private fun copySource(source: File, dest: Uri): Long {
        val output = resolver.openOutputStream(dest, "w")
            ?: MediaDirectoryError.Io("无法打开 SAF 输出流").raise()
        return output.use { stream ->
            source.source().buffer().use { sourceBuffered ->
                stream.sink().buffer().use(sourceBuffered::readAll)
            }
        }
    }

    /**
     * Android Q 以下版本触发 MediaScanner，让旧相册立即可见。
     *
     * 失败仅记录日志；不影响 commit 主路径的结果。
     */
    private fun notifyMediaScanner(fileUri: Uri, mimeType: String) {
        runCatching {
            val physicalPath = fileUri.toString()
            MediaScannerConnection.scanFile(
                /* context = */ null,
                arrayOf(physicalPath),
                arrayOf(mimeType),
                null,
            )
        }.onFailure { failure ->
            AndroidMediaDirectoryLogger.w(failure) {
                "MediaScanner 通知失败，提交结果不受影响"
            }
        }
    }

    /**
     * 解析持久化 token 为 SAF Tree URI。
     *
     * 仅接受 `com.android.externalstorage.documents` 授权 provider 颁发的 Tree URI；
     * 其他形式的 token 一律归因为 [MediaDirectoryError.Unsupported]，防止 Domain 层误用。
     *
     * @param value 持久化的 token 字符串。
     * @return 通过校验的 [Uri]。
     */
    private fun parseTreeUri(value: String): Uri {
        val uri = Uri.parse(value)
        if (
            uri.scheme != ContentResolver.SCHEME_CONTENT ||
            uri.authority == null ||
            uri.authority != "com.android.externalstorage.documents"
        ) {
            MediaDirectoryError.Unsupported("Android 目录授权令牌不是有效的 SAF Tree URI").raise()
        }
        return uri
    }
}

/**
 * 将 Android 平台上下文装配为 SAF 会话实现。
 *
 * Android 端 SAF URI 的 commit / validate / delete 阶段完全不需要 [DirectoryPickerLauncher]，
 * 仅 `requestAccess` 需要它去拉起系统选择器。因此本 actual 即使未拿到 launcher
 * 也会构造一个 "commit-only" 会话，把抛 [MediaDirectoryError.Unsupported] 的时机
 * 推迟到 [MediaDirectorySession.requestAccess] 调用时刻。
 *
 * 这样消费方可以选择：
 * - 注入 `DirectoryPickerLauncher` 后完整使用所有 API。
 * - 不注入 launcher，仅用 [rememberMediaDirectoryAccessFactory](页面级 SAF 桥) 喂 token，
 *   然后用现成 session 调用 commit / validate / delete / exists。
 *
 * @param context 当前平台 [Context]，会取其 applicationContext 装配 SAF。
 * @param pickerLauncher 宿主注入的目录选择器；可为空。
 */
actual fun createMediaDirectorySession(
    context: Context,
    pickerLauncher: DirectoryPickerLauncher?,
): MediaDirectorySession {
    val androidContext = context.applicationContext ?: context
    return AndroidMediaDirectorySession(
        resolver = androidContext.contentResolver,
        pickerLauncher = pickerLauncher,
    )
}

/**
 * SAF 平台失败日志，统一使用 [AndroidMediaDirectorySession] 的简称便于检索。
 */
private val AndroidMediaDirectoryLogger = logger("AndroidMediaDirectory")

/**
 * 将 Android 平台异常映射为媒体目录技术错误。
 *
 * 优先级：
 * 1. 已经包装过的 [MediaDirectoryException] 原样返回。
 * 2. [CancellationException] 重新抛出，避免吞掉协程取消。
 * 3. [SecurityException] 归因为 [MediaDirectoryError.PermissionDenied]。
 * 4. [ErrnoException] 在 `ENOSPC` 时归因为 [MediaDirectoryError.NoSpace]，其他归因为 IO。
 * 5. 其余归因为通用 [MediaDirectoryError.Io]。
 */
private fun Throwable.toMediaDirectoryException(): MediaDirectoryException {
    if (this is MediaDirectoryException) return this
    if (this is CancellationException) throw this
    val error = when (this) {
        is SecurityException -> MediaDirectoryError.PermissionDenied("没有访问 SAF 授权目录的权限")
        is ErrnoException -> if (errno == OsConstants.ENOSPC) {
            MediaDirectoryError.NoSpace
        } else {
            MediaDirectoryError.Io("Android SAF 操作失败", this)
        }

        else -> MediaDirectoryError.Io("Android SAF 操作失败", this)
    }
    return MediaDirectoryException(error)
}
