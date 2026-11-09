package ciyin.parser.core

import ciyin.parser.model.MultiParserResult
import ciyin.parser.model.Pagination
import ciyin.parser.model.ParserRequest
import ciyin.parser.model.ParserResult
import ciyin.platform.thisLogger
import co.touchlab.kermit.Logger
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlin.coroutines.cancellation.CancellationException

/**
 * 多解析器聚合执行器抽象基类。
 *
 * - 持有一组具体站点解析器 [parsers]；
 * - 根据 [enabledParserIds] 过滤出当前启用的解析器；
 * - 对同一个请求并发执行所有启用的解析器；
 * - 将成功的结果列表交给 [onMerge] 做业务层面的聚合；
 * - 当无可用解析器或全部失败时，通过 [onFallback] 返回兜底结果。
 *
 * @param TType  解析类型密封类，需实现 [ParserType]。
 * @param TRequest  请求模型类型，需实现 [ParserRequest]。
 * @param TResult  解析结果模型类型，需实现 [ParserResult]。
 * @property parsers         可参与聚合解析的具体解析器列表。
 * @property enabledParserIds 当前启用的解析器标识列表，只会对这些 ID 对应的解析器发起请求。
 */
abstract class MultiParser<TType : ParserType, TRequest : ParserRequest, TResult : ParserResult>(
    private val parsers: List<BaseParser<TType, TRequest, TResult>>,
    private val enabledParserIds: List<ParserId>,
) {

    /** 当前聚合器使用的日志记录器实例。*/
    private val logger: Logger = thisLogger()

    /**
     * 将所有成功执行得到的结果列表进行业务层面的聚合。
     *
     * @param request 本次原始解析请求。
     * @param results 所有成功返回的解析结果列表（至少包含一个元素）。
     * @return 聚合后的最终结果。
     */
    protected abstract suspend fun onMerge(
        request: TRequest,
        results: List<TResult>,
        multiResult: MultiParserResult
    ): TResult

    /**
     * 当没有启用的解析器，或者全部解析器执行失败时提供的兜底结果。
     *
     * @return 在异常场景下返回给调用方的安全默认结果。
     */
    protected abstract fun onFallback(): TResult

    /**
     * 对同一请求并发执行所有“启用”的解析器，并合并结果。
     *
     * @param request 本次解析请求。
     * @return 合并后的聚合结果，或在全部失败 / 无可用解析器时返回兜底结果。
     */
    private suspend fun execute(request: TRequest): Pair<Map<ParserId, Throwable>, TResult> {
        val enabledParsers = parsers.filter {
            enabledParserIds.contains(it.configure.id)
        }
        if (enabledParsers.isEmpty()) {
            logger.w { "MultiParser: 启用的解析器列表为空，返回兜底结果。" }
            return emptyMap<ParserId, Throwable>() to fallbackForPage(request.page)
        }

        logger.d { "MultiParser: 开始并发执行 ${enabledParsers.size} 个解析器，启用的解析器列表：${enabledParsers.map { it.configure.id }}。" }

        if (enabledParsers.size > 1 && enabledParsers.any { it.supportsCursorPagination }) {
            val error = IllegalArgumentException(
                "游标分页站点不能与多个站点同时聚合；请按单站点请求继续分页。",
            )
            return enabledParsers.associate { it.configure.id to error } to fallbackForPage(request.page)
        }

        val (errors, successResults) = executeAll(enabledParsers, request)

        if (enabledParsers.size > 1 && successResults.any { it.pagination.nextCursor != null }) {
            val error = IllegalStateException(
                "游标分页不能与多站点聚合混用；请只启用一个支持游标的站点。",
            )
            return enabledParsers.associate { it.configure.id to error } to fallbackForPage(request.page)
        }

        if (successResults.isEmpty()) {
            logger.e { "MultiParser: 所有解析器均执行失败（共 ${errors.size} 个），返回兜底结果。" }
            return errors to fallbackForPage(request.page)
        }

        if (errors.isNotEmpty()) {
            logger.w { "MultiParser: 有 ${errors.size} 个解析器执行失败，${successResults.size} 个成功。" }
        } else {
            logger.d { "MultiParser: 所有解析器均执行成功（共 ${successResults.size} 个）。" }
        }

        val multiResult = MultiParserResult(
            pagination = mergePagination(request.page, successResults.map { it.pagination }),
            tags = successResults.flatMap { it.tags }.distinctBy { it.tag },
        )

        return errors to onMerge(request, successResults, multiResult)
    }

    /**
     * 执行多站点请求并发送聚合成功或失败事件。
     *
     * 没有可用站点时发送带请求页码的兜底成功结果；至少一个站点可用且全部失败时才发送失败事件。
     *
     * @param request 本次解析请求
     * @return 多站点解析事件流
     */
    fun request(request: TRequest): Flow<MultiParserEvent<TResult>> = flow {
        val (errors, result) = execute(request)
        val availableParserCount = parsers.count { enabledParserIds.contains(it.configure.id) }
        if (availableParserCount > 0 && errors.size == availableParserCount) {
            emit(MultiParserEvent.Failure(errors))
        } else {
            emit(MultiParserEvent.Success(result))
        }
    }

    /** 使用请求页码修订聚合器兜底结果。 */
    @Suppress("UNCHECKED_CAST")
    private fun fallbackForPage(page: Int): TResult {
        val fallback = onFallback()
        return fallback.withPagination(fallback.pagination.copy(page = page)) as TResult
    }

    /**
     * 归并多个站点的分页状态。
     *
     * 总页数仅在全部成功站点都明确提供时取最大值；任一站点未知则保持未知，避免提前截断续页。
     * 游标只在归并结果中唯一时保留，
     * 但多站点游标场景会在执行阶段提前拒绝，避免生成无法安全续接的请求。
     */
    private fun mergePagination(page: Int, paginations: List<Pagination>): Pagination {
        require(paginations.isNotEmpty()) { "归并分页状态时至少需要一个解析结果" }
        val knownTotalPages = paginations.mapNotNull { it.totalPages }
        val totalPages = knownTotalPages.takeIf { it.size == paginations.size }?.maxOrNull()
        val cursors = paginations.map { it.nextCursor }.distinct()
        val nextCursor = cursors.singleOrNull()
        val hasNext = when {
            nextCursor != null -> true
            paginations.any { it.hasNext == true } -> true
            paginations.all { it.hasNext == false } -> false
            else -> null
        }
        return Pagination(
            page = page,
            totalPages = totalPages,
            hasNext = hasNext,
            nextCursor = nextCursor,
        )
    }

    private suspend fun executeAll(
        enabledParsers: List<BaseParser<TType, TRequest, TResult>>,
        request: TRequest,
    ): Pair<Map<ParserId, Throwable>, List<TResult>> = coroutineScope {

        // 先取出 id，方便用下标关联
        val parserIds = enabledParsers.map { it.configure.id }

        val deferred = enabledParsers.map { parser ->
            async {
                try {
                    logger.d { "MultiParser: 开始执行解析器：${parser::class.simpleName}" }
                    Result.success(
                        when (val event = parser.request(request).first()) {
                            is ParserEvent.Failure -> error(event.errors)
                            is ParserEvent.Success -> event.result
                        },
                    )
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (throwable: Throwable) {
                    logger.e(throwable) {
                        "MultiParser: 解析器 ${parser::class.simpleName} 执行失败：$throwable"
                    }
                    Result.failure(throwable)
                }
            }
        }

        val results = deferred.awaitAll()

        val errors = mutableMapOf<ParserId, Throwable>()
        val successResults = mutableListOf<TResult>()

        results.forEachIndexed { index, result ->
            val id = parserIds[index]
            if (result.isSuccess) {
                successResults += result.getOrThrow()
            } else {
                result.exceptionOrNull()?.let { errors[id] = it }
            }
        }

        errors to successResults
    }
}

