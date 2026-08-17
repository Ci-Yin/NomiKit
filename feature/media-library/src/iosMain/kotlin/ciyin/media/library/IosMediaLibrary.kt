package ciyin.media.library

import ciyin.platform.Context
import ciyin.platform.logger
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.Photos.PHAsset
import platform.Photos.PHAssetChangeRequest
import platform.Photos.PHAssetCreationRequest
import platform.Photos.PHAssetResourceCreationOptions
import platform.Photos.PHAssetResourceType
import platform.Photos.PHAssetResourceTypePhoto
import platform.Photos.PHAssetResourceTypeVideo
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHAuthorizationStatusLimited
import platform.Photos.PHPhotoLibrary

/** 基于 iOS Photos 的系统媒体库实现。 */
internal class IosMediaLibrary : MediaLibrary {
    /** 将图片或视频事务性提交到 Photos。 */
    override suspend fun publish(request: MediaPublishRequest): PublishedMedia {
        val validated = request.validate()
        if (validated.relativeDirectorySegments.isNotEmpty()) {
            MediaLibraryError.Unsupported("iOS Photos 不支持相对目录").raise()
        }
        val resourceType = request.collection.toPhotoResourceType()
        ensurePhotosAccess()
        val fileUrl = NSURL.fileURLWithPath(request.source.path)

        return suspendCancellableCoroutine { continuation ->
            var localIdentifier: String? = null
            continuation.invokeOnCancellation {
                val identifier = localIdentifier ?: return@invokeOnCancellation
                deleteCreatedAsset(identifier) { cleanupFailure ->
                    cleanupFailure?.let { failure ->
                        IosMediaLibraryLogger.e(failure) {
                            "协程取消后无法清理由 Photos 创建的媒体资产"
                        }
                    }
                }
            }
            PHPhotoLibrary.sharedPhotoLibrary().performChanges({
                val creation = PHAssetCreationRequest.creationRequestForAsset()
                val options = PHAssetResourceCreationOptions().apply {
                    originalFilename = request.displayName
                }
                creation.addResourceWithType(
                    type = resourceType,
                    fileURL = fileUrl,
                    options = options,
                )
                localIdentifier = creation.placeholderForCreatedAsset?.localIdentifier
            }) { success, error ->
                val identifier = localIdentifier
                when {
                    !success || identifier == null -> if (continuation.isActive) {
                        continuation.resumeWithException(error.toMediaLibraryException())
                    }
                    !continuation.isActive -> deleteCreatedAsset(identifier) { cleanupFailure ->
                        cleanupFailure?.let { failure ->
                            IosMediaLibraryLogger.e(failure) {
                                "协程取消后无法清理由 Photos 创建的媒体资产"
                            }
                        }
                    }
                    else -> {
                        val sourceSizeAfterPublish = runCatching { request.source.strictSize() }
                        val sourceSize = sourceSizeAfterPublish.getOrNull()
                        if (sourceSize == null || sourceSize != validated.sourceSize) {
                            val publishFailure = sourceSizeAfterPublish.exceptionOrNull()
                                ?: MediaLibraryException(
                                    MediaLibraryError.Io("源文件在发布期间发生变化"),
                                )
                            deleteCreatedAsset(identifier) { cleanupFailure ->
                                if (!continuation.isActive) {
                                    cleanupFailure?.let { failure ->
                                        IosMediaLibraryLogger.e(failure) {
                                            "协程取消后无法清理校验失败的 Photos 媒体资产"
                                        }
                                    }
                                } else if (cleanupFailure == null) {
                                    continuation.resumeWithException(publishFailure)
                                } else {
                                    cleanupFailure.addSuppressed(publishFailure)
                                    continuation.resumeWithException(
                                        MediaLibraryException(
                                            MediaLibraryError.Io(
                                                "发布失败且无法清理 Photos 媒体资产",
                                                cleanupFailure,
                                            ),
                                        ),
                                    )
                                }
                            }
                        } else {
                            continuation.resume(
                                PublishedMedia(
                                    platformId = identifier,
                                    uri = null,
                                    displayName = request.displayName,
                                    mimeType = request.mimeType,
                                    size = sourceSize,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }

    /** 使用 Photos localIdentifier 删除资产，资产不存在时保持幂等。 */
    override suspend fun delete(media: PublishedMedia) {
        ensurePhotosAccess()
        val assets = PHAsset.fetchAssetsWithLocalIdentifiers(
            identifiers = listOf(media.platformId),
            options = null,
        )
        if (assets.count.toInt() == 0) return

        suspendCancellableCoroutine { continuation ->
            PHPhotoLibrary.sharedPhotoLibrary().performChanges({
                PHAssetChangeRequest.deleteAssets(assets)
            }) { success, error ->
                if (!continuation.isActive) return@performChanges
                if (success) {
                    continuation.resume(Unit)
                } else {
                    continuation.resumeWithException(error.toMediaLibraryException())
                }
            }
        }
    }

    /** 使用 Photos localIdentifier 查询资产是否仍存在。 */
    override suspend fun exists(media: PublishedMedia): Boolean {
        ensurePhotosAccess()
        return PHAsset.fetchAssetsWithLocalIdentifiers(
            identifiers = listOf(media.platformId),
            options = null,
        ).count.toInt() > 0
    }
}

/** 将 iOS 平台上下文装配为 Photos 实现。 */
@Suppress("UNUSED_PARAMETER")
actual fun createMediaLibrary(context: Context): MediaLibrary = IosMediaLibrary()

/** 将公共媒体分类映射为 Photos 资源类型。 */
private fun MediaCollection.toPhotoResourceType(): PHAssetResourceType = when (this) {
    MediaCollection.Images -> PHAssetResourceTypePhoto
    MediaCollection.Videos -> PHAssetResourceTypeVideo
    MediaCollection.Audio -> MediaLibraryError.Unsupported("iOS Photos 不支持音频 Collection").raise()
    MediaCollection.Downloads -> MediaLibraryError.Unsupported("iOS Photos 不支持下载 Collection").raise()
}

/** 确认宿主已经授予 Photos 读写权限。 */
private fun ensurePhotosAccess() {
    when (PHPhotoLibrary.authorizationStatus()) {
        PHAuthorizationStatusAuthorized,
        PHAuthorizationStatusLimited,
        -> Unit
        else -> MediaLibraryError.PermissionDenied("没有 iOS Photos 读写权限").raise()
    }
}

/** Photos 清理失败日志。 */
private val IosMediaLibraryLogger = logger("IosMediaLibrary")

/** 删除取消或校验失败后已经由 Photos 创建的资产并报告结果。 */
private fun deleteCreatedAsset(
    localIdentifier: String,
    completion: (MediaLibraryException?) -> Unit,
) {
    val assets = PHAsset.fetchAssetsWithLocalIdentifiers(
        identifiers = listOf(localIdentifier),
        options = null,
    )
    if (assets.count.toInt() == 0) {
        completion(null)
        return
    }
    PHPhotoLibrary.sharedPhotoLibrary().performChanges({
        PHAssetChangeRequest.deleteAssets(assets)
    }) { success, error ->
        completion(if (success) null else error.toMediaLibraryException())
    }
}

/** 将 Photos 回调错误映射为媒体库技术异常。 */
private fun NSError?.toMediaLibraryException(): MediaLibraryException {
    val description = this?.localizedDescription
    val error = if (description.orEmpty().lowercase().contains("space")) {
        MediaLibraryError.NoSpace("iOS Photos 空间不足")
    } else {
        MediaLibraryError.Io(description ?: "iOS Photos 操作失败")
    }
    return MediaLibraryException(error)
}
