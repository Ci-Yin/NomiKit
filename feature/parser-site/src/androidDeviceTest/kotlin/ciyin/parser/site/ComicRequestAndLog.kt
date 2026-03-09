package ciyin.parser.site

import ciyin.parser.core.ParserEvent
import ciyin.parser.core.comic.ComicParser
import ciyin.parser.core.comic.model.Comic
import ciyin.parser.core.comic.model.ComicChapter
import ciyin.parser.core.comic.model.ComicChapterGroup
import ciyin.parser.core.comic.model.ComicRequest

/**
 * 执行请求并断言返回成功且内容非空。
 */
internal suspend fun ComicParser.requestAndLog(
    request: ComicRequest,
    block: ComicAssert.(Comic) -> Unit = {},
) = request(request).collect { event ->
    ComicAssert {
        when (event) {
            is ParserEvent.Failure -> error(event.errors)
            is ParserEvent.Success -> {
                val result = event.result
                println(
                    "${this::class.simpleName} type=${request.type} \n     request: $request ->\n     result: ok totalPages=${result.totalPages} tags=${result.tags.size} contents=${result.contents.size}",
                )
                println(result)
                result.totalPages.checkBounds("ComicResult.totalPages")
                result.contents.checkNotEmpty("ComicResult.contents")
                result.contents.forEach { block(it.apply { checkBase() }) }
            }
        }
    }
}

internal class ComicAssert : BaseAssert() {

    companion object {
        operator fun invoke(block: ComicAssert.() -> Unit) = ComicAssert().apply(block)
    }

    internal fun Comic.checkBase() {

        // ---- 基本标识 ----
        id.checkNotEmpty("Comic.id")
        title.checkNotEmpty("Comic.title")
        fileName.checkNotEmpty("Comic.fileName")
        site.checkNotEmpty("Comic.web")
        md5.checkMd5("Comic.md5")


        alias.checkTrimmed("Comic.alias")
        description.checkTrimmed("Comic.description")
        region.checkTrimmed("Comic.region")
        restrict.checkTrimmed("Comic.restrict")

        originalUrl.checkUrl("Comic.originalUrl")
        sampleUrl.checkUrl("Comic.sampleUrl")
        thumbUrl.checkUrl("Comic.thumbUrl")
        sourceUrl.checkUrlIfNotEmpty("Comic.sourceUrl")

        fileSize.checkBounds("Comic.fileSize")
        width.checkBounds("Comic.width")
        height.checkBounds("Comic.height")
        createdAt.checkBounds("Comic.createdAt")
        updatedAt.checkBounds("Comic.updatedAt")

        // ---- 标签 ----
        tags.checkTags()

        contents.forEach { it.assertStrictAsComicMedia("Comic.contents") }

        relatedContents.forEach { it.assertStrictAsComicMedia("Comic.relatedContents") }
    }


    /**
     * 严格校验 `Comic` 的每一个字段，用于后续按业务拆分更细的断言。
     *
     * 注意：当前工程内的部分解析器尚未完全填充所有字段，直接调用本方法可能导致大量断言失败，
     * 建议在逐步完善解析结果或编写针对性的契约测试时再启用。
     */
    internal fun Comic.assertStrict() {

        checkBase()

        // ---- 数值范围 ----
        status.checkBounds("Comic.status")
        popularity.checkBounds("Comic.popularity")

        latestChapter.checkChapterStrict("Comic.latestChapter")
        lastUpdatedChapter.checkChapterStrict("Comic.lastUpdatedChapter")

        // ---- 章节分组 & 章节列表 ----
        chapterGroups.checkChapterGroups()

    }

    internal fun List<ComicChapterGroup>.checkChapterGroups() = forEach { group ->
        group.type.checkNotEmpty("ComicChapterGroup.type")
        group.name.checkNotEmpty("ComicChapterGroup.name")
        group.chapters.checkNotEmpty("ComicChapterGroup.chapters")
        group.chapters.forEach { it.checkChapterStrict("chapter") }
    }

    /** 最新章节 / 最后更新章节（如果被填充则校验）*/
    internal fun ComicChapter.checkChapterStrict(fieldPrefix: String) {
        if (id.isNotEmpty() || title.isNotEmpty() || contents.isNotEmpty()) {
            id.checkNotEmpty("$fieldPrefix.id")
            title.checkNotEmpty("$fieldPrefix.title")
            index.checkBounds("$fieldPrefix.index")
            pageCount.checkBounds("$fieldPrefix.pageCount")
            createdAt.checkBounds("$fieldPrefix.createdAt")
        }
    }

}

