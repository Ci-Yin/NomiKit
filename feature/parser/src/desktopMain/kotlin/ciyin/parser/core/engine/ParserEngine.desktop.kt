package ciyin.parser.core.engine

import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.okhttp.OkHttp

actual val ParserEngine: HttpClientEngineFactory<HttpClientEngineConfig> get() = OkHttp

actual val MockWeb: HttpClientEngineFactory<HttpClientEngineConfig> get() = OkHttp
