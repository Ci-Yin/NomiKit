package ciyin.parser.site.picture

import ciyin.io.File
import ciyin.io.nameWithoutExtension
import ciyin.io.replaceName
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
import ciyin.parser.model.TagCategory
import ciyin.parser.scope.ResponseScope
import ciyin.parser.site.PictureSiteId
import ciyin.parser.util.PictureParserScope
import ciyin.platform.time.toInstant
import com.fleeksoft.ksoup.nodes.Element
import io.ktor.http.URLBuilder
import io.ktor.http.encodedPath
import io.ktor.http.path
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Danbooru 解析器（新 DSL 版）。
 */
class DanbooruParser : PictureParser() {

    /**
     * Danbooru 站点的 DSL 配置入口。
     */
    override fun PictureParserScope.setup() {

        id = PictureSiteId.Danbooru
        baseUrl = "https://danbooru.donmai.us"

        onItemRevise {
            val isZip = fileExt == "zip"
            val thumbnailUrl = thumbnailUrl.toImageUrl("360x360")
            val sampleUrl = sampleUrl.ifBlank { thumbnailUrl.toImageUrl("sample") }
            val originalUrl = originalUrl.ifBlank { thumbnailUrl.toImageUrl("original") }
            copy(
                thumbnailUrl = thumbnailUrl,
                sampleUrl = sampleUrl,
                originalUrl = if (isZip) sampleUrl else originalUrl,
                zipUrl = if (isZip) originalUrl else zipUrl,
                fileExt = if (isZip) "zip" else fileExt,
                md5 = md5.ifBlank { originalUrl.toFile().nameWithoutExtension },
            ).run {
                if (thumbnailUrl.isNotEmpty() && sampleUrl.isNotEmpty() && originalUrl.isNotEmpty()) {
                    this
                } else {
                    logger.w { "图片信息不完整，已忽略：$this" }
                    null
                }
            }

        }

        on(Posts) {

            request { req ->
                val parameters = parametersOf(
                    "page" to req.page,
                    "tags" to req.tags.joinToString(",")
                )
                html { url("/posts", parameters) }
                json { url("/posts.json", parameters) }
            }

            response { result ->
                parsePictureResult(result)
            }

        }

        on(Post) {

            request { req ->
                html { url("/posts/${req.id}") }
                json { url("/posts/${req.id}.json") }
            }

            response { result ->
                val info = bodyForJson<DanbooruPost>().toPicture()
                result.copy(
                    contents = listOf(info),
                    totalPages = 1,
                )
            }

        }

        on(Pools) {

            request { req ->
                val parameters = parametersOf(
                    "page" to req.page,
                    "search%5Bname_matches%5D" to req.search
                )
                html { url("/pools/gallery", parameters) }
                json { url("/pools/gallery.json", parameters) }
            }

            response { result ->
                parsePoolsResult(result)
            }

        }

        on(Pool) {

            request { req ->
                html { url("/pools/${req.id}") }
                json { url("/pools/${req.id}.json") }
            }

            response { result ->
                parsePoolsResult(result)
            }

        }

        on(Popular) {

            request { req ->
                val parameters = parametersOf(
                    "page" to req.page,
                    "scale" to req.scale
                )
                html { url("/explore/posts/popular", parameters) }
                json { url("/explore/posts/popular.json", parameters) }
            }

            response { result ->
                parsePictureResult(result)
            }

        }

    }

    private fun ResponseScope.parsePictureResult(result: PictureResult): PictureResult {
        if (bodyForJson().isEmpty()) {
            return result
        }
        return result.copy(
            tags = document.tags(),
            totalPages = document.talPages(),
            contents = bodyForJson<List<DanbooruPost>>().map { it.toPicture() },
        )
    }

    private fun ResponseScope.parsePoolsResult(result: PictureResult): PictureResult {
        return result.copy(
            contents = document.pictures(),
            totalPages = document.talPages(),
        )
    }

    /** 从 HTML 中提取图片信息。*/
    private fun Element.pictures(
        cssQuery: String = "div.posts-container article.post-preview",
    ): List<Picture> = select(cssQuery).map { element ->
        val img = element.select("img")
        val poolUrl = configure.baseUrl + element.select("a.post-preview-link")
            .attr("href")
        val thumbnailUrl = img.attr("src")
        Picture(
            thumbnailUrl = thumbnailUrl,
            poolId = poolUrl.match("\\d+").toIntOrNull() ?: 0,
            id = element.attr("data-id").toIntOrNull() ?: 0,
            md5 = thumbnailUrl.match("([a-f0-9]{32})(?=\\.)"),
            tags = element.attr("data-tags").split(" ").map { Tag(tag = it) },
            width = img.attr("width").toIntOrNull() ?: 0,
            height = img.attr("height").toIntOrNull() ?: 0,
            name = element.select("p.desc").text(),
            poolUrl = poolUrl,
        )
    }

    /** 从 HTML 分页组件中推断总页数。*/
    private fun Element.talPages(): Int {

        // 提取所有 <a class="paginator-page"> 的页码
        val pageLinks = select("a.paginator-page")
            .mapNotNull { it.text().trim().toIntOrNull() }

        // 提取当前页码（可能已经是最后一页）
        val currentPage = selectFirst("span.paginator-current")
            ?.text()
            ?.trim()
            ?.toIntOrNull()

        // 合并并找出最大值
        val allPages = pageLinks + (currentPage ?: 0)
        return allPages.maxOrNull() ?: 1000
    }

    /** 从 HTML 中提取标签。*/
    fun Element.tags(
        cssQuery: String = "ul.tag-list.search-tag-list li",
    ): List<Tag> = select(cssQuery).mapNotNull { element ->
        Tag(
            tag = element.attr("data-tag-name"),
            category = when (element.className()) {
                "tag-type-1" -> TagCategory.Artist
                "tag-type-2" -> TagCategory.Character
                "tag-type-3" -> TagCategory.Copyright
                "tag-type-5" -> TagCategory.Meta
                else -> TagCategory.General
            },
        )
    }

    /** 将 Danbooru JSON 对象转换为通用的 [Picture]。*/
    private fun DanbooruPost.toPicture(): Picture {
        val createdTs = createdAt.toInstant().epochSeconds
        val updatedTs = updatedAt.toInstant().epochSeconds

        var original = fileUrl
        var sample = previewFileUrl
        var thumb = previewFileUrl
        var ext = fileExt

        mediaAsset.variants.forEach { variant ->
            when (variant.type) {
                "original", "large" -> {
                    original = variant.url
                    ext = variant.fileExt.ifEmpty { ext }
                }

                "720x720", "sample" -> {
                    sample = variant.url
                }

                "preview", "360x360" -> {
                    thumb = variant.url
                }
            }
        }

        val ratingEnum = when (rating) {
            "g" -> Rating.Safe
            "s", "q" -> Rating.Sensitive
            else -> Rating.Explicit
        }

        return Picture(
            id = id,
            parentId = parentId,
            pixivId = pixivId,
            name = "",
            description = "",
            site = configure.id.site,
            originalUrl = original,
            sampleUrl = sample,
            thumbnailUrl = thumb,
            sourceUrl = source,
            zipUrl = "",
            postUrl = "${configure.baseUrl}/posts/$id",
            poolUrl = "",
            poolId = 0,
            fileExt = ext,
            fileSize = fileSize,
            md5 = md5,
            width = imageWidth,
            height = imageHeight,
            rating = ratingEnum,
            tags = buildTagInfos(),
            children = emptyList(),
            createdAt = createdTs,
            updatedAt = updatedTs,
        ).apply { logger.d { toString() } }
    }

    /** 将 Danbooru 标签字段拆分为结构化的 [Tag] 列表。*/
    private fun DanbooruPost.buildTagInfos(): List<Tag> {
        val result = mutableListOf<Tag>()
        fun appendAll(tags: String?) {
            tags.orEmpty()
                .split(" ")
                .mapNotNull { it.trim().takeIf(String::isNotEmpty) }
                .forEach { name ->
                    result += Tag(name)
                }
        }

        appendAll(tagStringArtist)
        appendAll(tagStringCopyright)
        appendAll(tagStringCharacter)
        appendAll(tagStringGeneral)
        appendAll(tagStringMeta)
        return result
    }

    private fun String.toImageUrl(type: String): String {
        if (this.isEmpty()) {
            return this
        }

        return URLBuilder(this).apply {
            path(encodedPath.replaceFirst("^/[^/]+".toRegex(), "/$type"))
            if (type == "sample") {
                val file = File(encodedPath)
                path(file.replaceName("sample-" + file.name))
            }
        }.build().toString()
    }

    // ====== Danbooru JSON 数据模型 ======

    @Serializable
    private data class DanbooruPost(
        @SerialName("approver_id")
        val approverId: Int = 0,
        @SerialName("bit_flags")
        val bitFlags: Int = 0,
        @SerialName("created_at")
        val createdAt: String = "",
        @SerialName("down_score")
        val downScore: Int = 0,
        @SerialName("fav_count")
        val favCount: Int = 0,
        @SerialName("file_ext")
        val fileExt: String = "",
        @SerialName("file_size")
        val fileSize: Long = 0L,
        @SerialName("file_url")
        val fileUrl: String = "",
        @SerialName("has_active_children")
        val hasActiveChildren: Boolean = false,
        @SerialName("has_children")
        val hasChildren: Boolean = false,
        @SerialName("has_large")
        val hasLarge: Boolean = false,
        @SerialName("has_visible_children")
        val hasVisibleChildren: Boolean = false,
        @SerialName("id")
        val id: Int = 0,
        @SerialName("image_height")
        val imageHeight: Int = 0,
        @SerialName("image_width")
        val imageWidth: Int = 0,
        @SerialName("is_banned")
        val isBanned: Boolean = false,
        @SerialName("is_deleted")
        val isDeleted: Boolean = false,
        @SerialName("is_flagged")
        val isFlagged: Boolean = false,
        @SerialName("is_pending")
        val isPending: Boolean = false,
        @SerialName("large_file_url")
        val largeFileUrl: String = "",
        @SerialName("last_comment_bumped_at")
        val lastCommentBumpedAt: String = "",
        @SerialName("last_commented_at")
        val lastCommentedAt: String = "",
        @SerialName("last_noted_at")
        val lastNotedAt: String = "",
        @SerialName("md5")
        val md5: String = "",
        @SerialName("media_asset")
        val mediaAsset: MediaAsset = MediaAsset(),
        @SerialName("parent_id")
        val parentId: Int = 0,
        @SerialName("pixiv_id")
        val pixivId: Int = 0,
        @SerialName("preview_file_url")
        val previewFileUrl: String = "",
        @SerialName("rating")
        val rating: String = "",
        @SerialName("score")
        val score: Int = 0,
        @SerialName("source")
        val source: String = "",
        @SerialName("tag_count")
        val tagCount: Int = 0,
        @SerialName("tag_count_artist")
        val tagCountArtist: Int = 0,
        @SerialName("tag_count_character")
        val tagCountCharacter: Int = 0,
        @SerialName("tag_count_copyright")
        val tagCountCopyright: Int = 0,
        @SerialName("tag_count_general")
        val tagCountGeneral: Int = 0,
        @SerialName("tag_count_meta")
        val tagCountMeta: Int = 0,
        @SerialName("tag_string")
        val tagString: String = "",
        @SerialName("tag_string_artist")
        val tagStringArtist: String = "",
        @SerialName("tag_string_character")
        val tagStringCharacter: String = "",
        @SerialName("tag_string_copyright")
        val tagStringCopyright: String = "",
        @SerialName("tag_string_general")
        val tagStringGeneral: String = "",
        @SerialName("tag_string_meta")
        val tagStringMeta: String = "",
        @SerialName("up_score")
        val upScore: Int = 0,
        @SerialName("updated_at")
        val updatedAt: String = "",
        @SerialName("uploader_id")
        val uploaderId: Int = 0,
    )

    @Serializable
    private data class Variant(
        @SerialName("type")
        val type: String = "",
        @SerialName("url")
        val url: String = "",
        @SerialName("width")
        val width: Int = 0,
        @SerialName("height")
        val height: Int = 0,
        @SerialName("file_ext")
        val fileExt: String = "",
    )

    @Serializable
    private data class MediaAsset(
        @SerialName("id")
        val id: Int = 0,
        @SerialName("created_at")
        val createdAt: String = "",
        @SerialName("updated_at")
        val updatedAt: String = "",
        @SerialName("md5")
        val md5: String = "",
        @SerialName("file_ext")
        val fileExt: String = "",
        @SerialName("file_size")
        val fileSize: Int = 0,
        @SerialName("image_width")
        val imageWidth: Int = 0,
        @SerialName("image_height")
        val imageHeight: Int = 0,
        @SerialName("duration")
        val duration: Double = 0.0,
        @SerialName("status")
        val status: String = "",
        @SerialName("file_key")
        val fileKey: String = "",
        @SerialName("is_public")
        val isPublic: Boolean = false,
        @SerialName("pixel_hash")
        val pixelHash: String = "",
        @SerialName("variants")
        val variants: List<Variant> = emptyList(),
    )

}
