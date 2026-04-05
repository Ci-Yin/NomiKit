package ciyin.parser.core.comic

import ciyin.parser.core.BaseParser
import ciyin.parser.core.comic.model.Comic
import ciyin.parser.core.comic.model.ComicRequest
import ciyin.parser.core.comic.model.ComicResult
import ciyin.parser.scope.ParserDsl
import ciyin.parser.util.ComicParserScope

/**
 * 漫画解析父类骨架。
 */
abstract class ComicParser : BaseParser<ComicParserType, ComicRequest, ComicResult>() {

    override val defTResult: ComicResult get() = ComicResult()

    /**
     * 添加漫画信息修改。
     *
     * @param block 漫画信息修改。
     */
    @ParserDsl
    fun ComicParserScope.onItemRevise(block: Comic.() -> Comic) {
        onResultRevise {
            copy(contents = contents.map(block))
        }
    }

}
