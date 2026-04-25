package ciyin.ai.facade.internal

import ciyin.ai.core.error.AiEngineError
import ciyin.ai.facade.observability.AiInvocationListener
import ciyin.ai.facade.observability.InvocationMetadata

/**
 * 把 [List] 形态的 [AiInvocationListener] 集中调度。
 *
 * 单个 listener 抛出的异常**不应**影响其他 listener，也不应中断 Facade 主流程；
 * 这里通过 `runCatching { ... }` 静默吞掉。Facade 只是观察通道，业务故障不该被
 * 一个失败的埋点上报打翻。
 */
internal class ListenerDispatcher(
    private val listeners: List<AiInvocationListener>,
) {
    fun onStart(metadata: InvocationMetadata) {
        listeners.forEach { runCatching { it.onStart(metadata) } }
    }

    fun onCompleted(metadata: InvocationMetadata, durationMs: Long) {
        listeners.forEach { runCatching { it.onCompleted(metadata, durationMs) } }
    }

    fun onFailed(metadata: InvocationMetadata, error: AiEngineError) {
        listeners.forEach { runCatching { it.onFailed(metadata, error) } }
    }
}
