package ciyin.ai.facade.observability

import ciyin.ai.core.error.AiEngineError

/**
 * AI 调用观测回调。
 *
 * 由调用方在装配 `DefaultAiChat` / `DefaultAiImage` 时以 `List<AiInvocationListener>` 形式传入；
 * 0..N 个皆可，按 list 顺序依次回调。典型用法：
 * - Kermit 日志：把 invocationId / 引擎 / 耗时 / 错误打印到日志，便于线上排查；
 * - 计费：根据 `metadata.engineId + model + 实际 token` 累计成本；
 * - Sentry / Crashlytics：在 [onFailed] 中上报特定类别错误；
 * - 业务埋点：在 [onCompleted] 中按"用户 → 模型 → 耗时"打点统计。
 *
 * 实现约束：
 * - **不应抛异常**：listener 只是观察者，业务故障不该被它的副作用打断；
 * - **不应阻塞 / 不应调 IO**：默认在调用线程上同步触发，需要 IO 请自行切线程；
 * - 三个回调对**同一次尝试**应严格按 [onStart] → ([onCompleted] | [onFailed]) 顺序触发。
 */
interface AiInvocationListener {

    /**
     * 一次尝试即将开始时回调（主引擎首次调用、重试、降级到备用引擎都会触发各自的 [onStart]）。
     *
     * @param metadata 本次尝试的元信息，参见 [InvocationMetadata]。
     */
    fun onStart(metadata: InvocationMetadata)

    /**
     * 一次尝试成功完成时回调。
     *
     * @param metadata 与对应 [onStart] 同一份。
     * @param durationMs 从 [onStart] 到本回调的毫秒数。
     */
    fun onCompleted(metadata: InvocationMetadata, durationMs: Long)

    /**
     * 一次尝试失败时回调。
     *
     * 注意：失败之后若仍有备用引擎可降级，下一次尝试会触发新的 [onStart]，但 [InvocationMetadata.invocationId]
     * 与本次相同；listener 可借此在 UI 上显示"已切换到 XX 引擎"。
     *
     * @param metadata 与对应 [onStart] 同一份。
     * @param error 引擎层错误模型。
     */
    fun onFailed(metadata: InvocationMetadata, error: AiEngineError)
}
