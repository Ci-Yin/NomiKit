package ciyin.sdwebui.client

import ciyin.sdwebui.SdWebUi
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * [Client] 的默认实现，基于 Ktor `HttpClient` 完成实际网络请求。
 *
 * 所使用的 HTTP 引擎按平台动态选择：
 * - Android：OkHttp
 * - JVM/Desktop：CIO
 * - iOS：Darwin
 *
 * 通过 [defaultHttpClientEngineFactory] 这一 `expect` 函数桥接到各平台 `actual` 实现。
 *
 * 调用方仍可通过自行实现 [Client] 抽象类完全替换底层网络栈；本类只是提供开箱即用的默认能力。
 *
 * @param json 用于序列化请求体与解析响应内容的 [Json] 实例。
 */
class DefaultClient(private val json: Json) : Client() {

    private val httpClient: HttpClient by lazy {
        HttpClient(defaultHttpClientEngineFactory()) {
            install(ContentNegotiation) {
                json(json)
            }
            install(HttpTimeout) {
                requestTimeoutMillis = SdWebUi.DEFAULT_TIMEOUT
            }
        }
    }

    override suspend fun request(builder: RequestBuilder.() -> RequestBuilder): Response {
        val request = RequestBuilder().builder().build()
        val httpResponse = httpClient.request {
            url("${request.baseUrl}/${request.path}")
            method = HttpMethod(request.method.name)
            contentType(ContentType.Application.Json)
            if (request.body != null && request.bodyType != null) {
                setBody(request.body, request.bodyType)
            }
        }
        return Response(
            isSuccess = httpResponse.status.isSuccess(),
            body = httpResponse.body(),
        )
    }
}

/**
 * 由各平台提供默认的 Ktor [HttpClientEngineFactory]。
 *
 * 仅在 [DefaultClient] 内部使用，库使用方不应直接依赖此函数；
 * 如需自定义引擎，请实现 [Client] 抽象类并通过 `SdWebUi.Builder.client(...)` 注入。
 */
internal expect fun defaultHttpClientEngineFactory(): HttpClientEngineFactory<*>
