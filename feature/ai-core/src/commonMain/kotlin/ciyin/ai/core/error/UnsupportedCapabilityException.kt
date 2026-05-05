package ciyin.ai.core.error

import ciyin.ai.core.capability.AiCapability

/**
 * [ciyin.ai.core.registry.ChatEngineSelector] / [ciyin.ai.core.registry.ImageEngineSelector] 找不到任何满足要求 capability 的引擎时抛出的异常。
 *
 * 这是**装配 / 编排错误**，应该在开发期被发现并修正（而不是当成业务错误反复重试）。
 * 故选择以异常形式抛出而非走 `Result` / `Failed` 事件。
 *
 * @property required 调用方要求的能力集合。
 */
class UnsupportedCapabilityException(
    val required: Set<AiCapability>,
) : RuntimeException("没有引擎支持所需能力: ${required.joinToString { it::class.simpleName ?: "?" }}")
