package ciyin.ai.chat.openai.client

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.cio.CIO

/**
 * Desktop 平台默认使用 CIO 引擎。
 */
internal actual fun defaultHttpClientEngineFactory(): HttpClientEngineFactory<*> = CIO
