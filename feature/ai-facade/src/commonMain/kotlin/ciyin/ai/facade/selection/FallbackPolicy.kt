package ciyin.ai.facade.selection

import ciyin.ai.core.engine.EngineId
import ciyin.ai.core.error.AiEngineError
import kotlin.reflect.KClass

/**
 * 降级 / 重试策略。
 *
 * `AiChat` / `AiImage` 默认实现的执行流程：
 * 1. 先用主引擎调用，**单引擎内**失败时按 [maxRetries] 重试；
 * 2. 若 [maxRetries] 次后仍失败且错误命中 [triggerOn]，则按 [backupEngines] 顺序逐个尝试备用引擎；
 * 3. 全部失败后，把**最后一次**的 [AiEngineError] 通过 `Failed` 事件透传。
 *
 * 注意：用户主动取消（[AiEngineError.Cancelled]）和明确拒绝（[AiEngineError.Refused]）
 * **默认不**触发降级——拒绝是引擎/模型的明确信号，盲目切到另一家不解决问题。
 *
 * @property maxRetries 单引擎内的重试次数（**不含**首次调用）；`0` 表示不重试。
 * @property backupEngines 主引擎失败后按顺序尝试的备用引擎 ID 列表；为空则不降级。
 * @property triggerOn 哪些错误类型才触发"切下一个引擎"；只在 `is` 判断成立时算命中。
 */
data class FallbackPolicy(
    val maxRetries: Int = 1,
    val backupEngines: List<EngineId> = emptyList(),
    val triggerOn: Set<KClass<out AiEngineError>> = setOf(
        AiEngineError.Network::class,
        AiEngineError.RateLimited::class,
        AiEngineError.Unknown::class,
    ),
) {
    init {
        require(maxRetries >= 0) { "maxRetries 不能为负: $maxRetries" }
    }

    /**
     * 判断给定错误是否应触发"切到下一个引擎"。
     *
     * 用 `KClass.isInstance` 而非 `==` 比较，以兼容子类型；当前 [AiEngineError] 各分支
     * 均为 `data class` / `data object`，等价语义但保留扩展空间。
     */
    fun shouldFallback(error: AiEngineError): Boolean =
        triggerOn.any { it.isInstance(error) }
}
