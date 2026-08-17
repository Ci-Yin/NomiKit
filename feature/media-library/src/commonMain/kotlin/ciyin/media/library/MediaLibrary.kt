package ciyin.media.library

import ciyin.io.File

/** 系统媒体库发布请求。 */
data class MediaPublishRequest(
    /** 待读取的本地源文件。 */
    val source: File,
    /** 发布后的显示名称。 */
    val displayName: String,
    /** 发布媒体的 MIME 类型。 */
    val mimeType: String,
    /** 目标系统媒体库分类。 */
    val collection: MediaCollection,
    /** 平台支持时使用的相对目录。 */
    val relativeDirectory: String? = null,
)

/** 可由调用方持久化的平台媒体引用。 */
data class PublishedMedia(
    /** 平台删除和存在性检查所需的稳定标识。 */
    val platformId: String,
    /** 平台提供稳定 URI 时返回，否则为 `null`。 */
    val uri: String?,
    /** 发布后的显示名称。 */
    val displayName: String,
    /** 发布时使用的 MIME 类型。 */
    val mimeType: String,
    /** 发布完成时记录的字节大小。 */
    val size: Long,
)

/** 跨平台系统媒体库。 */
interface MediaLibrary {
    /** 将本地源文件提交到平台系统媒体库。 */
    suspend fun publish(request: MediaPublishRequest): PublishedMedia

    /** 删除指定平台媒体；媒体已不存在时按成功处理。 */
    suspend fun delete(media: PublishedMedia)

    /** 检查媒体引用当前是否仍存在。 */
    suspend fun exists(media: PublishedMedia): Boolean
}
