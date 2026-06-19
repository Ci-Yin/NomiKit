package ciyin.ai.integrate.image.internal

import ciyin.ai.core.capability.AiCapability
import ciyin.ai.core.engine.EngineId
import ciyin.ai.core.error.AiEngineError
import ciyin.ai.integrate.image.AiInvocationListener
import ciyin.ai.integrate.image.FallbackPolicy
import ciyin.ai.integrate.image.InvocationMetadata
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.TimeSource

/**
 * 一次选定的生图引擎尝试。
 *
 * @param E 引擎类型。
 * @param V 事件类型。
 * @property engine 本次尝试要调用的引擎实例。
 * @property model 本次尝试使用的模型名。
 * @property stream 实际发起调用并返回事件流的函数。
 */
internal class EngineAttempt<E, V>(
    val engine: E,
    val model: String?,
    val stream: () -> Flow<V>,
)

/**
 * 生图聚合通用 fallback 调度器。
 *
 * 事件会在引擎产出时立即透传给下游；若一次尝试失败且策略允许，会继续重试或切换备用引擎。
 *
 * @param attempts 本次调用的候选引擎尝试，首个为主引擎。
 * @param policy 降级与重试策略。
 * @param invocationId 跨尝试共享的调用 ID。
 * @param capability 本次请求的主要能力。
 * @param listeners 观测监听器集合。
 * @param engineIdOf 从引擎实例读取 [EngineId] 的函数。
 * @param errorOf 从事件中识别失败错误的函数。
 * @param isCompleted 判断事件是否为成功终结事件的函数。
 * @param uncaughtFailureEvent 将未捕获异常转换为失败事件的函数。
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
    uncaughtFailureEvent: ((AiEngineError) -> V)? = null,
) {
    require(attempts.isNotEmpty()) { "collectWithFallback 至少需要一个候选引擎" }

    var attemptCounter = 0
    var lastFailureEvent: V? = null
    var lastFailureError: AiEngineError? = null
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
            var streamedFailureEvent = false
            var attemptLastFailureEvent: V? = null
            var downstreamCollectorFailed = false

            try {
                attempt.stream().collect { event ->
                    try {
                        emit(event)
                    } catch (ce: CancellationException) {
                        throw ce
                    } catch (e: Throwable) {
                        downstreamCollectorFailed = true
                        throw e
                    }
                    val err = errorOf(event)
                    if (err != null) {
                        failureError = err
                        attemptLastFailureEvent = event
                        streamedFailureEvent = true
                    } else if (isCompleted(event)) {
                        completed = true
                    }
                }
            } catch (ce: CancellationException) {
                throw ce
            } catch (t: Throwable) {
                if (downstreamCollectorFailed) {
                    listeners.notify {
                        it.onFailed(
                            metadata = metadata,
                            error = AiEngineError.Unknown(cause = t, message = t.message),
                        )
                    }
                    throw t
                }
                failureError = AiEngineError.Unknown(cause = t, message = t.message)
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

            var streamedForTerminal = streamedFailureEvent
            var attemptTerminalEvent = attemptLastFailureEvent
            if (!streamedForTerminal && uncaughtFailureEvent != null && !downstreamCollectorFailed) {
                try {
                    val synthetic = uncaughtFailureEvent(finalError)
                    emit(synthetic)
                    streamedForTerminal = true
                    attemptTerminalEvent = synthetic
                } catch (ce: CancellationException) {
                    throw ce
                } catch (_: Throwable) {
                    // 遵守 Flow 异常透明度：下游拒收补发终端事件时不再继续 emit。
                }
            }

            lastFailureEvent = attemptTerminalEvent
            lastAttemptStreamedFailure = streamedForTerminal

            if (retryLeft > 0 && policy.shouldFallback(finalError)) {
                retryLeft--
                continue
            }
            break
        }

        if (!policy.shouldFallback(lastFailureError)) {
            break@attemptsLoop
        }
    }

    val toEmit = lastFailureEvent
    if (toEmit != null && !lastAttemptStreamedFailure) {
        try {
            emit(toEmit)
        } catch (ce: CancellationException) {
            throw ce
        } catch (_: Throwable) {
            // 末尾补发若被下游打断则放弃，保持 Flow 异常透明度。
        }
    }
}

/**
 * 把主引擎与备用引擎 ID 列表组装为去重后的尝试链。
 *
 * @param primary 主引擎尝试。
 * @param primaryId 主引擎 ID，用于去重。
 * @param backupIds 备用引擎 ID 列表。
 * @param resolve 将备用引擎 ID 解析为尝试的函数；返回 `null` 表示跳过。
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

/**
 * 安全地按顺序触发 listener。
 */
private inline fun List<AiInvocationListener>.notify(block: (AiInvocationListener) -> Unit) {
    for (listener in this) {
        try {
            block(listener)
        } catch (ce: CancellationException) {
            throw ce
        } catch (_: Throwable) {
            // 观察者副作用不应阻断主流程。
        }
    }
}
