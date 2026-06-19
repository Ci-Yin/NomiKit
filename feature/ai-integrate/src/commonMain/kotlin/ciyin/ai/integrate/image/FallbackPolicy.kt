package ciyin.ai.integrate.image

import ciyin.ai.core.engine.EngineId
import ciyin.ai.core.error.AiEngineError
import kotlin.reflect.KClass

/**
 * 生图聚合调用的降级与重试策略。
 *
 * @property maxRetries 单引擎内的重试次数，不包含首次调用；`0` 表示不重试。
 * @property backupEngines 主引擎失败后按顺序尝试的备用引擎 ID 列表。
 * @property triggerOn 哪些错误类型才触发重试或切换到下一个引擎。
 */
internal data class FallbackPolicy(
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
     * 判断 [error] 是否应触发一次重试或备用引擎切换。
     */
    fun shouldFallback(error: AiEngineError): Boolean =
        triggerOn.any { it.isInstance(error) }
}
