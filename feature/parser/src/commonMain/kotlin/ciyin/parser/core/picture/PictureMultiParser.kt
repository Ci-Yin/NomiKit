package ciyin.parser.core.picture

import ciyin.parser.core.MultiParser
import ciyin.parser.core.picture.model.PictureParserId
import ciyin.parser.core.picture.model.PictureRequest
import ciyin.parser.core.picture.model.PictureResult
import ciyin.parser.model.MultiParserResult

/**
 * 图站多站点解析聚合器。
 *
 * 将多个具体的 [PictureParser] 作为子解析器，对同一个 [PictureRequest]
 * 并发发起解析请求，并在所有站点的结果基础上进行去重与合并，
 * 得到一个聚合后的 [PictureResult]。
 *
 * - 站点列表通过构造参数 [parsers] 传入；
 * - 实际参与本次解析的站点由 [enabledParserIds] 控制；
 * - 公共的并发调度与基础统计逻辑由框架层的 [MultiParser] 提供。
 *
 * @param parsers         所有可用的图站解析器列表。
 * @param enabledParserIds 本次启用的站点标识列表，仅这些站点会被请求。
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2026/3/5 22:00
 */
class PictureMultiParser(
    parsers: List<PictureParser>,
    enabledParserIds: List<PictureParserId>,
) : MultiParser<PictureParserType, PictureRequest, PictureResult>(parsers, enabledParserIds) {

    /**
     * 在多个站点的图站解析结果基础上，构造聚合后的 [PictureResult]。
     *
     * - 分页信息与标签来自框架归并后的 [multiResult]；
     * - 内容列表为所有站点返回内容的去重并集（按 `md5` 去重）。
     */
    override suspend fun onMerge(
        request: PictureRequest,
        results: List<PictureResult>,
        multiResult: MultiParserResult,
    ): PictureResult {
        return PictureResult(
            totalPages = multiResult.totalPages,
            tags = multiResult.tags,
            contents = results.flatMap { it.contents }.distinctBy { it.md5 },
        )
    }

    /**
     * 当没有启用站点或所有站点均解析失败时返回的兜底结果。
     */
    override fun onFallback(): PictureResult {
        return PictureResult()
    }
}

