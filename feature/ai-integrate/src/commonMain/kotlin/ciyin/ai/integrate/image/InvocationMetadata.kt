package ciyin.ai.integrate.image

import ciyin.ai.core.capability.AiCapability
import ciyin.ai.core.engine.EngineId

/**
 * 一次生图聚合调用的尝试元信息。
 *
 * @property invocationId 同一次业务调用共享的关联 ID。
 * @property capability 本次请求的主要生图能力。
 * @property engineId 本次尝试实际命中的引擎 ID。
 * @property model 本次尝试使用的模型名。
 * @property attempt 第几次尝试，从 `1` 开始。
 */
internal data class InvocationMetadata(
    val invocationId: String,
    val capability: AiCapability,
    val engineId: EngineId,
    val model: String? = null,
    val attempt: Int = 1,
)
