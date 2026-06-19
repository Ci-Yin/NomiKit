package ciyin.ai.integrate.chat

import ciyin.ai.core.engine.EngineId

/**
 * 聊天引擎的配置入口：共通字段在父类型声明，各后端以 sealed 子类表达。
 */
sealed class ChatEngineConfig {

    /** 聚合层用于注册和选择具体聊天引擎的稳定标识。 */
    abstract val engineId: EngineId

    /** 聊天服务根地址，例如 `https://api.openai.com/v1` 或本地 OpenAI 兼容端点。 */
    abstract val baseUrl: String

    /** 鉴权用密钥；可为空，适用于本地 Ollama / vLLM 等无鉴权端点。 */
    abstract val apiKey: String?

    /** 当 `ChatRequest.model` 为空时注入的默认模型名。 */
    abstract val defaultModel: String?

    /**
     * OpenAI 兼容聊天端点配置。
     *
     * @property engineId 聚合层稳定引擎 ID。
     * @property baseUrl OpenAI 兼容 `/v1` 根地址。
     * @property apiKey 可选 API Key。
     * @property defaultModel 默认模型名。
     */
    data class OpenAiCompatible(
        override val engineId: EngineId,
        override val baseUrl: String,
        override val apiKey: String?,
        override val defaultModel: String?,
    ) : ChatEngineConfig()
}
