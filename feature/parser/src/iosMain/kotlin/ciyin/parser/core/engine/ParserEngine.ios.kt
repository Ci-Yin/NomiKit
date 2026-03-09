package ciyin.parser.core.engine

import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.Darwin

actual val ParserEngine: HttpClientEngineFactory<HttpClientEngineConfig> get() = Darwin

actual val MockWeb: HttpClientEngineFactory<HttpClientEngineConfig> get() = Darwin