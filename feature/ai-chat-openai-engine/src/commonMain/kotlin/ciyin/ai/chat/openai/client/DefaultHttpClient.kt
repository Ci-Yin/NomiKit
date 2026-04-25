package ciyin.ai.chat.openai.client

import ciyin.ai.chat.openai.OpenAiChatEngineConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

/**
 * 创建 `OpenAI 兼容协议` 默认 HTTP 客户端。
 */
internal fun createDefaultHttpClient(
    config: OpenAiChatEngineConfig,
    json: Json,
): HttpClient = HttpClient(defaultHttpClientEngineFactory()) {
    install(ContentNegotiation) {
        json(json)
    }
    install(HttpTimeout) {
        requestTimeoutMillis = config.requestTimeoutMs
        socketTimeoutMillis = config.streamReadTimeoutMs
    }
    install(Logging) {
        level = LogLevel.NONE
    }
}

/**
 * 由各平台提供默认 HTTP 引擎。
 */
internal expect fun defaultHttpClientEngineFactory(): HttpClientEngineFactory<*>

/**
 * 当前模块统一使用的 `Json` 实例。
 */
@OptIn(ExperimentalSerializationApi::class)
internal val OpenAiJson: Json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    encodeDefaults = true
}
