package ciyin.ai.facade.internal

import ciyin.ai.core.capability.AiCapability
import ciyin.ai.core.engine.EngineId
import ciyin.ai.core.error.AiEngineError
import ciyin.ai.facade.observability.AiInvocationListener
import ciyin.ai.facade.observability.InvocationMetadata
import ciyin.ai.facade.selection.FallbackPolicy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlin.time.TimeSource

/**
 * 一次"选定的引擎尝试"——封装"调用什么引擎、用什么 model、产出什么 Flow"三件事。
 *
 * 与具体能力（Chat / Image）解耦：调用方提供 [stream] 即可，所以 Chat 与 Image 共用同一套 fallback 调度逻辑。
 *
 * @param E 引擎类型（[ciyin.ai.core.engine.ChatEngine] 或 [ciyin.ai.core.engine.ImageEngine]）。
 * @param V 事件类型（[ciyin.ai.core.chat.ChatEvent] 或 [ciyin.ai.core.image.ImageEvent]）。
 * @property engine 本次尝试要调用的引擎实例。
 * @property model 本次尝试使用的模型名（仅用于 metadata；具体如何传入 request 由调用方在 [stream] 内部完成）。
 * @property stream 实际发起调用并返回事件 Flow 的函数。
 */
internal class EngineAttempt<E, V>(
    val engine: E,
    val model: String?,
    val stream: () -> Flow<V>,
)

/**
 * Facade 通用 fallback 调度器。
 *
 * 行为约定：
 * 1. 按 [attempts] 顺序逐个尝试；首个永远是"主引擎"；
 * 2. 单个引擎内最多 `1 + policy.maxRetries` 次调用（含首次）；
 * 3. 同一引擎内的失败若不命中 [FallbackPolicy.triggerOn]，立即停止重试并跳出（**不**再切下一个引擎）；
 * 4. 单引擎彻底失败后，若错误类型命中 [FallbackPolicy.triggerOn]，才切到下一个引擎；
 * 5. 全部失败时，**透传最后一次的失败事件**给下游（已经被下游收到的部分事件不撤销）；
 *    事件在引擎产出时**立即** `emit` 给下游，不在成功路径上整段缓冲后再重放（以便生图进度 / 预览等流式 UI）；
 * 6. 任何一次成功（命中 [isCompleted]）后立即返回，不再尝试后续引擎。
 *
 * 每次"尝试"会触发一次完整的 listener 回调对：
 * - 开始时 [AiInvocationListener.onStart]；
 * - 成功时 [AiInvocationListener.onCompleted]（含 `durationMs`）；
 * - 失败时 [AiInvocationListener.onFailed]。
 *
 * 注意：单个 listener 抛出的异常**会被吞掉**（除 `CancellationException`），以保证观察者副作用不阻断主流程。
 *
 * @param attempts 本次调用的全部候选引擎，按尝试顺序排列；至少 1 个。
 * @param policy 降级 / 重试策略。
 * @param invocationId 跨尝试共享的 ID。
 * @param capability 本次调用的主要能力，用于 metadata。
 * @param listeners listener 集合；按集合顺序依次触发。
 * @param engineIdOf 从引擎实例上读取 EngineId 的 lambda（避免本文件依赖具体引擎接口）。
 * @param errorOf 从事件中识别"失败事件"并提取 [AiEngineError] 的 lambda；命中即认为本次尝试失败。
 * @param isCompleted 判断事件是否为"成功完结事件"的 lambda；命中即认为本次尝试成功，停止后续尝试。
 */
internal suspend fun <E : Any, V> FlowCollector<V>.collectWithFallback(
    attempts: List<EngineAttempt<E, V>>,
    policy: FallbackPolicy,
    invocationId: String,
    capability: AiCapability,
    listeners: List<AiInvocationListener>,
    engineIdOf: (E) -> EngineId,
    errorOf: (V) -> AiEngineError?,
    isCompleted: (V) -> Boolean,
) {
    require(attempts.isNotEmpty()) { "collectWithFallback 至少需要一个候选引擎" }

    var attemptCounter = 0
    var lastFailureEvent: V? = null
    var lastFailureError: AiEngineError? = null

    /** 最近一次失败尝试是否已在流中 `emit` 过 [errorOf] 非空事件（用于避免末尾重复 `emit`）。 */
    var lastAttemptStreamedFailure = false

    attemptsLoop@ for (attempt in attempts) {
        var retryLeft = policy.maxRetries
        while (true) {
            attemptCounter++
            val metadata = InvocationMetadata(
                invocationId = invocationId,
                capability = capability,
                engineId = engineIdOf(attempt.engine),
                model = attempt.model,
                attempt = attemptCounter,
            )
            listeners.notify { it.onStart(metadata) }

            val mark = TimeSource.Monotonic.markNow()
            var failureError: AiEngineError? = null
            var completed = false

            /** 本次尝试内是否已通过流发出过带 [errorOf] 的终端失败事件（避免末尾再 `emit` 一次）。 */
            var streamedFailureEvent = false
            var attemptLastFailureEvent: V? = null

            try {
                attempt.stream().collect { event ->
                    emit(event)
                    val err = errorOf(event)
                    if (err != null) {
                        failureError = err
                        attemptLastFailureEvent = event
                        streamedFailureEvent = true
                    } else if (isCompleted(event)) {
                        completed = true
                    }
                }
            } catch (ce: kotlin.coroutines.cancellation.CancellationException) {
                throw ce
            } catch (t: Throwable) {
                failureError = AiEngineError.Unknown(
                    cause = t,
                    message = t.message,
                )
            }

            if (completed) {
                listeners.notify { it.onCompleted(metadata, mark.elapsedNow().inWholeMilliseconds) }
                return
            }

            val finalError = failureError
                ?: AiEngineError.Unknown(
                    cause = null,
                    message = "引擎流结束但未发出 Completed/Failed 事件",
                )

            listeners.notify { it.onFailed(metadata, finalError) }
            lastFailureError = finalError
            lastFailureEvent = attemptLastFailureEvent
            lastAttemptStreamedFailure = streamedFailureEvent

            if (retryLeft > 0 && policy.shouldFallback(finalError)) {
                retryLeft--
                continue
            }
            break
        }

        if (!policy.shouldFallback(lastFailureError!!)) {
            break@attemptsLoop
        }
    }

    val toEmit = lastFailureEvent
    if (toEmit != null && !lastAttemptStreamedFailure) {
        emit(toEmit)
    }
}

/**
 * 安全地按顺序触发 listener；单个 listener 抛异常时**吞掉**——观察者不应影响主流程。
 *
 * `CancellationException` 例外，原样向上抛由协程框架处理。
 */
private inline fun List<AiInvocationListener>.notify(block: (AiInvocationListener) -> Unit) {
    for (l in this) {
        try {
            block(l)
        } catch (ce: kotlin.coroutines.cancellation.CancellationException) {
            throw ce
        } catch (_: Throwable) {
            // 故意吞掉：观察者副作用不应阻断业务
        }
    }
}

/**
 * 把"主引擎 + 备用引擎 ID 列表 + 'how to resolve a backup engine'"组装成 [EngineAttempt] 列表。
 *
 * 设计要点：
 * - 备用引擎可能不在 Registry 中（用户配置漂移）：找不到的直接跳过，**不**报错；
 *   这属于"用户配置过期"的常见情况，不应阻塞主调用。
 * - 主引擎已经在列表首位，[backupIds] 中若包含与主引擎相同的 ID 会被自动去重——
 *   避免出现"先试 openai → 又试 openai"这种没意义的重复。
 *
 * @param primary 主引擎尝试。
 * @param primaryId 主引擎 ID，用于去重比较。
 * @param backupIds 备用引擎 ID 列表，按尝试顺序。
 * @param resolve 把 [EngineId] 解析为 [EngineAttempt] 的 lambda；返回 `null` 表示该 ID 在 Registry 中找不到。
 */
internal inline fun <E, V> buildAttempts(
    primary: EngineAttempt<E, V>,
    primaryId: EngineId,
    backupIds: List<EngineId>,
    resolve: (EngineId) -> EngineAttempt<E, V>?,
): List<EngineAttempt<E, V>> {
    if (backupIds.isEmpty()) return listOf(primary)
    val list = ArrayList<EngineAttempt<E, V>>(1 + backupIds.size)
    list += primary
    val seen = mutableSetOf(primaryId.value)
    for (id in backupIds) {
        if (!seen.add(id.value)) continue
        val attempt = resolve(id) ?: continue
        list += attempt
    }
    return list
}
