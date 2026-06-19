package ciyin.ai.integrate.image

import ciyin.ai.core.error.AiEngineError

/**
 * 生图聚合调用的观测回调。
 *
 * Listener 只用于观察调用过程，不应抛异常或执行阻塞 IO；调度器会吞掉非取消类异常，避免观测副作用影响主流程。
 */
internal interface AiInvocationListener {

    /**
     * 一次引擎尝试即将开始时回调。
     *
     * @param metadata 本次尝试的元信息。
     */
    fun onStart(metadata: InvocationMetadata)

    /**
     * 一次引擎尝试成功完成时回调。
     *
     * @param metadata 与对应 [onStart] 同一份。
     * @param durationMs 本次尝试耗时毫秒数。
     */
    fun onCompleted(metadata: InvocationMetadata, durationMs: Long)

    /**
     * 一次引擎尝试失败时回调。
     *
     * @param metadata 与对应 [onStart] 同一份。
     * @param error 引擎层错误模型。
     */
    fun onFailed(metadata: InvocationMetadata, error: AiEngineError)
}
