package ciyin.parser.site

import ciyin.parser.core.comic.model.ComicParserId
import ciyin.parser.core.movie.model.MovieParserId
import ciyin.parser.core.picture.model.PictureParserId

/**
 * 图站站点身份枚举。
 */
enum class PictureSiteId(override val site: String) : PictureParserId {
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
 * 图站站点身份枚举。
 */
enum class ComicSiteId(override val site: String) : ComicParserId {
    Hentai("hentai"),
    Nhentai("nhentai"),
    Mangacopy("mangacopy"),
    Ehentai("ehentai"),
    Hanime("hanime1"),
}

/**
 * 图站站点身份枚举。
 */
enum class MovieSiteId(override val site: String) : MovieParserId {
    Hanime("hanime1"),
    Age("age"),
    HanimeJav("hanimejav"),
    Qkan8("qkan8"),
}
