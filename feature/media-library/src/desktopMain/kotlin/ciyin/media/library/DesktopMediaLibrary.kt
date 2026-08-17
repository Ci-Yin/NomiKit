package ciyin.media.library

import ciyin.io.AccessDeniedException
import ciyin.io.File
import ciyin.io.FileAlreadyExistsException
import ciyin.io.NoSuchFileException
import ciyin.io.SystemFileSystem
import ciyin.io.copyTo
import ciyin.io.resolve
import java.nio.file.AccessDeniedException as NioAccessDeniedException
import java.nio.file.FileAlreadyExistsException as NioFileAlreadyExistsException
import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.nio.file.Paths
import java.util.UUID
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import okio.FileMetadata
import okio.IOException

/** Desktop 用户标准目录媒体库实现。 */
internal class DesktopMediaLibrary(
    /** 用户标准媒体目录解析器。 */
    private val directoryResolver: DesktopMediaDirectoryResolver,
    /** 文件操作使用的协程上下文。 */
    private val ioContext: CoroutineContext = Dispatchers.IO,
    /** 测试提交与取消竞态时使用的提交后回调。 */
    private val onCommitted: () -> Unit = {},
) : MediaLibrary {
    /** 复制源文件并以原子移动提交到用户标准目录。 */
    override suspend fun publish(request: MediaPublishRequest): PublishedMedia {
        var committedTarget: File? = null
        return try {
            withContext(ioContext) {
                val validated = request.validate()
                val targetDirectory = validated.relativeDirectorySegments.fold(
                    initial = directoryResolver.resolve(request.collection),
                ) { directory, segment ->
                    directory.resolve(segment)
                }
                ensureDirectory(targetDirectory)
                val target = targetDirectory.resolve(request.displayName)
                if (target.exists()) {
                    MediaLibraryError.AlreadyExists("目标目录中已存在同名媒体").raise()
                }
                val temporary = targetDirectory.resolve(
                    ".${request.displayName}.${UUID.randomUUID()}.nomikit-part",
                )

                try {
                    request.source.copyTo(temporary, overwrite = false)
                    val copiedSize = temporary.strictSize()
                    val sourceSizeAfterCopy = request.source.strictSize()
                    if (copiedSize != validated.sourceSize || sourceSizeAfterCopy != validated.sourceSize) {
                        MediaLibraryError.Io("源文件在发布期间发生变化").raise()
                    }
                    SystemFileSystem.atomicMove(temporary.toPath(), target.toPath())
                    committedTarget = target
                    onCommitted()
                    PublishedMedia(
                        platformId = target.normalizedAbsolutePath(),
                        uri = target.toNioPath().toUri().toString(),
                        displayName = request.displayName,
                        mimeType = request.mimeType,
                        size = copiedSize,
                    )
                } catch (exception: CancellationException) {
                    cleanupFile(committedTarget ?: temporary)?.let(exception::addSuppressed)
                    throw exception
                } catch (exception: Throwable) {
                    val cleanupFailure = cleanupFile(committedTarget ?: temporary)
                    if (cleanupFailure != null) {
                        cleanupFailure.addSuppressed(exception)
                        throw MediaLibraryException(
                            MediaLibraryError.Io("发布失败且无法清理临时文件", cleanupFailure),
                        )
                    }
                    throw exception.toMediaLibraryException()
                }
            }
        } catch (exception: CancellationException) {
            val target = committedTarget
            if (target != null) {
                val cleanupFailure = withContext(NonCancellable + Dispatchers.IO) {
                    cleanupFile(target)
                }
                cleanupFailure?.let(exception::addSuppressed)
            }
            throw exception
        } catch (exception: Throwable) {
            throw exception.toMediaLibraryException()
        }
    }

    /** 删除媒体引用指向的用户标准目录文件。 */
    override suspend fun delete(media: PublishedMedia): Unit = withContext(ioContext) {
        val target = resolveReferencedMedia(media.platformId)
        val metadata = target.strictMetadataOrNull() ?: return@withContext
        if (!metadata.isRegularFile) {
            MediaLibraryError.Unsupported("Desktop 平台媒体标识没有指向普通文件").raise()
        }
        try {
            SystemFileSystem.delete(target.toPath(), mustExist = false)
        } catch (exception: Throwable) {
            throw exception.toMediaLibraryException()
        }
    }

    /** 检查媒体引用是否仍指向用户标准目录中的普通文件。 */
    override suspend fun exists(media: PublishedMedia): Boolean = withContext(ioContext) {
        val target = resolveReferencedMedia(media.platformId)
        target.strictMetadataOrNull()?.isRegularFile == true
    }

    /** 创建目标目录并确认结果为目录。 */
    private fun ensureDirectory(directory: File) {
        try {
            if (!directory.exists()) SystemFileSystem.createDirectories(directory.toPath())
            if (!directory.isDirectory) {
                MediaLibraryError.Io("目标媒体目录不是文件夹").raise()
            }
        } catch (exception: Throwable) {
            throw exception.toMediaLibraryException()
        }
    }

    /** 将持久化平台标识还原成受支持媒体目录内的安全路径。 */
    private fun resolveReferencedMedia(platformId: String): File = try {
        val path = try {
            Paths.get(platformId).toAbsolutePath().normalize()
        } catch (exception: InvalidPathException) {
            MediaLibraryError.Unsupported("Desktop 平台媒体标识不是有效路径").raise()
        }
        val supportedRoots = MediaCollection.entries.map { collection ->
            directoryResolver.resolve(collection).toNioPath().toAbsolutePath().normalize()
        }
        if (supportedRoots.none { root -> path != root && path.startsWith(root) }) {
            MediaLibraryError.PermissionDenied("媒体引用不在受支持的用户媒体目录中").raise()
        }
        File(path.toString())
    } catch (exception: Throwable) {
        throw exception.toMediaLibraryException()
    }
}

/** 将 Desktop 平台上下文装配为系统媒体库。 */
@Suppress("UNUSED_PARAMETER")
actual fun createMediaLibrary(context: ciyin.platform.Context): MediaLibrary =
    DesktopMediaLibrary(DefaultDesktopMediaDirectoryResolver)

/** 返回文件对应的归一化绝对路径。 */
private fun File.normalizedAbsolutePath(): String = toNioPath().toAbsolutePath().normalize().toString()

/** 将项目文件抽象转换到 Desktop 原生路径边界。 */
private fun File.toNioPath(): Path = Paths.get(path)

/** 严格读取文件元数据，仅在目标确实不存在时返回 `null`。 */
private fun File.strictMetadataOrNull(): FileMetadata? = try {
    SystemFileSystem.metadataOrNull(toPath())
} catch (exception: Throwable) {
    throw exception.toMediaLibraryException()
}

/** 清理发布过程产生的文件，并在失败时返回底层清理异常。 */
private fun cleanupFile(temporary: File): Throwable? {
    return try {
        SystemFileSystem.delete(temporary.toPath(), mustExist = false)
        null
    } catch (cleanupFailure: Throwable) {
        cleanupFailure
    }
}

/** 将 Desktop 文件系统异常映射为媒体库技术错误。 */
private fun Throwable.toMediaLibraryException(): MediaLibraryException {
    if (this is MediaLibraryException) return this
    if (this is CancellationException) throw this
    val error = when (this) {
        is FileAlreadyExistsException,
        is NioFileAlreadyExistsException,
        -> MediaLibraryError.AlreadyExists("目标目录中已存在同名媒体")
        is AccessDeniedException,
        is NioAccessDeniedException,
        is SecurityException,
        -> MediaLibraryError.PermissionDenied("没有读写用户媒体目录的权限")
        is NoSuchFileException -> MediaLibraryError.NotFound("源文件不存在")
        else -> if (isNoSpaceFailure()) {
            MediaLibraryError.NoSpace("用户媒体目录所在磁盘空间不足")
        } else {
            MediaLibraryError.Io("Desktop 系统媒体库操作失败", this)
        }
    }
    return MediaLibraryException(error)
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
