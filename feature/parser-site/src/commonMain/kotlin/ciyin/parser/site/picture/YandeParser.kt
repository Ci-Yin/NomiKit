package ciyin.parser.site.picture

import ciyin.io.extension
import ciyin.io.nameWithoutExtension
import ciyin.io.toFile
import ciyin.lang.match
import ciyin.parser.core.parametersOf
import ciyin.parser.core.picture.PictureParser
import ciyin.parser.core.picture.PictureParserType.Pool
import ciyin.parser.core.picture.PictureParserType.Pools
import ciyin.parser.core.picture.PictureParserType.Popular
import ciyin.parser.core.picture.PictureParserType.Post
import ciyin.parser.core.picture.PictureParserType.Posts
import ciyin.parser.core.picture.model.Picture
import ciyin.parser.core.picture.model.PictureResult
import ciyin.parser.core.url
import ciyin.parser.model.Rating
import ciyin.parser.model.Tag
import ciyin.parser.scope.ResponseScope
import ciyin.parser.site.PictureSiteId
import ciyin.parser.site.util.NumberAsStringSerializer
import ciyin.parser.site.util.toTimestamp
import ciyin.parser.util.PictureParserScope
import io.ktor.http.URLBuilder
import io.ktor.http.encodedPath
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Yande 解析器（新 DSL 版）。
 *
 * 该实现将旧版 `YandeParser` 的 URL 规则、脚本内联 JSON 提取和
 * `Post / Pool / Popular` 等解析逻辑迁移到多平台 DSL。
 */
class YandeParser : PictureParser() {

    private companion object {
        private const val PostsScriptIndex = 5
        private const val PostScriptIndex = 3
        private const val PoolsScriptIndex = 4
        private const val MillisPerDay = 86_400_000L

        private val ExcludedTags = setOf("video", "animated", "animated_gif")

    }

    /**
     * Yande 站点 DSL 配置入口。
     */
    override fun PictureParserScope.setup() {
        id = PictureSiteId.Yande
        baseUrl = "https://yande.re"

        onItemRevise {
            val isZip = fileExt.equals("zip", ignoreCase = true)
            val resolvedThumbnail = thumbnailUrl.ifBlank { sampleUrl.ifBlank { originalUrl } }
            val resolvedSample = sampleUrl.ifBlank { originalUrl.ifBlank { resolvedThumbnail } }
            val resolvedOriginal =
                originalUrl.ifBlank { resolvedSample.ifBlank { resolvedThumbnail } }
            val previewUrl =
                resolvedSample.ifBlank { resolvedThumbnail.ifBlank { resolvedOriginal } }
            val zipAssetUrl = if (isZip) resolvedOriginal.ifBlank { zipUrl } else zipUrl
            val finalOriginalUrl = if (isZip) previewUrl else resolvedOriginal
            val finalMd5 = md5.ifBlank {
                zipAssetUrl.ifBlank { finalOriginalUrl }.urlFileNameWithoutExtension()
            }
            copy(
                fileName = fileName.ifBlank { finalMd5 },
                thumbnailUrl = resolvedThumbnail,
                sampleUrl = previewUrl,
                originalUrl = finalOriginalUrl,
                zipUrl = zipAssetUrl,
                fileExt = when {
                    isZip -> previewUrl.urlFileExtension().ifBlank { fileExt }
                    fileExt.isBlank() -> finalOriginalUrl.urlFileExtension()
                    else -> fileExt
                },
                md5 = finalMd5,
            )
        }

        on(Posts) {
            request { req ->
                val parameters = parametersOf(
                    "page" to req.page,
                    "tags" to req.tags.joinToString(" "),
                )
                html { url("/post", parameters) }
            }

            response { result ->
                parsePostsResult(result)
            }
        }

        on(Post) {
            request { req ->
                html { url("/post/show/${req.id}") }
            }

            response { result ->
                parsePostResult(result)
            }
        }

        on(Pools) {
            request { req ->
                val parameters = parametersOf(
                    "page" to req.page,
                    "query" to req.search,
                )
                html { url("/pool", parameters) }
                json { url("/pool.json", parameters) }
            }

            response { result ->
                parsePoolsResult(result)
            }
        }

        on(Pool) {
            request { req ->
                json { url("/pool/show/${req.id}.json") }
            }

            response { result ->
                parsePoolResult(result)
            }
        }

        on(Popular) {
            request { req ->
                val targetDateTime = Instant.fromEpochMilliseconds(
                    Clock.System.now().toEpochMilliseconds() - req.page.toLong() * MillisPerDay
                ).toLocalDateTime(TimeZone.currentSystemDefault())
                val parameters = parametersOf(
                    "year" to targetDateTime.year,
                    "month" to targetDateTime.month.number,
                    "day" to targetDateTime.day,
                )
                html { url("/post/popular_by_${req.scale}", parameters) }
                json { url("/post/popular_by_${req.scale}.json", parameters) }
            }

            response { result ->
                parsePopularResult(result)
            }
        }
    }

    /**
     * 解析帖子列表页面。
     */
    private fun ResponseScope.parsePostsResult(result: PictureResult): PictureResult {
        return result.copy(
            tags = parseRegisteredTags(PostsScriptIndex),
            contents = parseRegisteredPosts(PostsScriptIndex).toSupportedPictures(),
            totalPages = totalPagesFromHtml(),
        )
    }

    /**
     * 解析帖子详情页面。
     */
    private fun ResponseScope.parsePostResult(result: PictureResult): PictureResult {
        val payload = parseRegisteredResults(PostScriptIndex)
            ?: return result.copy(totalPages = 1)
        val post = payload.posts.firstOrNull()
            ?: return result.copy(totalPages = 1)

        var picture = post.toPictureInfo(payload.pools.firstOrNull())

        if (picture.parentId == 0) {
            picture = picture.copy(
                parentId = bodyForHtml()
                    .match("href=\"/post\\?tags=parent%3A(\\d+)\"")
                    .toIntOrNull() ?: 0,
            )
        }

        if (picture.poolId == 0) {
            val poolLink = primaryPoolLink()
            if (poolLink != null) {
                picture = picture.copy(
                    poolId = poolLink.first,
                    name = poolLink.second,
                    poolUrl = "${configure.baseUrl}/pool/show/${poolLink.first}",
                )
            }
        }

        return result.copy(
            contents = listOf(picture).filterSupportedPictures(),
            totalPages = 1,
        )
    }

    /**
     * 解析画集列表页面。
     */
    private fun ResponseScope.parsePoolsResult(result: PictureResult): PictureResult {
        val contents = parseRegisteredPosts(PoolsScriptIndex).toSupportedPictures()
        val pools = if (bodyForJson().isBlank()) {
            emptyList()
        } else {
            bodyForJson<List<YandePool>>()
        }

        val revisedContents = if (pools.size == contents.size) {
            contents.zip(pools).map { (picture, pool) ->
                picture.copy(
                    poolId = pool.id,
                    name = pool.name,
                    poolUrl = pool.poolUrl(configure.baseUrl),
                )
            }
        } else {
            contents
        }

        return result.copy(
            contents = revisedContents,
            totalPages = totalPagesFromHtml(),
        )
    }

    /**
     * 解析单个画集详情。
     */
    private fun ResponseScope.parsePoolResult(result: PictureResult): PictureResult {
        val pool = bodyForJson<YandePool>()
        return result.copy(
            contents = pool.posts.toSupportedPictures(pool),
            totalPages = 1,
        )
    }

    /**
     * 解析热门榜单。
     */
    private fun ResponseScope.parsePopularResult(result: PictureResult): PictureResult {
        val jsonBody = bodyForJson().trim()
        if (jsonBody.isEmpty() || jsonBody == "[]") {
            return result.copy(totalPages = totalPagesFromHtml())
        }
        return result.copy(
            contents = bodyForJson<List<YandePost>>().toSupportedPictures(),
            totalPages = totalPagesFromHtml(),
        )
    }

    /**
     * 提取脚本中注册的帖子对象列表。
     */
    private fun ResponseScope.parseRegisteredPosts(preferredIndex: Int): List<YandePost> {
        return scriptPayloads(preferredIndex, "Post.register").map { payload ->
            payload.fromJson<YandePost>().apply { logger.d { "Post: $this" } }
        }
    }

    /**
     * 提取脚本中注册的详情结果对象。
     */
    private fun ResponseScope.parseRegisteredResults(preferredIndex: Int): YandeResults? {
        val payload = scriptPayloads(preferredIndex, "Post.register_resp").let { payloads ->
            payloads.firstOrNull { candidate ->
                candidate.contains("\"posts\"") && candidate.contains("\"pools\"")
            } ?: payloads.firstOrNull()
        }
        logger.d { "Results: $payload" }
        return payload?.fromJson<YandeResults>()
    }

    /** 提取脚本中注册的标签键集合。*/
    private fun ResponseScope.parseRegisteredTags(preferredIndex: Int): List<Tag> {
        return scriptPayloads(preferredIndex, "Post.register_tags")
            .firstOrNull()
            ?.let { payload ->
                Json.parseToJsonElement(payload)
                    .jsonObject
                    .keys
                    .map { Tag(it) }
            }
            .orEmpty()
    }

    /** 从脚本节点中提取匹配的 JSON 负载。*/
    private fun ResponseScope.scriptPayloads(preferredIndex: Int, funcName: String): List<String> {
        // Yande 某些脚本未必标明 type，这里直接遍历所有 script 节点以提高兼容性。
        val scripts = document.select("script[type=text/javascript]")
        val preferredPayloads = scripts.getOrNull(preferredIndex)
            ?.data()
            .orEmpty()
            .extractJsFuncArg(funcName)
        if (preferredPayloads.isNotEmpty()) {
            return preferredPayloads
        }
        return scripts.flatMap { element ->
            element.data().extractJsFuncArg(funcName)
        }
    }

    /** 提取页面中的主 pool 链接。*/
    private fun ResponseScope.primaryPoolLink(): Pair<Int, String>? {
        val element = document.selectFirst("""a[href^="/pool/show/"]""") ?: return null
        val id = element.attr("href").match("""/pool/show/(\d+)""").toIntOrNull() ?: return null
        return id to element.text().trim()
    }

    /** 从 Yande 分页组件中提取总页数。*/
    private fun ResponseScope.totalPagesFromHtml(): Int {
        return document.select("div#paginator div.pagination a")
            .mapNotNull { element ->
                element.attr("href").match("""page=(\d+)""").toIntOrNull()
            }
            .maxOrNull() ?: 1000
    }

    /** 将帖子列表转换为可用图片列表。*/
    private fun List<YandePost>.toSupportedPictures(pool: YandePool? = null): List<Picture> {
        return map { post ->
            post.toPictureInfo(pool)
        }.filterSupportedPictures()
    }

    /** 过滤掉当前解析层暂不支持的媒体类型。*/
    private fun List<Picture>.filterSupportedPictures(): List<Picture> {
        return filterNot { picture ->
            picture.tags.any { tag ->
                tag.tag.lowercase() in ExcludedTags
            }
        }
    }

    /** 将 Yande 帖子对象转换为通用 [Picture]。*/
    private fun YandePost.toPictureInfo(pool: YandePool? = null): Picture {
        val createdTs = createdAt.toTimestamp()
        val updatedTs = updatedAt.toTimestamp()
        val resolvedMd5 = md5.ifBlank {
            fileUrl.ifBlank { sampleUrl.ifBlank { previewUrl } }.urlFileNameWithoutExtension()
        }
        return Picture(
            fileName = resolvedMd5,
            id = id,
            parentId = parentId,
            pixivId = 0,
            name = pool?.name.orEmpty(),
            description = "",
            site = configure.id.site,
            originalUrl = fileUrl,
            sampleUrl = sampleUrl.ifBlank { jpegUrl },
            thumbnailUrl = previewUrl,
            sourceUrl = source,
            zipUrl = "",
            postUrl = "${configure.baseUrl}/post/show/$id",
            poolUrl = pool?.poolUrl(configure.baseUrl).orEmpty(),
            poolId = pool?.id ?: 0,
            fileExt = fileExt,
            fileSize = fileSize,
            md5 = resolvedMd5,
            width = width,
            height = height,
            rating = when (rating) {
                "s" -> Rating.Safe
                "q" -> Rating.Sensitive
                else -> Rating.Explicit
            },
            tags = tags.toTagInfos(),
            children = emptyList(),
            createdAt = createdTs,
            updatedAt = updatedTs,
        )
    }

    /** 从脚本中提取函数参数。*/
    fun String.extractJsFuncArg(funcName: String): List<String> {

        val result = mutableListOf<String>()
        val key = "$funcName("

        var index = 0

        while (true) {
            val start = indexOf(key, index)
            if (start == -1) break

            var i = start + key.length
            val argStart = i

            var depth = 1

            while (i < length) {
                when (this[i]) {
                    '(' -> depth++
                    ')' -> {
                        depth--
                        if (depth == 0) {
                            result.add(this.substring(argStart, i))
                            index = i + 1
                            break
                        }
                    }
                }
                i++
            }

            index = i
        }

        return result
    }

    /** 将 Yande 标签串拆分为结构化标签列表。*/
    private fun String.toTagInfos(): List<Tag> {
        return split(" ")
            .mapNotNull { name ->
                name.trim().takeIf(String::isNotEmpty)
            }
            .distinct()
            .map { Tag(it) }
    }

    /** 从 URL 路径中提取文件扩展名。*/
    private fun String.urlFileExtension(): String {
        if (isBlank()) {
            return ""
        }
        return URLBuilder(this).encodedPath.toFile().extension
    }

    /** 从 URL 路径中提取不带扩展名的文件名。*/
    private fun String.urlFileNameWithoutExtension(): String {
        if (isBlank()) {
            return ""
        }
        return URLBuilder(this).encodedPath.toFile().nameWithoutExtension
    }

    /** 构建 pool 的详情链接。*/
    private fun YandePool.poolUrl(baseUrl: String): String {
        return "$baseUrl/pool/show/$id"
    }

    /**
     * Yande 帖子详情聚合结果。
     */
    @Serializable
    private data class YandeResults(
        @SerialName("posts")
        val posts: List<YandePost> = emptyList(),
        @SerialName("pool_posts")
        val poolPosts: List<YandePoolPost> = emptyList(),
        @SerialName("pools")
        val pools: List<YandePool> = emptyList(),
        @SerialName("tags")
        var tags: Map<String, String> = emptyMap(),
    )

    /**
     * Yande 画集帖子关联对象。
     */
    @Serializable
    private data class YandePoolPost(
        @SerialName("id")
        val id: Int = 0,
        @SerialName("pool_id")
        val poolId: Int = 0,
        @SerialName("post_id")
        val postId: Int = 0,
        @SerialName("active")
        val active: Boolean = false,
        @SerialName("sequence")
        val sequence: String = "",
        @SerialName("next_post_id")
        val nextPostId: JsonElement? = null,
        @SerialName("prev_post_id")
        val prevPostId: Int = 0,
    )

    /**
     * Yande 画集对象。
     */
    @Serializable
    private data class YandePool(
        @SerialName("id")
        val id: Int = 0,
        @SerialName("name")
        val name: String = "",
        @SerialName("created_at")
        val createdAt: String = "",
        @SerialName("updated_at")
        val updatedAt: String = "",
        @SerialName("user_id")
        val userId: Long = 0L,
        @SerialName("is_public")
        val isPublic: Boolean = false,
        @SerialName("post_count")
        val postCount: Int = 0,
        @SerialName("posts")
        val posts: List<YandePost> = emptyList(),
        @SerialName("description")
        val description: String = "",
    )

    /**
     * Yande 帖子对象。
     */
    @Serializable
    private data class YandePost(
        @SerialName("actual_preview_height")
        val actualPreviewHeight: Int = 0,
        @SerialName("actual_preview_width")
        val actualPreviewWidth: Int = 0,
        @SerialName("approver_id")
        val approverId: Int = 0,
        @SerialName("author")
        val author: String = "",
        @SerialName("change")
        val change: Int = 0,
        @SerialName("creator_id")
        val creatorId: Int = 0,
        @SerialName("file_ext")
        val fileExt: String = "",
        @SerialName("file_size")
        val fileSize: Long = 0L,
        @SerialName("file_url")
        val fileUrl: String = "",
        @SerialName("frames")
        val frames: List<JsonElement> = emptyList(),
        @SerialName("frames_pending")
        val framesPending: List<JsonElement> = emptyList(),
        @SerialName("frames_pending_string")
        val framesPendingString: String = "",
        @SerialName("frames_string")
        val framesString: String = "",
        @SerialName("has_children")
        val hasChildren: Boolean = false,
        @SerialName("height")
        val height: Int = 0,
        @SerialName("id")
        val id: Int = 0,
        @SerialName("is_held")
        val isHeld: Boolean = false,
        @SerialName("is_note_locked")
        val isNoteLocked: Boolean = false,
        @SerialName("is_pending")
        val isPending: Boolean = false,
        @SerialName("is_rating_locked")
        val isRatingLocked: Boolean = false,
        @SerialName("is_shown_in_index")
        val isShownInIndex: Boolean = false,
        @SerialName("jpeg_file_size")
        val jpegFileSize: Int = 0,
        @SerialName("jpeg_height")
        val jpegHeight: Int = 0,
        @SerialName("jpeg_url")
        val jpegUrl: String = "",
        @SerialName("jpeg_width")
        val jpegWidth: Int = 0,
        @SerialName("last_commented_at")
        val lastCommentedAt: Int = 0,
        @SerialName("last_noted_at")
        val lastNotedAt: Int = 0,
        @SerialName("md5")
        val md5: String = "",
        @SerialName("parent_id")
        val parentId: Int = 0,
        @SerialName("preview_height")
        val previewHeight: Int = 0,
        @SerialName("preview_url")
        val previewUrl: String = "",
        @SerialName("preview_width")
        val previewWidth: Int = 0,
        @SerialName("rating")
        val rating: String = "",
        @SerialName("sample_file_size")
        val sampleFileSize: Int = 0,
        @SerialName("sample_height")
        val sampleHeight: Int = 0,
        @SerialName("sample_url")
        val sampleUrl: String = "",
        @SerialName("sample_width")
        val sampleWidth: Int = 0,
        @SerialName("score")
        val score: Int = 0,
        @SerialName("source")
        val source: String = "",
        @SerialName("status")
        val status: String = "",
        @SerialName("tags")
        val tags: String = "",
        @SerialName("created_at")
        @Serializable(NumberAsStringSerializer::class)
        val createdAt: String = "",
        @SerialName("updated_at")
        @Serializable(NumberAsStringSerializer::class)
        val updatedAt: String = "",
        @SerialName("width")
        val width: Int = 0,
    )

}
