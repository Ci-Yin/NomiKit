package ciyin.parser.site

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


/**
 * 站点解析器工厂与多解析器构造入口。
 *
 * 提供从站点枚举（[PictureSiteId]、[ComicSiteId]、[MovieSiteId]）到具体解析器
 * 实例（[PictureParser]、[ComicParser]、[MovieParser]）的映射，并封装各业务域下
 * 的多站点聚合解析器构造函数：
 *
 * - [PictureMultiParser]：图片站点聚合解析入口；
 * - [ComicMultiParser]：漫画站点聚合解析入口；
 * - [MovieMultiParser]：番剧 / 影视站点聚合解析入口。
 *
 * 通过这些入口，可以在应用层以站点 ID 列表为维度，快速构建对应的 `MultiParser`
 * 聚合器，而无需关心具体解析器的创建细节。
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2026/3/5 22:06
 */
private fun preview() {}

/**
 * 构建图片领域的多站点聚合解析器。
 *
 * @param siteIds          所有可供选择的图片站点 ID 列表。
 * @param enabledParserIds 本次实际启用的图片站点 ID 列表。
 */
fun PictureMultiParser(
    siteIds: List<PictureSiteId>,
    enabledParserIds: List<PictureSiteId>,
) = PictureMultiParser(siteIds.map { it.factory() }, enabledParserIds)

/**
 * 构建漫画领域的多站点聚合解析器。
 *
 * @param siteIds          所有可供选择的漫画站点 ID 列表。
 * @param enabledParserIds 本次实际启用的漫画站点 ID 列表。
 */
fun ComicMultiParser(
    siteIds: List<ComicSiteId>,
    enabledParserIds: List<ComicSiteId>,
) = ComicMultiParser(siteIds.map { it.factory() }, enabledParserIds)

/**
 * 构建番剧 / 影视领域的多站点聚合解析器。
 *
 * @param siteIds          所有可供选择的番剧 / 影视站点 ID 列表。
 * @param enabledParserIds 本次实际启用的番剧 / 影视站点 ID 列表。
 */
fun MovieMultiParser(
    siteIds: List<MovieSiteId>,
    enabledParserIds: List<MovieSiteId>,
) = MovieMultiParser(siteIds.map { it.factory() }, enabledParserIds)


/**
 * 将图片站点 ID 映射为具体的 [PictureParser] 实例。
 */
fun PictureSiteId.factory(): PictureParser = when (this) {
    PictureSiteId.Danbooru -> DanbooruParser()
    PictureSiteId.Gelbooru -> GelbooruParser()
    PictureSiteId.Rule34 -> TODO()
    PictureSiteId.Yande -> YandeParser()
    PictureSiteId.Konachan -> TODO()
    PictureSiteId.Hypnohub -> HypnohubParser()
    PictureSiteId.Xbooru -> XbooruParser()
    PictureSiteId.Safebooru -> SafebooruParser()
    PictureSiteId.Zerochan -> ZerochanParser()
}

/**
 * 将漫画站点 ID 映射为具体的 [ComicParser] 实例。
 */
fun ComicSiteId.factory(): ComicParser = when (this) {
    ComicSiteId.Hanime -> HanimeComicParser()
    ComicSiteId.Hentai -> TODO()
    ComicSiteId.Nhentai -> TODO()
    ComicSiteId.Mangacopy -> TODO()
    ComicSiteId.Ehentai -> TODO()
}

/**
 * 将番剧 / 影视站点 ID 映射为具体的 [MovieParser] 实例。
 */
fun MovieSiteId.factory(): MovieParser = when (this) {
    MovieSiteId.Hanime -> HanimeMovieParser()
    MovieSiteId.Age -> TODO()
    MovieSiteId.HanimeJav -> TODO()
    MovieSiteId.Qkan8 -> TODO()
}
