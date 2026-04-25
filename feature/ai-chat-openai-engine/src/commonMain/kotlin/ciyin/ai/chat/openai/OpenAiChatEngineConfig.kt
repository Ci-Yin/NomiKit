package ciyin.ai.chat.openai

import ciyin.ai.core.engine.EngineId

/**
 * [OpenAiChatEngine] 的配置项。
 *
 * 该配置既可指向 OpenAI 官方服务，也可指向任意 OpenAI 兼容端点（OpenRouter、DeepSeek、
 * Together、vLLM、Ollama 等）。
 */
data class OpenAiChatEngineConfig(
    val id: EngineId,
    val baseUrl: String,
    val apiKey: String? = null,
    val organization: String? = null,
    val defaultModel: String? = null,
    val customHeaders: Map<String, String> = emptyMap(),
    val requestTimeoutMs: Long = 60_000,
    val streamReadTimeoutMs: Long = 5 * 60_000,
)
