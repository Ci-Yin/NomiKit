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
 * [Client] 的默认实现，基于 Ktor [HttpClient] 发起真实 HTTP 请求。
 *
 * 各平台默认 HTTP 引擎为：Android 使用 OkHttp，Desktop/JVM 使用 CIO，iOS 使用 Darwin；
 * 通过 [defaultHttpClientEngineFactory] 的 expect/actual 在各源码集落地。
 *
 * 若需完全自定义网络栈，可实现 [Client] 并通过 [SdWebUi.Builder.client] 注入；本类仅提供开箱即用方案。
 *
 * @param json 用于编码请求体与解码响应 JSON 的实例。
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

    /**
     * 使用内部 [HttpClient] 执行 [RequestBuilder] 描述的一次请求并返回 [Response]。
     */
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
 * 由各平台提供的默认 Ktor [HttpClientEngineFactory]。
 *
 * 仅供 [DefaultClient] 内部使用；调用方若要替换引擎应自行实现 [Client] 并通过 `SdWebUi.Builder.client` 注入。
 */
internal expect fun defaultHttpClientEngineFactory(): HttpClientEngineFactory<*>
