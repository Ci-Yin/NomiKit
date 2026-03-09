package ciyin.parser.core.comic

import ciyin.parser.core.MultiParser
import ciyin.parser.core.comic.model.ComicParserId
import ciyin.parser.core.comic.model.ComicRequest
import ciyin.parser.core.comic.model.ComicResult
import ciyin.parser.model.MultiParserResult

/**
 * 漫画多站点解析聚合器。
 *
 * 将多个具体的 [ComicParser] 作为子解析器，对同一个 [ComicRequest]
 * 并发发起解析请求，并在所有站点的结果基础上进行去重与合并，
 * 得到一个聚合后的 [ComicResult]。
 *
 * - 站点列表通过构造参数 [parsers] 传入；
 * - 实际参与本次解析的站点由 [enabledParserIds] 控制；
 * - 公共的并发调度逻辑由框架层的 [MultiParser] 提供。
 *
 * @param parsers         所有可用的漫画站点解析器列表。
 * @param enabledParserIds 本次启用的站点标识列表，仅这些站点会被请求。
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2026/3/5 21:01
 */
class ComicMultiParser(
    parsers: List<ComicParser>,
    enabledParserIds: List<ComicParserId>,
) : MultiParser<ComicParserType, ComicRequest, ComicResult>(parsers, enabledParserIds) {

    /**
     * 在多个站点的漫画解析结果基础上，构造聚合后的 [ComicResult]。
     *
     * - 分页信息与标签来自框架归并后的 [multiResult]；
     * - 内容列表为所有站点返回内容的去重并集（按 `md5` 去重）。
     */
    override suspend fun onMerge(
        request: ComicRequest,
        results: List<ComicResult>,
        multiResult: MultiParserResult,
    ): ComicResult {
        return ComicResult(
            totalPages = multiResult.totalPages,
            tags = multiResult.tags,
            contents = results.flatMap { it.contents }.distinctBy { it.md5 },
        )
    }

    /**
     * 当没有启用站点或所有站点均解析失败时返回的兜底结果。
     */
    override fun onFallback(): ComicResult {
        return ComicResult()
    }
}