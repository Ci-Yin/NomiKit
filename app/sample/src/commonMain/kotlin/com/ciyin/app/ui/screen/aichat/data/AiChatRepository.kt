package com.ciyin.app.ui.screen.aichat.data

import ciyin.ai.chat.openai.OpenAiChatEngine
import ciyin.ai.chat.openai.OpenAiChatEngineConfig
import ciyin.ai.core.engine.EngineId
import ciyin.ai.core.registry.ChatEngineSelector
import ciyin.ai.core.registry.DefaultChatEngineRegistry
import ciyin.ai.facade.AiChat
import ciyin.ai.facade.DefaultAiChat
import com.ciyin.app.ui.screen.aichat.AiChatConnectionConfig
import com.ciyin.app.ui.screen.aichat.AiChatMessageItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * AI 聊天示例的数据与远程访问入口。
 *
 * 集中持有 OpenAI 兼容 [AiChat] 的构造与缓存，以及 [AiChatPreferences] 的 DataStore 读写；
 * [com.ciyin.app.ui.screen.aichat.AiChatViewModel] 只通过本类访问外部 API 与持久化。
 */
internal class AiChatRepository(
    private val dataStore: AiChatDataStore = AiChatDataStore(),
) {
    private val chatCache = mutableMapOf<AiChatConnectionConfig, AiChat>()

    suspend fun loadPreferences(): AiChatPreferences = withContext(Dispatchers.IO) {
        dataStore.data.first()
    }

    suspend fun persistConnection(baseUrl: String, apiKey: String, model: String) {
        dataStore.updateData { prefs ->
            prefs.copy(
                baseUrl = baseUrl,
                apiKey = apiKey,
                model = model,
            )
        }
    }

    suspend fun persistMessages(messages: List<AiChatMessageItem>) {
        dataStore.updateData { it.copy(messages = messages.take(100)) }
    }

    /**
     * 根据界面输入的配置获取聊天 Facade。
     *
     * 相同规范化配置会复用同一个 [AiChat] 实例，从而避免重复创建底层 HTTP 客户端。
     */
    fun chat(config: AiChatConnectionConfig): AiChat {
        val normalized = config.normalized()
        return chatCache.getOrPut(normalized) {
            val engineId = normalized.engineId()
            val engine = OpenAiChatEngine(
                OpenAiChatEngineConfig(
                    id = engineId,
                    baseUrl = normalized.baseUrl,
                    apiKey = normalized.apiKey.takeIf { it.isNotBlank() },
                    defaultModel = normalized.model,
                )
            )
            val selector = ChatEngineSelector(
                registry = DefaultChatEngineRegistry(listOf(engine)),
            )
            DefaultAiChat(
                selector = selector,
                preferences = AiChatEnginePreferences(
                    engineId = engineId,
                    model = normalized.model,
                ),
            )
        }
    }

    private fun AiChatConnectionConfig.normalized(): AiChatConnectionConfig = copy(
        baseUrl = baseUrl.trim().trimEnd('/'),
        apiKey = apiKey.trim(),
        model = model.trim(),
    )

    private fun AiChatConnectionConfig.engineId(): EngineId =
        EngineId("openai-compatible:demo:${baseUrl.lowercase()}:${model.lowercase()}")
}
