package com.ciyin.app.ui.screen.aichat.data

import ciyin.ai.core.engine.EngineId
import ciyin.ai.integrate.chat.AiChatIntegrate
import ciyin.ai.integrate.chat.ChatEngineConfig
import com.ciyin.app.ui.screen.aichat.AiChatConnectionConfig
import com.ciyin.app.ui.screen.aichat.AiChatMessageItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * AI 聊天示例的数据与远程访问入口。
 *
 * 集中持有 OpenAI 兼容 [AiChatIntegrate] 的构造与缓存，以及 [AiChatPreferences] 的 DataStore 读写；
 * [com.ciyin.app.ui.screen.aichat.AiChatViewModel] 只通过本类访问外部 API 与持久化。
 */
internal class AiChatRepository(
    private val dataStore: AiChatDataStore = AiChatDataStore(),
) {
    /** 按规范化连接配置缓存聊天聚合入口，避免重复创建底层 HTTP 客户端。 */
    private val chatCache = mutableMapOf<AiChatConnectionConfig, AiChatIntegrate>()

    /** 读取聊天示例的持久化偏好。 */
    suspend fun loadPreferences(): AiChatPreferences = withContext(Dispatchers.IO) {
        dataStore.data.first()
    }

    /**
     * 持久化聊天端点连接配置。
     *
     * @param baseUrl OpenAI 兼容端点根地址。
     * @param apiKey 可选 API Key。
     * @param model 默认模型名。
     */
    suspend fun persistConnection(baseUrl: String, apiKey: String, model: String) {
        dataStore.updateData { prefs ->
            prefs.copy(
                baseUrl = baseUrl,
                apiKey = apiKey,
                model = model,
            )
        }
    }

    /**
     * 持久化最近的聊天消息，限制数量避免示例数据无限增长。
     *
     * @param messages 当前页面消息列表。
     */
    suspend fun persistMessages(messages: List<AiChatMessageItem>) {
        dataStore.updateData { it.copy(messages = messages.take(100)) }
    }

    /**
     * 根据界面输入的配置获取聊天聚合入口。
     *
     * 相同规范化配置会复用同一个 [AiChatIntegrate] 实例，从而避免重复创建底层 HTTP 客户端。
     */
    fun chat(config: AiChatConnectionConfig): AiChatIntegrate {
        val normalized = config.normalized()
        return chatCache.getOrPut(normalized) {
            val engineId = normalized.engineId()
            AiChatIntegrate(
                engineConfigs = listOf(
                    ChatEngineConfig.OpenAiCompatible(
                        engineId = engineId,
                        baseUrl = normalized.baseUrl,
                        apiKey = normalized.apiKey.takeIf { it.isNotBlank() },
                        defaultModel = normalized.model,
                    ),
                ),
            )
        }
    }

    /**
     * 标准化界面输入，确保缓存键与实际请求配置一致。
     */
    private fun AiChatConnectionConfig.normalized(): AiChatConnectionConfig = copy(
        baseUrl = baseUrl.trim().trimEnd('/'),
        apiKey = apiKey.trim(),
        model = model.trim(),
    )

    /**
     * 为示例端点生成稳定引擎 ID。
     */
    private fun AiChatConnectionConfig.engineId(): EngineId =
        EngineId("openai-compatible:demo:${baseUrl.lowercase()}:${model.lowercase()}")
}
