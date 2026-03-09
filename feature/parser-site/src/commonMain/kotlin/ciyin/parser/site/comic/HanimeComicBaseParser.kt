package ciyin.parser.site.comic

import ciyin.lang.match
import ciyin.parser.core.comic.ComicParser
import ciyin.parser.core.comic.ComicParserType
import ciyin.parser.core.comic.ComicParserType.Comics
import ciyin.parser.core.comic.model.Comic
import ciyin.parser.core.comic.model.ComicRequest
import ciyin.parser.core.comic.model.ComicResult
import ciyin.parser.core.parametersOf
import ciyin.parser.core.url
import ciyin.parser.model.Media
import ciyin.parser.model.Tag
import ciyin.parser.model.TagCategory
import ciyin.parser.scope.ParserScope
import ciyin.parser.scope.ResponseScope
import ciyin.parser.site.HanimeBaseElement
import ciyin.parser.site.util.toTimestamp
import com.fleeksoft.ksoup.nodes.Element


/**
 *
 * Hanime Comic 基础解析器（新 DSL 版）。
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2026/3/8 15:30
 */
internal abstract class HanimeComicBaseParser : ComicParser(), HanimeBaseElement {

    protected fun ParserScope<ComicParserType, ComicRequest, ComicResult>.setup(
        block: ParserScope<ComicParserType, ComicRequest, ComicResult>.() -> Unit,
    ) {

//        httpClient(MockWeb) {}

        on<Comics> {
            request { req ->
                html { url("/comics/search", req.parameters()) }
            }
            response { result ->
                onComicsParse(result)
            }
        }

        on<ComicParserType.Comic> {

            request { req ->
                html { url("/comic/${req.id}") }
            }

            response { result ->
                onComicParse(result)
            }
        }

        block()
    }

    fun ResponseScope.onComicsParse(result: ComicResult): ComicResult {
        return result.copy(
            totalPages = document.totalPages(),
            contents = document.comics()
        )
    }


    fun ResponseScope.onComicParse(result: ComicResult): ComicResult {

        // 提取 JSON-LD 中的视频信息
        val hanimeContext = document.context()

        logger.d { document.toString() }

        // 获取ID部分
        val id = document.selectFirst("meta[property=og:url]")
            ?.attr("content")
            ?.split("/")
            ?.lastOrNull()
            ?.match("\\d+")

        // 获取<img>标签的src属性值
        val sampleUrl = document.selectFirst("div.col-md-4 > a > img")
            ?.attr("src").orEmpty()

        // 获取原图链接
        val originalUrl = sampleUrl.replace("t2.nhentai", "i2.nhentai")
            .replace("/cover.", "/1.")

        // 获取标签
        val tags = document.selectFirst("div.comics-metadata-margin-top")?.let { element ->

            val categoryMap = mapOf(
                "作者" to TagCategory.Artist,
                "社團" to TagCategory.Group,
                "同人" to TagCategory.Copyright,
                "角色" to TagCategory.Character,
                "標籤" to TagCategory.General,
                "語言" to TagCategory.Language,
                "分類" to TagCategory.Meta
            )

            categoryMap.flatMap { (title, category) ->
                element.select("h5:contains($title) a.hover-lighter div").map {
                    Tag(tag = it.text(), category = category)
                }
            }

        } ?: emptyList()

        // 获取漫画内容
        val contents = document.selectFirst(
            "div.comics-panel-margin.comics-panel-margin-top.comics-panel-padding.comics-thumbnail-wrapper.comic-rows-wrapper"
        )
            ?.select("img")
            ?.map {
                val srcset = it.attr("data-srcset").trim()
                val regex =
                    Regex("""https?://t2\.nhentai\.net(/galleries/\d+)/(\d+)t\.(\w+)""")
                val url = srcset.replace(regex, "https://i2.nhentai.net\$1/\$2.\$3")
                Media(
                    width = 1280,
                    height = 1816,
                    //thumbUrl = sampleUrl.replace("/cover.", "/${i}t."),
                    thumbUrl = url,
                    sampleUrl = url,
                    originalUrl = url,
                )
            } ?: emptyList()

        // 获取更多相关列表
//        val mores += document.comics()
        return if (id != null) {
            val comic = Comic(
                id = id,
                width = 1280,
                height = 1816,
                title = hanimeContext.name,
                description = hanimeContext.description,
                thumbUrl = hanimeContext.thumbnailUrl.firstOrNull() ?: "",
                sampleUrl = sampleUrl,
                originalUrl = originalUrl,
                sourceUrl = hanimeContext.contentUrl,
                tags = tags,
                contents = contents,
                updatedAt = hanimeContext.uploadDate.toTimestamp(),
            )
            result.copy(contents = listOf(comic))
        } else {
            result
        }
    }

    private fun ComicRequest.parameters(): MutableMap<String, Any> = parametersOf(
        "page" to page,
        "sort" to "popular-today",
        "query" to tags.formatTags()
    )

    fun Element.comics(
        cssQuery: String = "div.comic-rows-videos-div",
    ): List<Comic> = select(cssQuery).map { element ->

        // 获取链接
        val postUrl = element.selectFirst("a")?.attr("href") ?: ""

        // 获取ID部分
        val id = postUrl.split("/").lastOrNull()?.match("\\d+").orEmpty()

        // 获取图片src
        val thumbUrl = element.selectFirst("img")?.attr("data-srcset") ?: ""

        val originalUrl = thumbUrl.replace("t2.nhentai", "i2.nhentai").replace("/cover", "/1")

        // 获取漫画标题
        val name = element.selectFirst(".comic-rows-videos-title")?.text() ?: ""
        Comic(
            width = 1280,
            height = 1816,
            id = id,
            thumbUrl = thumbUrl,
            sampleUrl = originalUrl,
            originalUrl = originalUrl,
            title = name
        )
    }

}

