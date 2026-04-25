package ciyin.ai.chat.openai.client

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.Darwin

/**
 * iOS 平台默认使用 Darwin 引擎。
 */
internal actual fun defaultHttpClientEngineFactory(): HttpClientEngineFactory<*> = Darwin
