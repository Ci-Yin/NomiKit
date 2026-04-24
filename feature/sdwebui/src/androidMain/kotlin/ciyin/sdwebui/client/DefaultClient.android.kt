package ciyin.sdwebui.client

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.okhttp.OkHttp

/**
 * Android 平台默认引擎使用 OkHttp，复用系统已成熟的连接池、Cookie、TLS 配置。
 */
internal actual fun defaultHttpClientEngineFactory(): HttpClientEngineFactory<*> = OkHttp
