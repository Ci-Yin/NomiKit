package ciyin.ai.integrate.chat

import ciyin.ai.chat.openai.OpenAiChatEngine
import ciyin.ai.chat.openai.OpenAiChatEngineConfig
import ciyin.ai.core.engine.ChatEngine

/**
 * 面向调用方的默认聊天聚合入口：不内置任何端点，调用方通过 [AiChatIntegrate.engines] 或配置工厂注入。
 */
fun AiChatIntegrate(): AiChatIntegrate = AiChatIntegrate(
    defaultEngineConfigs = emptyList(),
    buildChatEngine = ::defaultOpenAiCompatibleChatEngine,
)

/**
 * 面向调用方的带配置聊天聚合入口：构造后即可调用 [AiChatIntegrate.stream]。
 *
 * @param engineConfigs 初始聊天引擎配置列表。
 */
fun AiChatIntegrate(engineConfigs: List<ChatEngineConfig>): AiChatIntegrate = AiChatIntegrate(
    defaultEngineConfigs = engineConfigs,
    buildChatEngine = ::defaultOpenAiCompatibleChatEngine,
)

/**
 * 将聚合层 [ChatEngineConfig] 转为具体 [ChatEngine]。
 */
private fun defaultOpenAiCompatibleChatEngine(config: ChatEngineConfig): ChatEngine =
    when (config) {
        is ChatEngineConfig.OpenAiCompatible -> OpenAiChatEngine(
            OpenAiChatEngineConfig(
                id = config.engineId,
                baseUrl = config.baseUrl,
                apiKey = config.apiKey,
                defaultModel = config.defaultModel,
            ),
        )
    }
