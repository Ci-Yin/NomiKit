package ciyin.parser.core.comic

import ciyin.parser.core.BaseParser
import ciyin.parser.core.comic.model.Comic
import ciyin.parser.core.comic.model.ComicRequest
import ciyin.parser.core.comic.model.ComicResult
import ciyin.parser.scope.ParserDsl
import ciyin.parser.scope.ParserScope
import kotlin.reflect.KClass

/**
 * 漫画解析父类骨架。
 */
abstract class ComicParser : BaseParser<ComicParserType, ComicRequest, ComicResult>() {

    override val defTResult: ComicResult get() = ComicResult()

    /**
     * 将 DSL 泛型类型映射为漫画解析类型对象。
     *
     * @param typeClass 类型运行时信息。
     * @return 漫画解析类型。
     */
    final override fun resolveType(typeClass: KClass<out ComicParserType>) = when (typeClass) {
        ComicParserType.Home::class -> ComicParserType.Home
        ComicParserType.Comics::class -> ComicParserType.Comics
        ComicParserType.Comic::class -> ComicParserType.Comic
        ComicParserType.Chapter::class -> ComicParserType.Chapter
        else -> error("未支持的 ComicParserType: $typeClass")
    }

    /**
     * 添加漫画信息修改。
     *
     * @param block 漫画信息修改。
     */
    @ParserDsl
    fun ParserScope<ComicParserType, ComicRequest, ComicResult>.onItemRevise(block: Comic.() -> Comic) {
        onResultRevise {
            copy(contents = contents.map(block))
        }
    }

}
