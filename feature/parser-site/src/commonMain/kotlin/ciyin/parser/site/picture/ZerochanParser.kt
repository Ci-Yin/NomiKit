package ciyin.parser.site.picture

import ciyin.io.extension
import ciyin.parser.core.parametersOf
import ciyin.parser.core.picture.PictureParser
import ciyin.parser.core.picture.PictureParserType.Home
import ciyin.parser.core.picture.PictureParserType.Post
import ciyin.parser.core.picture.PictureParserType.Posts
import ciyin.parser.core.picture.model.Picture
import ciyin.parser.core.picture.model.PictureResult
import ciyin.parser.core.url
import ciyin.parser.model.Rating
import ciyin.parser.model.Tag
import ciyin.parser.scope.ResponseScope
import ciyin.parser.site.PictureSiteId
import ciyin.parser.util.PictureParserScope
import io.ktor.http.URLBuilder
import io.ktor.http.encodedPath
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Zerochan 解析器（新 DSL 版）。
 *
 * 基于旧版 `ZerochanParser` 的 URL 与 JSON 协议迁移到多平台 DSL。
 */
class ZerochanParser : PictureParser() {

    /**
     * Zerochan 站点 DSL 配置入口。
     */
    override fun PictureParserScope.setup() {
        id = PictureSiteId.Zerochan
        baseUrl = "https://www.zerochan.net"

        onItemRevise {
            val resolvedMd5 = md5.ifBlank { originalUrl.urlFileNameWithoutExtension() }
            val resolvedWeb = site.ifBlank { configure.id.site }
            val resolvedFileName = fileName.ifBlank {
                "${resolvedWeb}_${id}_${resolvedMd5}.${fileExt}"
            }
            copy(
                fileName = resolvedFileName,
                fileExt = fileExt,
                site = resolvedWeb,
                md5 = resolvedMd5,
                children = if (children.isEmpty() && resolvedFileName.isNotBlank()) {
                    listOf(resolvedFileName)
                } else {
                    children
                },
            )
        }

        on(Home) {
            request {
                html { url(parameters = parametersOf("json" to "")) }
            }

            response { result ->
                parsePostsResult(result)
            }
        }

        on(Posts) {
            request { req ->
                html { url(buildTagPath(req.tags), parametersOf("p" to req.page)) }
                json { url(buildTagPath(req.tags), parametersOf("p" to req.page, "json" to "")) }
            }

            response { result ->
                parsePostsResult(result)
            }
        }

        on(Post) {
            request { req ->
                html { url(req.id) }
                json { url(req.id, parametersOf("json" to "")) }
            }

            response { result ->
                parsePostResult(result)
            }
        }
    }

    /**
     * 解析帖子列表结果。
     */
    private fun ResponseScope.parsePostsResult(result: PictureResult): PictureResult {
        val jsonBody = bodyForJson().trim()
        if (jsonBody.isBlank() || !jsonBody.contains("\"items\"")) {
            return result
        }

        val payload = bodyForJson<ZerochanResults>()
        val contents = payload.items.map { it.toPicture() }
        val tags = contents
            .flatMap { it.tags }
            .distinctBy { it.tag }

        val totalPages = document.select("div.pagination a")
            .mapNotNull { element -> element.text().trim().toIntOrNull() }
            .maxOrNull()
            ?: if (contents.isNotEmpty()) 1 else 0

        return result.copy(
            contents = contents,
            tags = tags,
            totalPages = totalPages,
        )
    }

    /**
     * 解析单帖详情结果。
     */
    private fun ResponseScope.parsePostResult(result: PictureResult): PictureResult {
        val jsonBody = bodyForJson().trim()
        if (jsonBody.isBlank()) {
            return result.copy(totalPages = 1)
        }
        val contents = bodyForJson<ZerochanItem>().toPicture()
        return result.copy(
            contents = listOf(contents),
            totalPages = 1,
        )
    }

    /**
     * 将通用标签列表转换为 Zerochan 路径参数。
     *
     * 旧实现中通过 `deTag(tagsStr)` 完成：
     *
     * - `+` 作为标签分隔符；
     * - `_` 替换为空格；
     * - 站点侧使用 `,` 与 `+` 组合表达多标签。
     */
    private fun buildTagPath(tags: List<String>): String {
        if (tags.isEmpty()) {
            // 无标签时访问首页。
            return ""
        }
        val raw = tags.joinToString("+") { enTag(it) }
        return raw.replace("+", ",").replace("_", "+")
    }

    /**
     * 转换成通用标签格式（类似编码）。
     */
    private fun enTag(tag: String): String {
        return tag.trim()
            .replace(" ", "_")
            .lowercase()
    }

    /**
     * 将 Zerochan JSON 对象转换为通用的 [Picture]。
     */
    private fun ZerochanItem.toPicture(): Picture {

        val thumbnailUrl = thumbnail.ifBlank { small }
        val original = full.ifBlank {
            "https://static.zerochan.net/Piccolo.full.$id.jpg"
        }

        val resolvedMd5 = hash.ifBlank { md5 }
        val resolvedExt = original.extension

        return Picture(
            fileName = "",
            id = id,
            parentId = 0,
            pixivId = 0,
            name = "",
            description = "",
            site = configure.id.site,
            originalUrl = original,
            sampleUrl = large.ifBlank { "https://static.zerochan.net/Piccolo.600.$id.jpg" },
            thumbnailUrl = thumbnailUrl.replace("zerochan.net/75", "zerochan.net/240"),
            sourceUrl = source,
            zipUrl = "",
            postUrl = "${configure.baseUrl}/$id",
            poolUrl = "",
            poolId = 0,
            fileExt = resolvedExt,
            fileSize = size,
            md5 = resolvedMd5,
            width = width,
            height = height,
            rating = Rating.Safe,
            tags = tags.map { Tag(enTag(it)) },
            children = emptyList(),
            createdAt = 0L,
            updatedAt = 0L,
        )
    }

    /**
     * 从 URL 路径中提取文件扩展名。
     */
    private fun String.urlFileExtension(): String {
        if (isBlank()) {
            return ""
        }
        val path = URLBuilder(this).encodedPath
        val lastSegment = path.substringAfterLast('/')
        val dotIndex = lastSegment.lastIndexOf('.')
        return if (dotIndex != -1 && dotIndex + 1 < lastSegment.length) {
            lastSegment.substring(dotIndex + 1)
        } else {
            ""
        }
    }

    /**
     * 从 URL 路径中提取不带扩展名的文件名。
     */
    private fun String.urlFileNameWithoutExtension(): String {
        if (isBlank()) {
            return ""
        }
        val path = URLBuilder(this).encodedPath
        val lastSegment = path.substringAfterLast('/')
        val dotIndex = lastSegment.lastIndexOf('.')
        return if (dotIndex != -1) {
            lastSegment.substring(0, dotIndex)
        } else {
            lastSegment
        }
    }

    /**
     * Zerochan 帖子列表响应对象。
     */
    @Serializable
    private data class ZerochanResults(
        @SerialName("items")
        val items: List<ZerochanItem> = emptyList(),
    )

    /**
     * Zerochan 帖子对象。
     */
    @Serializable
    private data class ZerochanItem(
        @SerialName("id")
        val id: Int = 0,
        @SerialName("small")
        val small: String = "",
        @SerialName("thumbnail")
        val thumbnail: String = "",
        @SerialName("large")
        val large: String = "",
        @SerialName("full")
        val full: String = "",
        @SerialName("width")
        val width: Int = 0,
        @SerialName("height")
        val height: Int = 0,
        @SerialName("size")
        val size: Long = 0,
        @SerialName("md5")
        val md5: String = "",
        @SerialName("hash")
        val hash: String = "",
        @SerialName("primary")
        val primary: String = "",
        @SerialName("source")
        val source: String = "",
        @SerialName("tags")
        val tags: List<String> = emptyList(),
    )
}

