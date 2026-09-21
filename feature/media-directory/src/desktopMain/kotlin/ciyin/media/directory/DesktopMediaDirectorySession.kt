package ciyin.media.directory

import ciyin.io.AccessDeniedException
import ciyin.io.File
import ciyin.io.FileAlreadyExistsException
import ciyin.io.NoSuchFileException
import ciyin.io.copyTo
import ciyin.platform.Context
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okio.IOException
import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.nio.file.Paths
import java.util.UUID
import javax.swing.JFileChooser
import kotlin.coroutines.resume
import java.nio.file.AccessDeniedException as NioAccessDeniedException
import java.nio.file.FileAlreadyExistsException as NioFileAlreadyExistsException

/**
 * Desktop 用户目录实现，基于 `java.nio.file` + `JFileChooser`。
 *
 * `accessToken` 即归一化的目录绝对路径；不需要额外持久化层。
 */
internal class DesktopMediaDirectorySession(
    /** 宿主注入的目录选择器；为 `null` 时使用内置 [JvmFileDialogDirectoryPicker]。 */
    private val pickerLauncher: DirectoryPickerLauncher?,
) : MediaDirectorySession {

    /** 拉起目录选择器；用户取消时抛 [MediaDirectoryException] 携带 [MediaDirectoryError.Cancelled]。 */
    override suspend fun requestAccess(): MediaDirectoryAccess = withContext(Dispatchers.Main) {
        val picker = pickerLauncher ?: JvmJFileChooserDirectoryPicker
        val picked = picker.pick()
        if (picked.isNullOrBlank()) {
            MediaDirectoryError.Cancelled.raise()
        }
        val directory = try {
            Paths.get(picked).toAbsolutePath().normalize()
        } catch (exception: InvalidPathException) {
            MediaDirectoryError.Unsupported("选中的目录路径无效").raise()
        }
        if (!Files.isDirectory(directory)) {
            MediaDirectoryError.Unsupported("选中的目标不是一个目录").raise()
        }
        MediaDirectoryAccess(
            accessToken = MediaDirectoryAccessToken(directory.toString()),
            displayName = directory.fileName?.toString().orEmpty(),
            canonicalPath = directory.toString(),
        )
    }

    /** 校验目录路径仍存在且可写。 */
    override suspend fun validate(accessToken: MediaDirectoryAccessToken): Unit =
        withContext(Dispatchers.IO) {
            val path = parseDirectoryPath(accessToken.value)
            if (!Files.isDirectory(path)) {
                MediaDirectoryError.AccessRevoked("授权目录已不存在").raise()
            }
            if (!Files.isWritable(path)) {
                MediaDirectoryError.AccessRevoked("授权目录已不可写").raise()
            }
        }

    /** 把本地源文件提交到已授权目录。 */
    override suspend fun commit(
        accessToken: MediaDirectoryAccessToken,
        source: File,
        fileName: String,
        mimeType: String,
    ): MediaDirectoryEntry = withContext(Dispatchers.IO) {
        val validated = validateCommitRequest(fileName, mimeType, source)
        val directory = parseDirectoryPath(accessToken.value)
        if (!Files.isDirectory(directory)) {
            MediaDirectoryError.AccessRevoked("授权目录已不存在").raise()
        }
        val target = directory.resolve(fileName)
        if (Files.exists(target)) {
            MediaDirectoryError.AlreadyExists(fileName).raise()
        }
        val staging = directory.resolve(".$fileName.${UUID.randomUUID()}.nomikit-part")

        try {
            source.copyTo(File(staging.toString()), overwrite = false)
            val copiedSize = File(staging.toString()).strictSize()
            val sourceSizeAfterCopy = source.strictSize()
            if (copiedSize != validated.sourceSize || sourceSizeAfterCopy != validated.sourceSize) {
                MediaDirectoryError.Io("源文件在提交期间发生变化").raise()
            }
            Files.move(staging, target)
            MediaDirectoryEntry(
                accessToken = accessToken,
                fileName = fileName,
                location = target.toString(),
                mimeType = mimeType,
                size = copiedSize,
            )
        } catch (exception: CancellationException) {
            runCatching { Files.deleteIfExists(staging) }
                .onFailure { exception.addSuppressed(it) }
            throw exception
        } catch (exception: MediaDirectoryException) {
            runCatching { Files.deleteIfExists(staging) }
            throw exception
        } catch (exception: Throwable) {
            runCatching { Files.deleteIfExists(staging) }
            throw exception.toMediaDirectoryException()
        }
    }

    /** 删除授权目录中的应用管理文件；不存在时按成功处理。 */
    override suspend fun delete(
        accessToken: MediaDirectoryAccessToken,
        location: String,
    ): Unit = withContext(Dispatchers.IO) {
        parseDirectoryPath(accessToken.value)
        val target = try {
            Paths.get(location).toAbsolutePath().normalize()
        } catch (exception: InvalidPathException) {
            MediaDirectoryError.Unsupported("Desktop 目录条目位置不是有效路径").raise()
        }
        try {
            Files.deleteIfExists(target)
        } catch (exception: Throwable) {
            throw exception.toMediaDirectoryException()
        }
    }

    /** 检查应用管理文件是否仍存在。 */
    override suspend fun exists(
        accessToken: MediaDirectoryAccessToken,
        location: String,
    ): Boolean = withContext(Dispatchers.IO) {
        parseDirectoryPath(accessToken.value)
        val target = try {
            Paths.get(location).toAbsolutePath().normalize()
        } catch (exception: InvalidPathException) {
            return@withContext false
        }
        Files.exists(target)
    }

    /** 解析 token 为规范化的目录路径。 */
    private fun parseDirectoryPath(value: String): Path = try {
        Paths.get(value).toAbsolutePath().normalize()
    } catch (exception: InvalidPathException) {
        MediaDirectoryError.Unsupported("Desktop 目录授权令牌不是有效路径").raise()
    }
}

/** 默认的 Swing JFileChooser 目录选择器实现。 */
private object JvmJFileChooserDirectoryPicker : DirectoryPickerLauncher {
    override suspend fun pick(): String? = suspendCancellableCoroutine { continuation ->
        try {
            val chooser = JFileChooser().apply {
                fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                dialogTitle = "选择媒体保存目录"
                isMultiSelectionEnabled = false
            }
            // JFileChooser.showOpenDialog 必须在 EDT 上调用；当前协程上下文已是 Dispatchers.Main。
            val result = chooser.showOpenDialog(null)
            if (!continuation.isActive) return@suspendCancellableCoroutine
            continuation.resume(
                if (result == JFileChooser.APPROVE_OPTION) {
                    chooser.selectedFile?.absolutePath
                } else {
                    null
                },
            )
        } catch (exception: Throwable) {
            if (continuation.isActive) {
                continuation.resume(null)
            }
        }
    }
}

/** 将 Desktop 平台上下文装配为文件系统会话。 */
@Suppress("UNUSED_PARAMETER")
actual fun createMediaDirectorySession(
    context: Context,
    pickerLauncher: DirectoryPickerLauncher?,
): MediaDirectorySession = DesktopMediaDirectorySession(pickerLauncher)

/** 将 Desktop 文件系统异常映射为媒体目录技术错误。 */
private fun Throwable.toMediaDirectoryException(): MediaDirectoryException {
    if (this is MediaDirectoryException) return this
    if (this is CancellationException) throw this
    val error = when (this) {
        is FileAlreadyExistsException,
        is NioFileAlreadyExistsException,
            -> MediaDirectoryError.Io("Desktop 目录写入失败", this)

        is AccessDeniedException,
        is NioAccessDeniedException,
        is SecurityException,
            -> MediaDirectoryError.PermissionDenied("没有读写用户目录的权限")

        is NoSuchFileException -> MediaDirectoryError.NotFound(this.file.toString())
        else -> if (isNoSpaceFailure()) {
            MediaDirectoryError.NoSpace
        } else {
            MediaDirectoryError.Io("Desktop 目录操作失败", this)
        }
    }
    return MediaDirectoryException(error)
}

/** 判断底层错误是否表示目标卷空间不足。 */
private fun Throwable.isNoSpaceFailure(): Boolean {
    if (this !is IOException && this !is java.io.IOException) return false
    val description = message.orEmpty().lowercase()
    return description.contains("no space") ||
            description.contains("not enough space") ||
            description.contains("disk full") ||
            description.contains("空间不足")
}
