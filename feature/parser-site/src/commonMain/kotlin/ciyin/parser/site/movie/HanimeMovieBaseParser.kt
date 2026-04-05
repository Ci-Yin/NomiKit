package ciyin.parser.site.movie

import ciyin.lang.match
import ciyin.parser.core.movie.MovieParser
import ciyin.parser.core.movie.MovieParserType
import ciyin.parser.core.movie.MovieParserType.Movies
import ciyin.parser.core.movie.model.Movie
import ciyin.parser.core.movie.model.MovieResult
import ciyin.parser.core.movie.model.Video
import ciyin.parser.core.parametersOf
import ciyin.parser.core.url
import ciyin.parser.model.Tag
import ciyin.parser.model.TagCategory
import ciyin.parser.scope.ResponseScope
import ciyin.parser.site.HanimeBaseElement
import ciyin.parser.site.util.toTimestamp
import ciyin.parser.util.MovieParserScope
import com.fleeksoft.ksoup.nodes.Element


/**
 *
 * Hanime Movie 基础解析器（新 DSL 版）。
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2026/3/8 15:30
 */
abstract class HanimeMovieBaseParser : MovieParser(), HanimeBaseElement {

    protected fun MovieParserScope.setup(
        block: MovieParserScope.() -> Unit,
    ) {

//        httpClient(MockWeb) {}

        on(Movies) {
            request { req ->
                val parameters = parametersOf(
                    "page" to req.page,
                    "query" to req.tags.formatTags(),
                    "sort" to "popular-today",
                    "type" to "",
                    //"genre" to "泡麵番",
                    "sort" to "",
                    "year" to "",
                    "month" to ""
                )
                html { url("search", parameters) }
            }
            response { result ->
                onMoviesParse(result)
            }
        }

        on(MovieParserType.Movie) {

            request { req ->
                html { url("watch", parametersOf("v" to req.id)) }
            }

            response { result ->
                onMovieParse(result)
            }
        }

        block()
    }

    fun ResponseScope.onMoviesParse(result: MovieResult): MovieResult {
        val contents = mutableListOf<Movie>()

        document.select(
            "div.row.no-gutter div.col-xs-6.col-sm-4.col-md-2.search-doujin-videos.hidden-xs.hover-lighter.multiple-link-wrapper"
        ).forEach { element ->

            // 提取图片链接
            val coverUrl = element.select("div.card-mobile-panel img[src*=thumbnail]")
                .attr("src")

            contents += Movie(
                id = element.movieId(),
                key = coverUrl.hashCode().toString(),
                name = element.title(),
                coverUrl = coverUrl,
                playCount = element.count(),
                duration = element.duration(),
                width = 640,
                height = 360,
            )

        }

        document.select("div.home-rows-videos-wrapper a").forEach { element ->

            // 提取<img>标签的src属性值
            val coverUrl = element.select("div.home-rows-videos-div.search-videos img")
                .attr("src")

            contents += Movie(
                id = element.movieId(),
                key = coverUrl.hashCode().toString(),
                name = element.title("div.home-rows-videos-title"),
                coverUrl = coverUrl,
                width = 360,
                height = 530,
            )
        }

        return result.copy(
            totalPages = document.totalPages(),
            contents = contents
        )
    }


    fun ResponseScope.onMovieParse(result: MovieResult): MovieResult {

        // 提取 JSON-LD 中的视频信息
        val hanimeContext = document.context()

        logger.d { document.toString() }

        // 获取ID部分
        val id = document.movieId("link[rel=canonical]")

        // 提取视频时长
        val duration = document.duration()

        // 提取网站链接
        val sourceUrl = responseForHtml().request.url.toString()

        // 提取 video 标签中的视频链接及其对应的 size
        val videos = document.select("video source").map {
            Video(
                url = it.attr("src"),
                size = it.attr("size").toIntOrNull() ?: 0
            )
        }

        // 获取标签
        val tags = document.selectFirst("div.comics-metadata-margin-top")
            ?.select("div.single-video-tag a[href]")
            ?.asSequence()
            ?.map {
                it.text()
                    .trim()
                    .replace(Regex("\\(\\d*\\)"), "")
                    .replace(Regex("^# "), "")
            }
            ?.filter { it.isNotBlank() }
            ?.map { Tag(tag = it, category = TagCategory.General) }
            ?.toList()
            ?: emptyList()

        // 提取播放列表的相关信息
        val playlist = document.select(
            "div.hidden-xs.hidden-sm div.hover-video-playlist div.related-watch-wrap"
        )
            .reversed()
            .map { element ->
                // 提取视频ID
                val id = element
                    .select("a.overlay")
                    .attr("href")
                    .match("\\?v=(\\d+)")

                Movie(
                    id = id,

                    // 提取标题
                    name = element.title(),

                    // 提取视频播放次数并转换为 Long 类型
                    playCount = element.count(),

                    // 提取视频时长并转换为 Long 类型（单位：秒）
                    duration = element.duration(),

                    // 提取封面图片链接
                    coverUrl = element.select("img[src*=thumbnail]").attr("src"),

                    site = configure.id.site
                )

            }

        return if (id.isNotBlank()) {
            val movie = Movie(
                id = id,
                width = 1280,
                height = 1816,
                name = hanimeContext.name,
                description = hanimeContext.description,
                coverUrl = hanimeContext.thumbnailUrl.firstOrNull() ?: "",
                sourceUrl = sourceUrl,
                playlist = playlist,
                videos = videos,
                playCount = hanimeContext.interactionStatistic.userInteractionCount,
                duration = duration,
                tags = tags,
                updatedAt = hanimeContext.uploadDate.toTimestamp(),
            )
            result.copy(contents = listOf(movie))
        } else {
            result
        }
    }

    /**
     * 提取视频标题
     */
    fun Element.movieId(cssQuery: String = "a"): String {
        return select(cssQuery)
            .attr("href")
            .match("\\?v=(\\d+)")
    }

}

