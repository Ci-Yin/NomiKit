package ciyin.sdwebui.client

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.Darwin

/**
 * iOS 平台默认引擎使用 Darwin，底层走 NSURLSession，可享受系统级网络配置（如代理、网络授权）。
 */
internal actual fun defaultHttpClientEngineFactory(): HttpClientEngineFactory<*> = Darwin
