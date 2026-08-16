package ciyin.parser.site.picture

import ciyin.parser.core.picture.PictureParserType
import ciyin.parser.core.picture.model.Picture
import ciyin.parser.core.picture.model.PictureRequest
import ciyin.parser.core.picture.model.PoolSummary
import ciyin.parser.model.Rating
import com.fleeksoft.ksoup.Ksoup
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Clock

/** 支持的热门榜单时间范围。 */
private val SupportedPopularScales = setOf("day", "week", "month")

/**
 * 单个图片站点请求的路径与查询参数计划。
 *
 * @property htmlPath HTML 请求路径
 * @property jsonPath JSON 请求路径；不需要时为 `null`
 * @property parameters 两类请求共享的查询参数
 */
internal data class PictureSiteRequestPlan(
    val htmlPath: String,
    val jsonPath: String?,
    val parameters: Map<String, Any>,
)

/**
 * 构建 Danbooru 图片列表请求计划。
 *
 * @param request 图片请求
 * @return 与请求类型严格对应的路径与参数
 */
internal fun danbooruPictureRequestPlan(request: PictureRequest): PictureSiteRequestPlan =
    when (request.type) {
        PictureParserType.Posts -> PictureSiteRequestPlan(
            htmlPath = "/posts",
            jsonPath = "/posts.json",
            parameters = mapOf(
                "page" to request.page,
                "tags" to request.tags.joinToString(","),
            ),
        )

        PictureParserType.Popular -> PictureSiteRequestPlan(
            htmlPath = "/explore/posts/popular",
            jsonPath = "/explore/posts/popular.json",
            parameters = mapOf(
                "page" to request.page,
                "scale" to request.requireSupportedPopularScale(),
            ),
        )

        PictureParserType.Pools -> PictureSiteRequestPlan(
            htmlPath = "/pools/gallery",
            jsonPath = "/pools/gallery.json",
            parameters = mapOf(
                "page" to request.page,
                "search[name_matches]" to request.search,
            ),
        )

        else -> error("Danbooru 列表请求计划不支持类型：${request.type}")
    }

/**
 * 构建 Yande 图片列表请求计划。
 *
 * @param request 图片请求
 * @param today 当前系统日期；调用方可显式传入以生成确定请求
 * @return 与请求类型严格对应的路径与参数
 */
internal fun yandePictureRequestPlan(
    request: PictureRequest,
    today: LocalDate = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date,
): PictureSiteRequestPlan =
    when (request.type) {
        PictureParserType.Posts -> PictureSiteRequestPlan(
            htmlPath = "/post",
            jsonPath = null,
            parameters = mapOf(
                "page" to request.page,
                "tags" to request.tags.joinToString(" "),
            ),
        )

        PictureParserType.Popular -> {
            val scale = request.requireSupportedPopularScale()
            val pageOffset = (request.page.coerceAtLeast(1) - 1)
            val targetDate = today.minus(
                when (scale) {
                    "day" -> DatePeriod(days = pageOffset)
                    "week" -> DatePeriod(days = pageOffset * 7)
                    "month" -> DatePeriod(months = pageOffset)
                    else -> error("已校验的热门范围无法映射：$scale")
                }
            )
            PictureSiteRequestPlan(
                htmlPath = "/post/popular_by_$scale",
                jsonPath = "/post/popular_by_$scale.json",
                parameters = mapOf(
                    "year" to targetDate.year,
                    "month" to targetDate.month.number,
                    "day" to targetDate.day,
                ),
            )
        }

        PictureParserType.Pools -> PictureSiteRequestPlan(
            htmlPath = "/pool",
            jsonPath = "/pool.json",
            parameters = mapOf(
                "page" to request.page,
                "query" to request.search,
            ),
        )

        else -> error("Yande 列表请求计划不支持类型：${request.type}")
    }

/** 返回经过显式支持校验的热门时间范围。 */
private fun PictureRequest.requireSupportedPopularScale(): String =
    scale.also { value ->
        require(value in SupportedPopularScales) {
            "不支持的热门时间范围：$value"
        }
    }

/**
 * 解析 Danbooru 画集列表并按 pool ID 合并 HTML 封面与 JSON 摘要。
 *
 * JSON 是条目集合和标题、数量的权威来源；HTML 只提供首图。没有首图的画集仍保留，
 * 让上层能够明确展示摘要而不是静默丢失条目。
 *
 * @param html 画集页面 HTML
 * @param json 画集摘要 JSON
 * @param baseUrl 站点基础地址
 * @return 一项对应一个 pool 的图片承载模型
 */
internal fun parseDanbooruPools(
    html: String,
    json: String,
    baseUrl: String,
): List<Picture> {
    val covers = Ksoup.parse(html)
        .select("div.posts-container article.post-preview")
        .mapNotNull { element ->
            val link = element.selectFirst("a.post-preview-link") ?: return@mapNotNull null
            val poolId = link.attr("href").substringAfterLast('/').toIntOrNull()
                ?: return@mapNotNull null
            val image = link.selectFirst("img")
            poolId to PoolCover(
                postId = element.attr("data-id").toIntOrNull() ?: 0,
                thumbnailUrl = image?.attr("src").orEmpty(),
                width = image?.attr("width")?.toIntOrNull() ?: 0,
                height = image?.attr("height")?.toIntOrNull() ?: 0,
            )
        }
        .toMap()
    val pools = decodePoolSummaries<DanbooruPoolPayload>(json)
    return pools.map { pool ->
        val cover = covers[pool.id]
        val url = "$baseUrl/pools/${pool.id}"
        Picture(
            id = cover?.postId?.takeIf { it > 0 } ?: pool.id,
            name = pool.name,
            site = "danbooru",
            thumbnailUrl = cover?.thumbnailUrl.orEmpty(),
            poolUrl = url,
            poolId = pool.id,
            width = cover?.width ?: 0,
            height = cover?.height ?: 0,
            poolSummary = PoolSummary(
                poolId = pool.id,
                title = pool.name,
                postCount = pool.postCount,
                url = url,
            ),
        )
    }
}

/**
 * 解析 Yande 画集列表并按页面顺序合并 HTML 首图与 JSON 摘要。
 *
 * Yande 的列表 HTML 没有为内嵌 `Post.register` 标注 pool ID，因此 JSON 顺序是条目权威，
 * 对应位置的首图只作为可空封面。JSON 条目不会因封面缺失而丢失。
 *
 * @param html 画集页面 HTML
 * @param json 画集摘要 JSON
 * @param baseUrl 站点基础地址
 * @return 一项对应一个 pool 的图片承载模型
 */
internal fun parseYandePools(
    html: String,
    json: String,
    baseUrl: String,
): List<Picture> {
    val covers = Ksoup.parse(html)
        .select("script")
        .flatMap { script -> script.data().extractFunctionArguments("Post.register") }
        .map { payload -> PoolJson.decodeFromString<YandeCoverPayload>(payload) }
    val pools = decodePoolSummaries<YandePoolPayload>(json)
    return pools.mapIndexed { index, pool ->
        val cover = covers.getOrNull(index)
        val url = "$baseUrl/pool/show/${pool.id}"
        Picture(
            fileName = cover?.md5.orEmpty(),
            id = cover?.id?.takeIf { it > 0 } ?: pool.id,
            name = pool.name,
            site = "yande",
            originalUrl = cover?.fileUrl.orEmpty(),
            sampleUrl = cover?.sampleUrl.orEmpty(),
            thumbnailUrl = cover?.previewUrl.orEmpty(),
            postUrl = cover?.id?.takeIf { it > 0 }?.let { "$baseUrl/post/show/$it" }.orEmpty(),
            poolUrl = url,
            poolId = pool.id,
            fileExt = cover?.fileExt.orEmpty(),
            fileSize = cover?.fileSize ?: 0L,
            md5 = cover?.md5.orEmpty(),
            width = cover?.width ?: 0,
            height = cover?.height ?: 0,
            rating = when (cover?.rating) {
                "q" -> Rating.Sensitive
                "e" -> Rating.Explicit
                else -> Rating.Safe
            },
            poolSummary = PoolSummary(
                poolId = pool.id,
                title = pool.name,
                postCount = pool.postCount,
                url = url,
            ),
        )
    }
}

/** 解析 JSON 画集摘要数组，空响应对应空列表。 */
private inline fun <reified T> decodePoolSummaries(json: String): List<T> =
    if (json.isBlank()) emptyList() else PoolJson.decodeFromString(json)

/** 从 JavaScript 文本提取指定函数的一组参数。 */
private fun String.extractFunctionArguments(functionName: String): List<String> {
    val results = mutableListOf<String>()
    val key = "$functionName("
    var searchFrom = 0
    while (searchFrom < length) {
        val start = indexOf(key, searchFrom)
        if (start < 0) break
        val argumentStart = start + key.length
        var depth = 1
        var cursor = argumentStart
        while (cursor < length && depth > 0) {
            when (this[cursor]) {
                '(' -> depth++
                ')' -> depth--
            }
            cursor++
        }
        if (depth == 0) {
            results += substring(argumentStart, cursor - 1)
        }
        searchFrom = cursor.coerceAtLeast(argumentStart + 1)
    }
    return results
}

/** 首图的最小布局信息。 */
private data class PoolCover(
    val postId: Int,
    val thumbnailUrl: String,
    val width: Int,
    val height: Int,
)

/** Danbooru 画集摘要响应。 */
@Serializable
private data class DanbooruPoolPayload(
    val id: Int,
    val name: String,
    @SerialName("post_count")
    val postCount: Int? = null,
)

/** Yande 画集摘要响应。 */
@Serializable
private data class YandePoolPayload(
    val id: Int,
    val name: String,
    @SerialName("post_count")
    val postCount: Int? = null,
)

/** Yande 页面内嵌首图响应的最小字段集。 */
@Serializable
private data class YandeCoverPayload(
    val id: Int = 0,
    @SerialName("file_url")
    val fileUrl: String = "",
    @SerialName("sample_url")
    val sampleUrl: String = "",
    @SerialName("preview_url")
    val previewUrl: String = "",
    @SerialName("file_ext")
    val fileExt: String = "",
    @SerialName("file_size")
    val fileSize: Long = 0L,
    val md5: String = "",
    val width: Int = 0,
    val height: Int = 0,
    val rating: String = "s",
)

/** 宽容读取真实站点可能追加字段的 JSON 配置。 */
private val PoolJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
}
