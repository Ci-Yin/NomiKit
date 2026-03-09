package ciyin.parser.core.engine

import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory


/**
 *
 * kotlin文件作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2026/3/8 21:35
 */
private fun preview() {}

expect val ParserEngine: HttpClientEngineFactory<HttpClientEngineConfig>

expect val MockWeb: HttpClientEngineFactory<HttpClientEngineConfig>

