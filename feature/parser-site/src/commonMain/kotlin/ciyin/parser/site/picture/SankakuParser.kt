package ciyin.parser.site.picture

import ciyin.parser.core.picture.PictureParser
import ciyin.parser.core.picture.PictureParserType.Detail
import ciyin.parser.core.picture.PictureParserType.Feed
import ciyin.parser.core.picture.model.Picture
import ciyin.parser.core.picture.model.PictureRequest
import ciyin.parser.model.Rating
import ciyin.parser.model.Tag
import ciyin.parser.site.PictureSiteId
import ciyin.parser.site.util.NumberAsStringSerializer
import ciyin.parser.util.PictureParserScope
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Sankaku Channel 图片解析器。
 *
 * 列表使用 Sankaku API 的 keyset 分页；下一页必须通过响应中的 `meta.next` 继续请求，
 * 不把普通页码参数伪装成 Sankaku 分页。
 *
 * @param apiBaseUrl 可选的 API 基础地址，供镜像端点和离线契约测试使用
 */
class SankakuParser(
    apiBaseUrl: String? = null,
) : PictureParser(apiBaseUrl) {

    /** Sankaku 使用 API 返回的 opaque keyset 游标继续分页。 */
    override val supportsCursorPagination: Boolean get() = true

    /** 配置 Sankaku API 请求与响应映射。 */
    override fun PictureParserScope.setup() {
        id = PictureSiteId.Sankaku
        baseUrl = DefaultApiBaseUrl

        onItemRevise {
            copy(
                site = configure.id.site,
                thumbnailUrl = thumbnailUrl.ifBlank { sampleUrl },
                sampleUrl = sampleUrl.ifBlank { thumbnailUrl },
            )
        }

        on(Feed) {
            request { request ->
                json {
                    url(
                        path = "/v2/posts/keyset",
                        parameters = request.feedParameters(),
                    )
                    headers(
                        "Accept" to ApiAccept,
                        "Origin" to WebOrigin,
                    )
                }
            }

            response { result ->
                val payload = bodyForJson<SankakuKeysetResponse>()
                val nextCursor = payload.meta.next?.takeIf { it.isNotBlank() }
                result.copy(
                    contents = payload.data.map { it.toPicture() },
                    pagination = result.pagination.copy(
                        totalPages = null,
                        hasNext = nextCursor != null,
                        nextCursor = nextCursor,
                    ),
                )
            }
        }

        on(Detail) {
            request { request ->
                require(request.id.isNotBlank()) { "Sankaku 详情请求缺少帖子 ID" }
                json {
                    url(
                        path = "/v2/posts",
                        parameters = mapOf(
                            "id" to listOf(request.id),
                        ),
                    )
                    headers(
                        "Accept" to ApiAccept,
                        "Origin" to WebOrigin,
                    )
                }
            }

            response { result ->
                val posts = bodyForJson<List<SankakuPost>>()
                require(posts.size == 1) {
                    "Sankaku 详情未返回唯一帖子：${posts.size}"
                }
                result.copy(contents = listOf(posts.single().toPicture()))
            }
        }
    }

    /** 构建 Sankaku keyset 列表查询参数。 */
    private fun PictureRequest.feedParameters(): Map<String, List<Any>> = buildMap {
        put("limit", listOf(PageLimit))
        cursor?.takeIf { it.isNotBlank() }?.let { put("next", listOf(it)) }
        if (tags.isNotEmpty()) {
            put("tags", listOf(tags.joinToString(" ")))
        }
    }

    /** 将 Sankaku 帖子 DTO 转成通用图片模型。 */
    private fun SankakuPost.toPicture(): Picture {
        require(id.isNotBlank()) { "Sankaku 帖子缺少 ID" }
        return Picture(
            id = id,
            originalUrl = fileUrl,
            sampleUrl = sampleUrl,
            thumbnailUrl = previewUrl,
            postUrl = "$WebBaseUrl/posts/$id",
            sourceUrl = source,
            fileExt = fileExt,
            fileSize = fileSize,
            md5 = md5,
            width = width,
            height = height,
            rating = rating.toRating(),
            tags = resolvedTags(),
            createdAt = createdAt.secondsLong,
        )
    }

    /** 选择 API 提供的标准标签名称，并保留标签列表作为缺省来源。 */
    private fun SankakuPost.resolvedTags(): List<Tag> {
        val names = tagNames.ifEmpty {
            tags.mapNotNull { tag ->
                tag.nameEn.ifBlank { tag.tagName.ifBlank { tag.name } }
                    .takeIf { it.isNotBlank() }
            }
        }
        return names.map { name -> Tag(tag = name) }
    }

    /** 将 Sankaku 分级值映射为通用分级。 */
    private fun String.toRating(): Rating = when (lowercase()) {
        "s" -> Rating.Safe
        "q" -> Rating.Sensitive
        "e" -> Rating.Explicit
        else -> error("Sankaku 返回未知图片分级：$this")
    }

    /** Sankaku API keyset 响应。 */
    @Serializable
    private data class SankakuKeysetResponse(
        val meta: SankakuMeta = SankakuMeta(),
        val data: List<SankakuPost> = emptyList(),
    )

    /** Sankaku keyset 分页元数据。 */
    @Serializable
    private data class SankakuMeta(
        val next: String? = null,
        val prev: String? = null,
    )

    /** Sankaku 帖子响应 DTO。 */
    @Serializable
    private data class SankakuPost(
        @Serializable(with = NumberAsStringSerializer::class)
        val id: String = "",
        val rating: String = "",
        @SerialName("sample_url")
        val sampleUrl: String = "",
        @SerialName("preview_url")
        val previewUrl: String = "",
        @SerialName("file_url")
        val fileUrl: String = "",
        @SerialName("file_ext")
        val fileExt: String = "",
        @SerialName("file_size")
        val fileSize: Long = 0L,
        val width: Int = 0,
        val height: Int = 0,
        val md5: String = "",
        val source: String = "",
        @SerialName("tag_names")
        val tagNames: List<String> = emptyList(),
        val tags: List<SankakuTag> = emptyList(),
        @SerialName("created_at")
        val createdAt: SankakuTime = SankakuTime(),
    )

    /** Sankaku 标签响应 DTO。 */
    @Serializable
    private data class SankakuTag(
        @SerialName("name_en")
        val nameEn: String = "",
        @SerialName("tagName")
        val tagName: String = "",
        val name: String = "",
    )

    /** Sankaku 时间响应 DTO。 */
    @Serializable
    private data class SankakuTime(
        @SerialName("s")
        @Serializable(with = NumberAsStringSerializer::class)
        val seconds: String = "0",
    ) {
        /** 将 API 时间戳转换为通用模型使用的 Long。 */
        val secondsLong: Long get() = seconds.toLongOrNull() ?: 0L
    }

    private companion object {
        const val DefaultApiBaseUrl = "https://sankakuapi.com"
        const val WebBaseUrl = "https://sankaku.app"
        const val WebOrigin = "https://sankaku.app"
        const val ApiAccept = "application/vnd.sankaku.api+json;v=2"
        const val PageLimit = 100
    }
}
