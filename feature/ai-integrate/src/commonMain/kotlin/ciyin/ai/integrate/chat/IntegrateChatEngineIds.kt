package ciyin.ai.integrate.chat

import ciyin.ai.core.engine.EngineId

/**
 * 聊天聚合层内置的稳定引擎 ID 集合。
 */
object IntegrateChatEngineIds {

    /** 默认 OpenAI 兼容聊天端点 ID，适合单端点快速装配场景。 */
    val openAiCompatible: EngineId = EngineId("openai-compatible:default")
}
