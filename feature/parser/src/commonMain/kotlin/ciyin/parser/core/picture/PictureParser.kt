package ciyin.parser.core.picture

import ciyin.io.extension
import ciyin.lang.match
import ciyin.lang.matchIn
import ciyin.parser.core.BaseParser
import ciyin.parser.core.picture.model.Picture
import ciyin.parser.core.picture.model.PictureRequest
import ciyin.parser.core.picture.model.PictureResult
import ciyin.parser.model.FileNameInfo
import ciyin.parser.scope.ParserDsl
import ciyin.parser.scope.ParserScope
import ciyin.parser.util.buildFileName
import kotlin.reflect.KClass

/**
 * 图站解析父类骨架。
 */
abstract class PictureParser : BaseParser<PictureParserType, PictureRequest, PictureResult>() {

    override val defTResult: PictureResult get() = PictureResult()

    /**
     * 将 DSL 泛型类型映射为图站解析类型对象。
     *
     * @param typeClass 类型运行时信息。
     * @return 图站解析类型。
     */
    final override fun resolveType(typeClass: KClass<out PictureParserType>) = when (typeClass) {
        PictureParserType.Home::class -> PictureParserType.Home
        PictureParserType.Posts::class -> PictureParserType.Posts
        PictureParserType.Post::class -> PictureParserType.Post
        PictureParserType.Pools::class -> PictureParserType.Pools
        PictureParserType.Pool::class -> PictureParserType.Pool
        PictureParserType.Popular::class -> PictureParserType.Popular
        else -> error("未支持的 PictureParserType: $typeClass")
    }

    /**
     * 添加图片信息修改。
     *
     * 多次添加图片信息修改，会按添加顺序执行。
     *
     * @param block 图片信息修改。
     */
    @ParserDsl
    fun ParserScope<PictureParserType, PictureRequest, PictureResult>.onItemRevise(block: Picture.() -> Picture?) {

        val revise: Picture.() -> Picture? = {

            val ext = fileExt.ifBlank {
                originalUrl
                    .ifEmpty { sampleUrl }
                    .ifEmpty { thumbnailUrl }
                    .extension
            }

            val name = fileName.ifBlank {
                FileNameInfo(configure.id.site, id, md5, ext).buildFileName()
            }

            //处理图片来源链接和Pixiv id
            val source = if (sourceUrl.matchIn("i\\.pximg\\.net")) {
                val pixivId = sourceUrl.match("(\\d+)_\\w+\\.\\w*$")
                if (pixivId.isNotEmpty()) "https://www.pixiv.net/artworks/$pixivId" else ""
            } else {
                ""
            }

//            val isR18 = isR18(bean)
//            val rating = if (isR18) {
//                Rating.Explicit.ordinal
//            } else {
//                rating
//            }

            copy(
                site = configure.id.site,
                fileName = name,
                fileExt = ext,
                sourceUrl = source,
                children = listOf(name)
            ).block()

        }

        onResultRevise {
            copy(contents = contents.mapNotNull(revise))
        }
    }

}
