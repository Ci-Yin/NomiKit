package ciyin.ai.chat.openai.client

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.okhttp.OkHttp

/**
 * Android 平台默认使用 OkHttp 引擎。
 */
internal actual fun defaultHttpClientEngineFactory(): HttpClientEngineFactory<*> = OkHttp
