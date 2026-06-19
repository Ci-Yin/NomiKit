package ciyin.ai.integrate.chat

import ciyin.ai.core.capability.ChatCapability
import ciyin.ai.core.engine.ChatEngine
import ciyin.ai.core.engine.EngineId

/**
 * 调用方用来表达「如何路由到聊天引擎（及可选模型）」的轻量描述。
 *
 * 调用方只描述选择意图，不直接持有 [ChatEngine]；具体引擎由 [AiChatIntegrate] 根据当前注册表解析。
 */
sealed interface ChatEngineSpec {

    /** 使用聚合层默认选择，即当前注册顺序中的首个可用聊天引擎。 */
    data object Default : ChatEngineSpec

    /**
     * 显式指定聊天引擎与可选模型。
     *
     * @property engineId 目标引擎 ID。
     * @property model 可选模型名；非空时优先于请求模型与配置默认模型。
     */
    data class Explicit(
        val engineId: EngineId,
        val model: String? = null,
    ) : ChatEngineSpec

    /**
     * 按能力筛选聊天引擎，使用注册顺序中的首个满足项。
     *
     * @property required 必须同时满足的聊天能力集合。
     */
    data class ByCapability(
        val required: Set<ChatCapability>,
    ) : ChatEngineSpec
}
