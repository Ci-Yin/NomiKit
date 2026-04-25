package ciyin.ai.facade.observability

import ciyin.ai.core.capability.AiCapability
import ciyin.ai.core.engine.EngineId

/**
 * 一次 Facade 调用的元信息，用于跨 [AiInvocationListener] 关联与归类。
 *
 * @property invocationId Facade 内部生成的唯一 ID（建议用 UUID-like 字符串），跨 listener 关联用。
 *           **同一次业务调用**在主引擎与 fallback 期间应当**保持同一个 invocationId**，
 *           只通过 [attempt] 区分不同尝试。
 * @property capability 本次调用属于哪种能力族——`ChatCapability.Streaming` / `ImageCapability.TextToImage` 等典型能力之一。
 *           取**主要能力**即可，不必穷举。
 * @property engineId **本次尝试**实际命中的引擎 ID（fallback 后会变）。
 * @property model 本次尝试实际使用的模型名；`null` 表示沿用引擎默认。
 * @property attempt 第几次尝试，从 `1` 开始。`1` 是首次主引擎调用，
 *           `1 + maxRetries` 之后才轮到第一个备用引擎。
 */
data class InvocationMetadata(
    val invocationId: String,
    val capability: AiCapability,
    val engineId: EngineId,
    val model: String? = null,
    val attempt: Int = 1,
)
