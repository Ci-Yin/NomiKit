package ciyin.parser.site

import ciyin.parser.core.comic.model.ComicParserId
import ciyin.parser.core.movie.model.MovieParserId
import ciyin.parser.core.picture.model.PictureParserId

/**
 * 图站站点身份枚举。
 *
 * 未实现站点仍保留在枚举中，便于持久化配置和后续接入；调用工厂时会抛出明确错误。
 *
 * @property site 稳定的站点标识字符串
 */
enum class PictureSiteId(
    override val site: String,
) : PictureParserId {
    Danbooru("danbooru"),
    Gelbooru("gelbooru"),
    Rule34("rule34"),
    Yande("yande"),
    Konachan("konachan"),
    Hypnohub("hypnohub"),
    Xbooru("xbooru"),
    Safebooru("safebooru"),
    Zerochan("zerochan"),
}

/**
 * 漫画站点身份枚举。
 *
 * 未实现站点仍保留在枚举中，便于持久化配置和后续接入；调用工厂时会抛出明确错误。
 *
 * @property site 稳定的站点标识字符串
 */
enum class ComicSiteId(
    override val site: String,
) : ComicParserId {
    Hentai("hentai"),
    Nhentai("nhentai"),
    Mangacopy("mangacopy"),
    Ehentai("ehentai"),
    Hanime("hanime1"),
}

/**
 * 影视站点身份枚举。
 *
 * 未实现站点仍保留在枚举中，便于持久化配置和后续接入；调用工厂时会抛出明确错误。
 *
 * @property site 稳定的站点标识字符串
 */
enum class MovieSiteId(
    override val site: String,
) : MovieParserId {
    Hanime("hanime1"),
    Age("age"),
    HanimeJav("hanimejav"),
    Qkan8("qkan8"),
}
