package ciyin.sdwebui.client

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.cio.CIO

/**
 * Desktop（JVM）平台默认引擎使用 CIO，与原 `sdwebui-kotlin` 单 JVM 工程保持一致。
 */
internal actual fun defaultHttpClientEngineFactory(): HttpClientEngineFactory<*> = CIO
