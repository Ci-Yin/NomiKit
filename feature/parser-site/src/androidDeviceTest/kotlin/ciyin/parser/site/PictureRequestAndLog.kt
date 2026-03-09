package ciyin.parser.site

import ciyin.parser.core.ParserEvent
import ciyin.parser.core.picture.PictureParser
import ciyin.parser.core.picture.PictureParserType
import ciyin.parser.core.picture.model.Picture
import ciyin.parser.core.picture.model.PictureRequest

/**
 * 执行请求并断言返回成功且内容非空。
 */
internal suspend fun PictureParser.requestAndLog(
    request: PictureRequest,
    block: PictureAssert.(Picture) -> Unit = {},
) = request(request).collect { event ->
    PictureAssert {
        when (event) {
            is ParserEvent.Failure -> error(event.errors)
            is ParserEvent.Success -> {
                val result = event.result
                println(
                    "${this::class.simpleName} type=${request.type} \n     request: $request ->\n     result: ok totalPages=${result.totalPages} tags=${result.tags.size} contents=${result.contents.size}",
                )
                println(result)
                when (request.type) {
                    PictureParserType.Posts,
                    PictureParserType.Pools,
                    PictureParserType.Popular,
                        -> result.totalPages.checkBounds("PictureResult.totalPages")

                    else -> {}
                }

                if (request.type == PictureParserType.Posts) {
                    result.tags.checkNotEmpty("PictureResult.tags")
                }
                result.contents.checkNotEmpty("PictureResult.contents")
                result.contents.forEach {
                    block(it.apply { checkBase() })
                    when (request.type) {
                        PictureParserType.Posts,
                        PictureParserType.Post,
                        PictureParserType.Popular,
                            -> it.postUrl.checkUrl("Picture.postUrl")

                        PictureParserType.Pools,
                        PictureParserType.Pool,
                            -> it.poolUrl.checkUrl("Picture.poolUrl")

                        else -> {}
                    }
                }
            }
        }
    }

}


internal class PictureAssert : BaseAssert() {

    companion object {
        operator fun invoke(block: PictureAssert.() -> Unit) = PictureAssert().apply(block)
    }

    internal fun Picture.checkBase() {

        // ---- 基本标识 ----
        fileName.checkFileName("Picture.fileName")
        id.checkBounds("Picture.id")
        fileExt.checkNotEmpty("Picture.fileExt")
        site.checkNotEmpty("Picture.web")
        md5.checkMd5("Picture.md5")

        description.checkTrimmed("Picture.description")

        originalUrl.checkUrl("Picture.originalUrl")
        sampleUrl.checkUrl("Picture.sampleUrl")
        thumbnailUrl.checkUrl("Picture.thumbnailUrl")
        sourceUrl.checkUrlIfNotEmpty("Picture.sourceUrl")

        fileSize.checkBounds("Picture.fileSize")
        width.checkBounds("Picture.width")
        height.checkBounds("Picture.height")
        createdAt.checkBounds("Picture.createdAt")
        updatedAt.checkBounds("Picture.updatedAt")

        tags.checkTags()
        children.checkNotEmpty("Picture.children")

    }


    /**
     * 严格校验 `Picture` 的每一个字段，用于后续按业务拆分更细的断言。
     *
     * 注意：当前工程内的部分解析器尚未完全填充所有字段，直接调用本方法可能导致大量断言失败，
     * 建议在逐步完善解析结果或编写针对性的契约测试时再启用。
     */
    internal fun Picture.assertStrict() {
        checkBase()
    }


}