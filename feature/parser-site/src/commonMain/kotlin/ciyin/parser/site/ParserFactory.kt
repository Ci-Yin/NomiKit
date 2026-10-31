package ciyin.parser.site

import ciyin.parser.core.ParserId
import ciyin.parser.core.comic.ComicMultiParser
import ciyin.parser.core.comic.ComicParser
import ciyin.parser.core.movie.MovieMultiParser
import ciyin.parser.core.movie.MovieParser
import ciyin.parser.core.picture.PictureMultiParser
import ciyin.parser.core.picture.PictureParser
import ciyin.parser.site.comic.HanimeComicParser
import ciyin.parser.site.movie.HanimeMovieParser
import ciyin.parser.site.picture.DanbooruParser
import ciyin.parser.site.picture.GelbooruParser
import ciyin.parser.site.picture.HypnohubParser
import ciyin.parser.site.picture.SafebooruParser
import ciyin.parser.site.picture.XbooruParser
import ciyin.parser.site.picture.YandeParser
import ciyin.parser.site.picture.ZerochanParser
import kotlin.collections.mapNotNull
import kotlin.jvm.JvmName


/**
 * 构建图片领域的多站点聚合解析器。
 *
 * @param siteIds          所有可供选择的图片站点 ID 列表。
 * @param enabledSiteIds 本次实际启用的图片站点 ID 列表。
 */
fun PictureMultiParser(
    siteIds: List<PictureSiteId>,
    enabledSiteIds: List<PictureSiteId>,
) = PictureMultiParser(siteIds.map { it.factory() }, enabledSiteIds)

/**
 * 构建图片领域的多站点聚合解析器。
 *
 * @param siteIds          所有可供选择的图片站点 ID 列表。
 * @param enabledSiteIds 本次实际启用的图片站点 ID 列表。
 */
@JvmName("PictureMultiParserEx")
fun PictureMultiParser(
    siteIds: List<ParserId>,
    enabledSiteIds: List<ParserId>,
) = PictureMultiParser(
    siteIds = siteIds.asSites(PictureSiteId.entries),
    enabledSiteIds = enabledSiteIds.asSites(PictureSiteId.entries),
)

/**
 * 构建漫画领域的多站点聚合解析器。
 *
 * @param siteIds          所有可供选择的漫画站点 ID 列表。
 * @param enabledSiteIds 本次实际启用的漫画站点 ID 列表。
 */
fun ComicMultiParser(
    siteIds: List<ComicSiteId>,
    enabledSiteIds: List<ComicSiteId>,
) = ComicMultiParser(siteIds.map { it.factory() }, enabledSiteIds)


/**
 * 从通用解析器 ID 构建漫画领域的多站点聚合解析器。
 *
 * @param siteIds 所有可供选择的漫画站点 ID 列表
 * @param enabledSiteIds 本次实际启用的漫画站点 ID 列表
 */
@JvmName("ComicMultiParserEx")
fun ComicMultiParser(
    siteIds: List<ParserId>,
    enabledSiteIds: List<ParserId>,
) = ComicMultiParser(
    siteIds = siteIds.asSites(ComicSiteId.entries),
    enabledSiteIds = enabledSiteIds.asSites(ComicSiteId.entries),
)
/**
 * 构建番剧 / 影视领域的多站点聚合解析器。
 *
 * @param siteIds          所有可供选择的番剧 / 影视站点 ID 列表。
 * @param enabledSiteIds 本次实际启用的番剧 / 影视站点 ID 列表。
 */
fun MovieMultiParser(
    siteIds: List<MovieSiteId>,
    enabledSiteIds: List<MovieSiteId>,
) = MovieMultiParser(siteIds.map { it.factory() }, enabledSiteIds)

/**
 * 从通用解析器 ID 构建影视领域的多站点聚合解析器。
 *
 * @param siteIds 所有可供选择的影视站点 ID 列表
 * @param enabledSiteIds 本次实际启用的影视站点 ID 列表
 */
@JvmName("MovieMultiParserEx")
fun MovieMultiParser(
    siteIds: List<ParserId>,
    enabledSiteIds: List<ParserId>,
) = MovieMultiParser(
    siteIds = siteIds.asSites(MovieSiteId.entries),
    enabledSiteIds = enabledSiteIds.asSites(MovieSiteId.entries),
)

private inline fun <reified T : ParserId> List<ParserId>.asSites(entries: List<T>): List<T> {
    val pictureSiteIds = filterIsInstance<T>()
    return when (pictureSiteIds.size) {
        size -> pictureSiteIds
        else -> mapNotNull { site -> entries.find { it.site == site.site } }
    }
}

/**
 * 将图片站点 ID 映射为具体的 [PictureParser] 实例。
 */
fun PictureSiteId.factory(): PictureParser = when (this) {
    PictureSiteId.Danbooru -> DanbooruParser()
    PictureSiteId.Gelbooru -> GelbooruParser()
    PictureSiteId.Rule34 -> unimplementedParser()
    PictureSiteId.Yande -> YandeParser()
    PictureSiteId.Konachan -> unimplementedParser()
    PictureSiteId.Hypnohub -> HypnohubParser()
    PictureSiteId.Xbooru -> XbooruParser()
    PictureSiteId.Safebooru -> SafebooruParser()
    PictureSiteId.Zerochan -> ZerochanParser()
}

private fun PictureSiteId.unimplementedParser(): Nothing = error(
    "图片站点 $site 尚未实现解析器"
)

/**
 * 将漫画站点 ID 映射为具体的 [ComicParser] 实例。
 */
fun ComicSiteId.factory(): ComicParser = when (this) {
    ComicSiteId.Hanime -> HanimeComicParser()
    ComicSiteId.Hentai,
    ComicSiteId.Nhentai,
    ComicSiteId.Mangacopy,
    ComicSiteId.Ehentai -> unimplementedParser()
}

private fun ComicSiteId.unimplementedParser(): Nothing = error(
    "漫画站点 $site 尚未实现解析器"
)

/**
 * 将番剧 / 影视站点 ID 映射为具体的 [MovieParser] 实例。
 */
fun MovieSiteId.factory(): MovieParser = when (this) {
    MovieSiteId.Hanime -> HanimeMovieParser()
    MovieSiteId.Age,
    MovieSiteId.HanimeJav,
    MovieSiteId.Qkan8 -> unimplementedParser()
}

private fun MovieSiteId.unimplementedParser(): Nothing = error(
    "影视站点 $site 尚未实现解析器"
)
