package ciyin.parser.site.picture

import ciyin.io.extension
import ciyin.io.replaceExtension
import ciyin.lang.match
import ciyin.parser.core.parametersOf
import ciyin.parser.core.picture.PictureParser
import ciyin.parser.core.picture.PictureParserType
import ciyin.parser.core.picture.PictureParserType.Post
import ciyin.parser.core.picture.PictureParserType.Posts
import ciyin.parser.core.picture.model.Picture
import ciyin.parser.core.picture.model.PictureRequest
import ciyin.parser.core.picture.model.PictureResult
import ciyin.parser.core.url
import ciyin.parser.model.Rating
import ciyin.parser.model.Tag
import ciyin.parser.model.TagCategory
import ciyin.parser.scope.ParserScope
import ciyin.parser.scope.ResponseScope
import ciyin.parser.site.util.NumberAsStringSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 *
 * 抽象的 Booru 解析器。
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2026/3/8 7:19
 */
abstract class BaseBooruParser : PictureParser() {
    /**
     * Booru 解析器内部常量与日期辅助映射。
     */
    private companion object {
        private const val PostsPid = 42
        private const val PoolsPid = 24
    }

    /** 配置 Booru 站点的 DSL 请求与响应逻辑。*/
    protected fun ParserScope<PictureParserType, PictureRequest, PictureResult>.superSetup() {

        onItemRevise {
            copy(
                postUrl = "${baseUrl}?page=post&s=view&id=${id}",
                sampleUrl = sampleUrl.ifBlank { originalUrl },
                fileExt = originalUrl.extension
            )
        }

        on<Posts> {
            request { req ->
                val page = req.page.coerceAtLeast(1)
                val htmlParameters = parametersOf(
                    "pid" to (page - 1) * PostsPid,
                    "page" to "post",
                    "s" to "list",
                    "tags" to req.tags.joinToString(" "),
                )
                val jsonParameters = htmlParameters.toMutableMap().apply {
                    this["page"] = "dapi"
                    this["s"] = "post"
                    this["q"] = "index"
                    this["json"] = "1"
                    this["limit"] = PostsPid
                }
                html { url("/index.php", htmlParameters) }
                json { url("/index.php", jsonParameters) }
            }

            response { result ->
                parsePostsResult(result)
            }
        }

        on<Post> {
            request { req ->
                val page = req.page.coerceAtLeast(1)
                val htmlParameters = parametersOf(
                    "pid" to (page - 1) * PostsPid,
                    "page" to "post",
                    "s" to "view",
                    "id" to req.id,
                )
                val jsonParameters = htmlParameters.toMutableMap().apply {
                    this["page"] = "dapi"
                    this["s"] = "post"
                    this["q"] = "index"
                    this["json"] = "1"
                    this["limit"] = PostsPid
                }
                html { url("/index.php", htmlParameters) }
                json { url("/index.php", jsonParameters) }
            }

            response { result ->
                parsePostsResult(result)
            }
        }

    }

    /** 解析 Booru 帖子列表结果。*/
    private fun ResponseScope.parsePostsResult(result: PictureResult): PictureResult {
        return result.copy(
            totalPages = totalPagesFromHtml(),
            tags = tagsFromHtml(),
            contents = bodyForJson<List<BooruPost>>().map { it.toPicture() }
        )
    }

    /** 从 HTML 分页组件中推断总页数。*/
    private fun ResponseScope.totalPagesFromHtml(): Int {
        //处理HTML数据
        val elements = document.select("div.pagination").select("a")
        return if (elements.isNotEmpty()) {
            val href = elements.last()!!.attr("href")
            val page = href.match("pid=(\\d+)")
            (page.toIntOrNull() ?: 1) / PostsPid
        } else {
            0
        }
    }

    /** 从 HTML 中提取标签。*/
    fun ResponseScope.tagsFromHtml(): List<Tag> {
        return document.select("ul#tag-sidebar").select("li").mapNotNull { element ->
            val byTag = element.getElementsByTag("a")
            val last = byTag.last()
            if (byTag.isEmpty() && last == null) {
                return@mapNotNull null
            }
            Tag(
                tag = last?.text()?.replace(" ", "_") ?: "",
                category = when (element.className()) {
                    "tag-type-artist" -> TagCategory.Artist
                    "tag-type-copyright" -> TagCategory.Copyright
                    "tag-type-character" -> TagCategory.Character
                    "tag-type-metadata" -> TagCategory.Meta
                    else -> TagCategory.General
                },
                count = element.select("span").text().toIntOrNull() ?: 0
            )
        }
    }


    /** 将 JSON 对象转换为通用的 [Picture]。*/
    private fun BooruPost.toPicture() = Picture(
        id = id,
        width = width,
        height = height,
        parentId = parentId,
        originalUrl = fileUrl.ifBlank {
            "${configure.baseUrl}/images/$directory/$image"
        },
        sampleUrl = sampleUrl.ifBlank {
            "${configure.baseUrl}/samples/$directory/sample_$image"
        },
        thumbnailUrl = previewUrl.ifBlank {
            "${configure.baseUrl}/thumbnails/$directory/thumbnail_${image.replaceExtension("jpg")}"
        },
        sourceUrl = source,
        postUrl = "${configure.baseUrl}/posts/$id",
        md5 = hash,
        tags = tags.split(" ").map { Tag(it) },
        rating = when (rating) {
            "explicit" -> Rating.Explicit
            "questionable" -> Rating.Sensitive
            else -> Rating.Safe
        },
        updatedAt = change,
        createdAt = change,
    )

    /**
     * 解析 Booru 单帖详情结果。
     */
    @Serializable
    private data class BooruPost(
        @SerialName("change")
        val change: Long = 0L,
        @SerialName("comment_count")
        val commentCount: Int = 0,
        @SerialName("directory")
        @Serializable(NumberAsStringSerializer::class)
        val directory: String = "",
        @SerialName("file_url")
        val fileUrl: String = "",
        @SerialName("has_notes")
        val hasNotes: Boolean = false,
        @SerialName("hash")
        val hash: String = "",
        @SerialName("height")
        val height: Int = 0,
        @SerialName("id")
        val id: Int = 0,
        @SerialName("image")
        val image: String = "",
        @SerialName("owner")
        val owner: String = "",
        @SerialName("parent_id")
        val parentId: Int = 0,
        @SerialName("preview_url")
        val previewUrl: String = "",
        @SerialName("rating")
        val rating: String = "",
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
        @SerialName("width")
        val width: Int = 0,
    )

}