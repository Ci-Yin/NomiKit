package ciyin.media.library

import android.content.ContentResolver
import android.content.ContentValues
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteFullException
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.system.ErrnoException
import android.system.OsConstants
import ciyin.io.source
import ciyin.platform.Context
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import okio.buffer
import okio.sink

/** 基于 Android MediaStore 的系统媒体库实现。 */
internal class AndroidMediaLibrary(
    /** 用于读写 MediaStore 的内容解析器。 */
    private val resolver: ContentResolver,
) : MediaLibrary {
    /** 以待处理记录写入媒体内容，并在全部完成后提交。 */
    override suspend fun publish(request: MediaPublishRequest): PublishedMedia {
        var committedUri: Uri? = null
        return try {
            withContext(Dispatchers.IO) {
                val validated = request.validate()
                val collection = request.collection.toAndroidCollection()
                val relativePath = collection.relativePath(validated.relativeDirectorySegments)
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && relativePath != null) {
                    MediaLibraryError.Unsupported("Android 10 以下不支持 MediaStore 相对目录").raise()
                }
                if (mediaExists(collection.uri, request.displayName, relativePath)) {
                    MediaLibraryError.AlreadyExists("目标 MediaStore 中已存在同名媒体").raise()
                }

                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, request.displayName)
                    put(MediaStore.MediaColumns.MIME_TYPE, request.mimeType)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        relativePath?.let { put(MediaStore.MediaColumns.RELATIVE_PATH, it) }
                        put(MediaStore.MediaColumns.IS_PENDING, 1)
                    }
                }
                val insertedUri = try {
                    resolver.insert(collection.uri, values)
                        ?: MediaLibraryError.Io("MediaStore 未返回新媒体 URI").raise()
                } catch (exception: Throwable) {
                    throw exception.toMediaLibraryException()
                }

                try {
                    val copiedSize = copySource(request, insertedUri)
                    val sourceSizeAfterCopy = request.source.strictSize()
                    if (copiedSize != validated.sourceSize || sourceSizeAfterCopy != validated.sourceSize) {
                        MediaLibraryError.Io("源文件在发布期间发生变化").raise()
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val committed = resolver.update(
                            insertedUri,
                            ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) },
                            null,
                            null,
                        )
                        if (committed != 1) {
                            MediaLibraryError.Io("MediaStore 待处理记录提交失败").raise()
                        }
                    }
                    committedUri = insertedUri
                    PublishedMedia(
                        platformId = insertedUri.toString(),
                        uri = insertedUri.toString(),
                        displayName = request.displayName,
                        mimeType = request.mimeType,
                        size = copiedSize,
                    )
                } catch (exception: CancellationException) {
                    rollback(insertedUri)?.let(exception::addSuppressed)
                    throw exception
                } catch (exception: Throwable) {
                    val cleanupFailure = rollback(insertedUri)
                    if (cleanupFailure != null) {
                        cleanupFailure.addSuppressed(exception)
                        throw MediaLibraryException(
                            MediaLibraryError.Io(
                                "发布失败且无法清理 MediaStore 待处理记录",
                                cleanupFailure,
                            ),
                        )
                    }
                    throw exception.toMediaLibraryException()
                }
            }
        } catch (exception: CancellationException) {
            val uri = committedUri
            if (uri != null) {
                val cleanupFailure = withContext(NonCancellable + Dispatchers.IO) {
                    rollback(uri)
                }
                cleanupFailure?.let(exception::addSuppressed)
            }
            throw exception
        }
    }

    /** 使用平台 URI 删除媒体，媒体已经不存在时保持幂等。 */
    override suspend fun delete(media: PublishedMedia): Unit = withContext(Dispatchers.IO) {
        val uri = media.platformId.toMediaStoreUri()
        try {
            resolver.delete(uri, null, null)
        } catch (exception: Throwable) {
            throw exception.toMediaLibraryException()
        }
    }

    /** 使用平台 URI 查询媒体是否仍存在。 */
    override suspend fun exists(media: PublishedMedia): Boolean = withContext(Dispatchers.IO) {
        val uri = media.platformId.toMediaStoreUri()
        try {
            resolver.query(
                uri,
                arrayOf(MediaStore.MediaColumns._ID),
                null,
                null,
                null,
            )?.use { cursor ->
                cursor.moveToFirst()
            } ?: MediaLibraryError.Io("MediaStore 查询未返回结果集").raise()
        } catch (exception: Throwable) {
            throw exception.toMediaLibraryException()
        }
    }

    /** 将源文件流式写入新建的 MediaStore URI。 */
    private fun copySource(request: MediaPublishRequest, uri: Uri): Long {
        val output = resolver.openOutputStream(uri, "w")
            ?: MediaLibraryError.Io("无法打开 MediaStore 输出流").raise()
        return output.use { stream ->
            request.source.source().buffer().use { source ->
                stream.sink().buffer().use(source::readAll)
            }
        }
    }

    /** 查询指定媒体集合和目录中是否已经存在同名条目。 */
    private fun mediaExists(collectionUri: Uri, displayName: String, relativePath: String?): Boolean {
        val selectionParts = mutableListOf("${MediaStore.MediaColumns.DISPLAY_NAME} = ?")
        val selectionArgs = mutableListOf(displayName)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && relativePath != null) {
            selectionParts += "${MediaStore.MediaColumns.RELATIVE_PATH} = ?"
            selectionArgs += relativePath
        }
        return try {
            resolver.query(
                collectionUri,
                arrayOf(MediaStore.MediaColumns._ID),
                selectionParts.joinToString(" AND "),
                selectionArgs.toTypedArray(),
                null,
            )?.use { cursor -> cursor.moveToFirst() }
                ?: MediaLibraryError.Io("MediaStore 重名检查未返回结果集").raise()
        } catch (exception: Throwable) {
            throw exception.toMediaLibraryException()
        }
    }

    /** 删除本次发布创建的记录，失败时返回底层清理异常。 */
    private fun rollback(uri: Uri): Throwable? {
        return try {
            resolver.delete(uri, null, null)
            null
        } catch (cleanupFailure: Throwable) {
            cleanupFailure
        }
    }
}

/** 将 Android 平台上下文装配为 MediaStore 实现。 */
actual fun createMediaLibrary(context: Context): MediaLibrary = AndroidMediaLibrary(
    resolver = (context.applicationContext ?: context).contentResolver,
)

/** Android MediaStore 目标集合及其公共目录名称。 */
private data class AndroidMediaCollection(
    /** 对应 MediaStore 集合 URI。 */
    val uri: Uri,
    /** Android 10 及以上使用的公共目录名称。 */
    val publicDirectory: String,
) {
    /** 构造包含末尾分隔符的 MediaStore 相对目录。 */
    fun relativePath(segments: List<String>): String? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return if (segments.isEmpty()) null else ""
        return (listOf(publicDirectory) + segments).joinToString(separator = "/", postfix = "/")
    }
}

/** 将公共媒体分类映射到当前系统支持的 MediaStore 集合。 */
private fun MediaCollection.toAndroidCollection(): AndroidMediaCollection = when (this) {
    MediaCollection.Images -> AndroidMediaCollection(
        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        publicDirectory = Environment.DIRECTORY_PICTURES,
    )
    MediaCollection.Videos -> AndroidMediaCollection(
        uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        publicDirectory = Environment.DIRECTORY_MOVIES,
    )
    MediaCollection.Audio -> AndroidMediaCollection(
        uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        publicDirectory = Environment.DIRECTORY_MUSIC,
    )
    MediaCollection.Downloads -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        AndroidMediaCollection(
            uri = MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            publicDirectory = Environment.DIRECTORY_DOWNLOADS,
        )
    } else {
        MediaLibraryError.Unsupported("Android 10 以下没有 Downloads MediaStore 集合").raise()
    }
}

/** 将持久化平台标识解析为 ContentResolver URI。 */
private fun String.toMediaStoreUri(): Uri {
    val uri = Uri.parse(this)
    val mediaId = uri.lastPathSegment?.toLongOrNull()
    if (
        uri.scheme != ContentResolver.SCHEME_CONTENT ||
        uri.authority != MediaStore.AUTHORITY ||
        mediaId == null ||
        mediaId < 0L
    ) {
        MediaLibraryError.Unsupported("Android 平台媒体标识不是有效的 Content URI").raise()
    }
    return uri
}

/** 将 Android 平台异常映射为媒体库技术错误。 */
private fun Throwable.toMediaLibraryException(): MediaLibraryException {
    if (this is MediaLibraryException) return this
    if (this is CancellationException) throw this
    val error = when (this) {
        is SecurityException -> MediaLibraryError.PermissionDenied("没有访问 Android 系统媒体库的权限")
        is SQLiteConstraintException -> MediaLibraryError.AlreadyExists("MediaStore 拒绝创建重复媒体")
        is SQLiteFullException -> MediaLibraryError.NoSpace("Android 系统媒体库空间不足")
        is ErrnoException -> if (errno == OsConstants.ENOSPC) {
            MediaLibraryError.NoSpace("Android 系统媒体库空间不足")
        } else {
            MediaLibraryError.Io("Android MediaStore 操作失败", this)
        }
        else -> MediaLibraryError.Io("Android MediaStore 操作失败", this)
    }
    return MediaLibraryException(error)
}
