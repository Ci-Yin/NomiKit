package ciyin.parser.site.picture

import ciyin.io.extension
import ciyin.io.nameWithoutExtension
import ciyin.io.toFile
import ciyin.parser.core.parametersOf
import ciyin.parser.core.picture.PictureParser
import ciyin.parser.core.picture.PictureParserType.Detail
import ciyin.parser.core.picture.PictureParserType.Feed
import ciyin.parser.core.picture.model.Picture
import ciyin.parser.core.picture.model.PictureResult
import ciyin.parser.core.url
import ciyin.parser.model.Rating
import ciyin.parser.model.Tag
import ciyin.parser.model.TagCategory
import ciyin.parser.scope.ResponseScope
import ciyin.parser.site.PictureSiteId
import ciyin.parser.util.PictureParserScope
import ciyin.platform.time.toInstant
import io.ktor.http.URLBuilder
import io.ktor.http.encodedPath
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * Gelbooru 站点解析器的新 DSL 实现。
 *
 * 该实现对齐旧版 `GelbooruParser` 的分页与 DAPI 调用方式，
 * 同时复用当前多平台 Parser DSL 的请求注册、结果映射和统一字段补全能力。
 * @param baseUrlOverride 可选基础地址，用于镜像端点与离线请求链契约测试。
 */
class GelbooruParser(baseUrlOverride: String? = null) : PictureParser(baseUrlOverride) {

    /**
     * Gelbooru 解析器内部常量与日期辅助映射。
     */
    private companion object {
        /** Gelbooru 单页帖子数量，用于把页码换算为 pid 偏移。 */
        private const val ItemsPerPage = 42

        private val GelbooruDateRegex =
            Regex("""^\w{3}\s+(\w{3})\s+(\d{1,2})\s+(\d{2}):(\d{2}):(\d{2})\s+([+-]\d{4})\s+(\d{4})$""")

        private val MonthNumbers = mapOf(
            "Jan" to "01",
            "Feb" to "02",
            "Mar" to "03",
            "Apr" to "04",
            "May" to "05",
            "Jun" to "06",
            "Jul" to "07",
            "Aug" to "08",
            "Sep" to "09",
            "Oct" to "10",
            "Nov" to "11",
            "Dec" to "12",
        )
    }

    /**
     * 配置 Gelbooru 站点的 DSL 请求与响应逻辑。
     */
    override fun PictureParserScope.setup() {
        id = PictureSiteId.Gelbooru
        baseUrl = "https://gelbooru.com"

        onItemRevise {
            val resolvedThumbnail = thumbnailUrl.ifBlank { sampleUrl.ifBlank { originalUrl } }
            val resolvedSample = sampleUrl.ifBlank { originalUrl.ifBlank { resolvedThumbnail } }
            val resolvedOriginal =
                originalUrl.ifBlank { resolvedSample.ifBlank { resolvedThumbnail } }
            val assetUrl = resolvedOriginal.ifBlank { resolvedSample.ifBlank { resolvedThumbnail } }
            val resolvedMd5 = md5.ifBlank { assetUrl.urlFileNameWithoutExtension() }
            val pictureId = this.id
            copy(
                fileName = fileName.ifBlank { resolvedMd5 },
                thumbnailUrl = resolvedThumbnail,
                sampleUrl = resolvedSample,
                originalUrl = resolvedOriginal,
                postUrl = postUrl.ifBlank {
                    if (pictureId.isBlank()) {
                        ""
                    } else {
                        "${configure.baseUrl}/index.php?page=post&s=view&id=$pictureId"
                    }
                },
                fileExt = fileExt.ifBlank { assetUrl.urlFileExtension() },
                md5 = resolvedMd5,
            )
        }

        on(Feed) {
            request { req ->
                val page = req.page.coerceAtLeast(1)
                val htmlParameters = parametersOf(
                    "pid" to (page - 1) * ItemsPerPage,
                    "page" to "post",
                    "s" to "list",
                    "tags" to req.tags.joinToString(" "),
                )
                val jsonParameters = htmlParameters.toMutableMap().apply {
                    // DAPI 的 pid 是零基页码，不是 HTML 的帖子偏移。
                    this["pid"] = page - 1
                    this["page"] = "dapi"
                    this["s"] = "post"
                    this["q"] = "index"
                    this["json"] = "1"
                    this["limit"] = ItemsPerPage
                }
                html { url("/index.php", htmlParameters) }
                json { url("/index.php", jsonParameters) }
            }

            response { result ->
                parseFeedResult(result)
            }
        }

        on(Detail) {
            request { req ->
                val htmlParameters = parametersOf(
                    "page" to "post",
                    "s" to "view",
                    "id" to req.id,
                )
                val jsonParameters = htmlParameters.toMutableMap().apply {
                    // 详情按 ID 定位，不随列表请求页码偏移唯一结果。
                    this["pid"] = 0
                    this["page"] = "dapi"
                    this["s"] = "post"
                    this["q"] = "index"
                    this["json"] = "1"
                    this["limit"] = ItemsPerPage
                }
                html { url("/index.php", htmlParameters) }
                json { url("/index.php", jsonParameters) }
            }

            response { result ->
                parseDetailResult(result)
            }
        }
    }

    /**
     * 解析 Gelbooru Feed 列表结果。
     */
    private fun ResponseScope.parseFeedResult(result: PictureResult): PictureResult {
        val siteTags = parseHtmlTags()
        val tagIndex = siteTags.associateBy { it.tag }
        val payload = parsePayload()

        val contents = payload.post.map { post ->
            post.toPictureInfo(tagIndex)
        }
        val totalPages = when {
            payload.attributes.count > 0 -> totalPagesFromCount(payload.attributes.count)
            else -> totalPagesFromHtml()
        }
        return result.copy(
            tags = siteTags,
            contents = contents,
            pagination = result.pagination.copy(totalPages = totalPages),
        )
    }

    /**
     * 解析 Gelbooru Detail 详情结果。
     */
    private fun ResponseScope.parseDetailResult(result: PictureResult): PictureResult {
        val siteTags = parseHtmlTags()
        val tagIndex = siteTags.associateBy { it.tag }
        val posts = parsePayload().post
        require(posts.size == 1) { "Gelbooru 详情未返回唯一帖子：${posts.size}" }
        val picture = posts.single().toPictureInfo(tagIndex)

        return result.copy(
            tags = siteTags,
            contents = listOf(picture),
        )
    }

    /**
     * 解析 DAPI 的帖子信封；只接受显式空数组或计数为零的空结果，不把错误对象当作空列表。
     */
    private fun ResponseScope.parsePayload(): GelbooruPostsResponse {
        val payload = Json.parseToJsonElement(bodyForJson())
        if (payload is JsonArray && payload.isEmpty()) return GelbooruPostsResponse(post = emptyList())
        require(payload is JsonObject && "success" !in payload && "error" !in payload) {
            "Gelbooru DAPI 返回错误或非帖子结构：$payload"
        }
        if ("post" !in payload) {
            val attributes = payload["@attributes"] as? JsonObject
            require(attributes?.get("count")?.jsonPrimitive?.intOrNull == 0) {
                "Gelbooru DAPI 缺少 post 列表：$payload"
            }
            return GelbooruPostsResponse(post = emptyList())
        }
        return bodyForJson<GelbooruPostsResponse>()
    }

    /**
     * 从 Gelbooru HTML 标签侧栏解析结构化标签。
     */
    private fun ResponseScope.parseHtmlTags(): List<Tag> {
        return document.select("ul.tag-list li")
            .mapNotNull { element ->
                val tagName = element.select("a").lastOrNull()
                    ?.text()
                    ?.trim()
                    ?.replace(" ", "_")
                    .orEmpty()
                if (tagName.isBlank()) {
                    return@mapNotNull null
                }
                Tag(
                    tag = tagName,
                    count = Regex("\\d+").find(element.select("span").text())
                        ?.value
                        ?.toIntOrNull() ?: 0,
                    category = when {
                        element.className().contains("tag-type-artist") -> TagCategory.Artist
                        element.className().contains("tag-type-copyright") -> TagCategory.Copyright
                        element.className().contains("tag-type-character") -> TagCategory.Character
                        element.className().contains("tag-type-metadata") -> TagCategory.Meta
                        else -> TagCategory.General
                    },
                )
            }
            .distinctBy { it.tag }
    }

    /**
     * 根据 Gelbooru 返回的总条数计算精确总页数。
     */
    private fun totalPagesFromCount(count: Int): Int? {
        if (count <= 0) return null
        return (count + ItemsPerPage - 1) / ItemsPerPage
    }

    /**
     * 从 HTML 分页组件推断总页数。
     */
    private fun ResponseScope.totalPagesFromHtml(): Int? {
        val maxPid = document.select("div.pagination a[href]")
            .mapNotNull { element ->
                Regex("""[?&]pid=(\d+)""").find(element.attr("href"))
                    ?.groupValues
                    ?.getOrNull(1)
                    ?.toIntOrNull()
            }
            .maxOrNull() ?: return null
        return (maxPid / ItemsPerPage) + 1
    }

    /**
     * 将 Gelbooru 帖子对象映射为通用 [Picture]。
     */
    private fun GelbooruPost.toPictureInfo(tagIndex: Map<String, Tag>): Picture {
        require(id > 0) { "Gelbooru 帖子缺少合法 ID" }
        val primaryUrl = fileUrl.ifBlank { sampleUrl.ifBlank { previewUrl } }
        val resolvedMd5 = md5.ifBlank {
            primaryUrl.urlFileNameWithoutExtension().ifBlank { image.toFile().nameWithoutExtension }
        }
        val resolvedFileExt = fileExt.ifBlank {
            primaryUrl.urlFileExtension().ifBlank { image.toFile().extension }
        }
        val createdTs = createdAt.toEpochSeconds(change)
        val updatedTs = change.normalizeEpochSeconds().takeIf { it > 0 } ?: createdTs

        return Picture(
            fileName = resolvedMd5,
            id = id.takeIf { it > 0 }?.toString().orEmpty(),
            parentId = parentId.takeIf { it > 0 }?.toString().orEmpty(),
            pixivId = 0,
            name = "",
            description = "",
            site = configure.id.site,
            originalUrl = fileUrl,
            sampleUrl = sampleUrl,
            thumbnailUrl = previewUrl,
            sourceUrl = source,
            zipUrl = "",
            postUrl = "${configure.baseUrl}/index.php?page=post&s=view&id=$id",
            poolUrl = "",
            poolId = 0,
            fileExt = resolvedFileExt,
            fileSize = fileSize,
            md5 = resolvedMd5,
            width = width,
            height = height,
            rating = rating.toRating(),
            tags = tags.toTagInfos(tagIndex),
            children = emptyList(),
            createdAt = createdTs,
            updatedAt = updatedTs,
        )
    }

    /**
     * 将 Gelbooru 标签串转换为结构化标签列表。
     */
    private fun String.toTagInfos(tagIndex: Map<String, Tag>): List<Tag> {
        return split(" ")
            .mapNotNull { value ->
                value.trim().takeIf(String::isNotEmpty)
            }
            .distinct()
            .map { name ->
                tagIndex[name] ?: Tag(
                    tag = name,
                )
            }
    }

    /**
     * 将 Gelbooru 评分字符串映射为通用 [Rating]。
     */
    private fun String.toRating(): Rating {
        return when (lowercase()) {
            "s", "safe", "general" -> Rating.Safe
            "q", "questionable", "sensitive" -> Rating.Sensitive
            else -> Rating.Explicit
        }
    }

    /**
     * 解析 Gelbooru 的时间字段为秒级时间戳。
     */
    private fun String.toEpochSeconds(fallback: Long = 0L): Long {
        val value = trim()
        if (value.isEmpty()) {
            return fallback.normalizeEpochSeconds()
        }

        value.toLongOrNull()?.let { timestamp ->
            return timestamp.normalizeEpochSeconds()
        }

        runCatching { value.toInstant().epochSeconds }
            .getOrNull()
            ?.let { return it }

        val matchResult =
            GelbooruDateRegex.matchEntire(value) ?: return fallback.normalizeEpochSeconds()
        val (monthText, dayText, hourText, minuteText, secondText, rawOffset, yearText) =
            matchResult.destructured
        val month = MonthNumbers[monthText] ?: return fallback.normalizeEpochSeconds()
        val offset = "${rawOffset.substring(0, 3)}:${rawOffset.substring(3)}"
        val isoValue = buildString {
            append(yearText)
            append('-')
            append(month)
            append('-')
            append(dayText.padStart(2, '0'))
            append('T')
            append(hourText)
            append(':')
            append(minuteText)
            append(':')
            append(secondText)
            append(offset)
        }
        return runCatching { isoValue.toInstant().epochSeconds }
            .getOrElse { fallback.normalizeEpochSeconds() }
    }

    /**
     * 将可能为毫秒的时间戳统一归一化为秒级。
     */
    private fun Long.normalizeEpochSeconds(): Long {
        return when {
            this <= 0L -> 0L
            this > 9_999_999_999L -> this / 1000L
            else -> this
        }
    }

    /**
     * 从 URL 路径中提取文件扩展名。
     */
    private fun String.urlFileExtension(): String {
        if (isBlank()) {
            return ""
        }
        return URLBuilder(this).encodedPath.toFile().extension
    }

    /**
     * 从 URL 路径中提取不带扩展名的文件名。
     */
    private fun String.urlFileNameWithoutExtension(): String {
        if (isBlank()) {
            return ""
        }
        return URLBuilder(this).encodedPath.toFile().nameWithoutExtension
    }

    /**
     * Gelbooru 帖子列表响应对象。
     */
    @Serializable
    private data class GelbooruPostsResponse(
        @SerialName("@attributes")
        val attributes: GelbooruAttributes = GelbooruAttributes(),
        @SerialName("post")
        val post: List<GelbooruPost>,
    )

    /**
     * Gelbooru DAPI 顶层属性对象。
     */
    @Serializable
    private data class GelbooruAttributes(
        @SerialName("limit")
        @Serializable(with = PrimitiveIntSerializer::class)
        val limit: Int = 0,
        @SerialName("offset")
        @Serializable(with = PrimitiveIntSerializer::class)
        val offset: Int = 0,
        @SerialName("count")
        @Serializable(with = PrimitiveIntSerializer::class)
        val count: Int = 0,
    )

    /**
     * Gelbooru 帖子对象。
     */
    @Serializable
    private data class GelbooruPost(
        @SerialName("id")
        @Serializable(with = PrimitiveIntSerializer::class)
        val id: Int = 0,
        @SerialName("parent_id")
        @Serializable(with = PrimitiveIntSerializer::class)
        val parentId: Int = 0,
        @SerialName("created_at")
        @Serializable(with = PrimitiveStringSerializer::class)
        val createdAt: String = "",
        @SerialName("change")
        @Serializable(with = PrimitiveLongSerializer::class)
        val change: Long = 0L,
        @SerialName("md5")
        val md5: String = "",
        @SerialName("tags")
        val tags: String = "",
        @SerialName("source")
        val source: String = "",
        @SerialName("file_url")
        val fileUrl: String = "",
        @SerialName("sample_url")
        val sampleUrl: String = "",
        @SerialName("preview_url")
        val previewUrl: String = "",
        @SerialName("image")
        val image: String = "",
        @SerialName("file_ext")
        val fileExt: String = "",
        @SerialName("file_size")
        @Serializable(with = PrimitiveLongSerializer::class)
        val fileSize: Long = 0L,
        @SerialName("width")
        @Serializable(with = PrimitiveIntSerializer::class)
        val width: Int = 0,
        @SerialName("height")
        @Serializable(with = PrimitiveIntSerializer::class)
        val height: Int = 0,
        @SerialName("rating")
        val rating: String = "",
    )

    /**
     * 将任意 JSON 原始值解析为字符串。
     */
    private object PrimitiveStringSerializer : KSerializer<String> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("PrimitiveString", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): String {
            val jsonDecoder = decoder as? JsonDecoder
                ?: return decoder.decodeString()
            val element = jsonDecoder.decodeJsonElement()
            if (element is JsonPrimitive) {
                return element.content
            }
            throw SerializationException("Expected primitive string, but was: $element")
        }

        override fun serialize(encoder: Encoder, value: String) {
            encoder.encodeString(value)
        }
    }

    /**
     * 将任意 JSON 原始值解析为 Int。
     */
    private object PrimitiveIntSerializer : KSerializer<Int> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("PrimitiveInt", PrimitiveKind.INT)

        override fun deserialize(decoder: Decoder): Int {
            val jsonDecoder = decoder as? JsonDecoder
                ?: return decoder.decodeInt()
            val element = jsonDecoder.decodeJsonElement()
            if (element is JsonPrimitive) {
                return element.content.toIntOrNull() ?: 0
            }
            throw SerializationException("Expected primitive int, but was: $element")
        }

        override fun serialize(encoder: Encoder, value: Int) {
            encoder.encodeInt(value)
        }
    }

    /**
     * 将任意 JSON 原始值解析为 Long。
     */
    private object PrimitiveLongSerializer : KSerializer<Long> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("PrimitiveLong", PrimitiveKind.LONG)

        override fun deserialize(decoder: Decoder): Long {
            val jsonDecoder = decoder as? JsonDecoder
                ?: return decoder.decodeLong()
            val element = jsonDecoder.decodeJsonElement()
            if (element is JsonPrimitive) {
                return element.content.toLongOrNull() ?: 0L
            }
            throw SerializationException("Expected primitive long, but was: $element")
        }

        override fun serialize(encoder: Encoder, value: Long) {
            encoder.encodeLong(value)
        }
    }
}
