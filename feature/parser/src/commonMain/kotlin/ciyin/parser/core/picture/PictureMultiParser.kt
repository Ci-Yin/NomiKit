package ciyin.parser.core.picture

import ciyin.parser.core.MultiParser
import ciyin.parser.core.picture.model.Picture
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
     * - 画集列表按“站点 + pool ID”去重；其他图片列表继续按 `md5` 去重。
     */
    override suspend fun onMerge(
        request: PictureRequest,
        results: List<PictureResult>,
        multiResult: MultiParserResult,
    ): PictureResult {
        return PictureResult(
            totalPages = multiResult.totalPages,
            tags = multiResult.tags,
            contents = results.flatMap { it.contents }.let { contents ->
                when (request.type) {
                    PictureParserType.Pools -> contents.distinctBy(Picture::requirePoolMergeIdentity)
                    else -> contents.distinctBy(Picture::md5)
                }
            },
        )
    }

    /**
     * 当没有启用站点或所有站点均解析失败时返回的兜底结果。
     */
    override fun onFallback(): PictureResult {
        return PictureResult()
    }
}

/**
 * 返回画集聚合使用的稳定业务身份。
 *
 * Pools 响应必须同时提供站点和正数 pool ID；缺失时直接失败，避免回退空 md5 后静默丢条目。
 *
 * @return 规范化站点与 pool ID 组成的身份
 */
private fun Picture.requirePoolMergeIdentity(): String {
    val normalizedSite = site.trim().lowercase()
    val poolId = requireNotNull(poolSummary) {
        "画集聚合条目缺少 PoolSummary：$this"
    }.poolId
    require(normalizedSite.isNotEmpty() && poolId > 0) {
        "画集聚合条目缺少合法站点或 pool ID：$this"
    }
    return "$normalizedSite:$poolId"
}

