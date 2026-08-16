package ciyin.parser.core

import ciyin.parser.model.ParserResult
import ciyin.parser.scope.ParserDsl
import ciyin.parser.scope.RequestScope
import ciyin.parser.scope.ResponseScope
import ciyin.parser.scope.TypeScope
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.buildUrl
import io.ktor.http.parametersOf
import io.ktor.http.takeFrom


/**
 * 用于构建 [HttpRequest] 的 DSL 构建器。
 *
 * 在 [RequestScope] 的 [RequestScope.html]、[RequestScope.json]、
 * [RequestScope.xml] 的 block 内使用，通过 [url]、[header]/[headers] 等配置请求 URL 与请求头，
 * 最终由解析引擎调用 [build] 得到 [HttpRequest] 并发起实际 HTTP 请求。
 *
 * @param key 结果类型 key，对应 [ResultType] 的 ordinal，用于在响应阶段按类型匹配 [HttpResponse]。
 * @param baseUrl 站点基础 URL，[url] 的 path/parameters 会基于此解析。
 */
@ParserDsl
class HttpRequestBuilder(
    private val key: Int,
    internal val baseUrl: String,
) {

    /** 请求的完整 URL，可通过 [url] 的多种重载设置。默认为 [baseUrl]。 */
    var url: Url = Url(baseUrl)

    /** 请求头键值对，可通过 [header]、[headers] 追加。 */
    var headers: MutableMap<String, String> = mutableMapOf()

    /**
     * 使用 [URLBuilder] DSL 设置请求 URL。
     *
     * 会继承 [baseUrl] 的 protocol、host 与 port，在 [block] 中可修改 path、pathSegments、parameters 等。
     *
     * @param block 在 [URLBuilder] 上配置 path、query 等的 DSL。
     */
    fun url(block: URLBuilder.() -> Unit) {
        url = buildUrl {
            val urlBuilder = takeFrom(baseUrl)
            protocol = urlBuilder.protocol
            host = urlBuilder.host
            port = urlBuilder.port
            block()
        }
    }

    /**
     * 通过 path 与 query 参数设置请求 URL。
     *
     * 基于 [baseUrl] 的 protocol 与 host，将 [path] 规范化为 pathSegments（首尾 `/` 会被 trim，空 path 表示根路径），
     * 并将 [parameters] 转为 query 字符串（值为 [List] 时支持多值同一 key）。
     *
     * @param path URL 路径，如 `"/api/list"` 或 `"detail"`；空字符串表示根路径。
     * @param parameters query 参数，key 为参数名，value 为参数值列表（支持多值）。
     * @param baseUrl 可选的基础 URL，默认使用构造时的 [baseUrl]。
     */
    fun url(
        path: String = "",
        parameters: Map<String, List<Any>>,
        baseUrl: String = this@HttpRequestBuilder.baseUrl,
    ) {
        val urlBuilder = URLBuilder(baseUrl)
        val normalizedPath = path.trim('/')
        val pathSegments = if (normalizedPath.isBlank()) {
            emptyList()
        } else {
            normalizedPath.split('/')
        }
        url = URLBuilder(
            protocol = urlBuilder.protocol,
            host = urlBuilder.host,
            port = urlBuilder.port,
            pathSegments = pathSegments,
            parameters = parametersOf(
                parameters.mapValues { entry ->
                    entry.value.map { it.toString() }
                }
            )
        ).build()
    }

    /**
     * 添加单个请求头。
     *
     * @param name 请求头名（如 "Accept"、"Authorization"）。
     * @param value 请求头值，会转为 [String] 存储。
     */
    fun header(name: String, value: Any) {
        headers[name] = value.toString()
    }

    /**
     * 批量添加请求头。
     *
     * @param pairs 请求头名值对，可多个。
     */
    fun headers(vararg pairs: Pair<String, Any>) {
        pairs.forEach { (name, value) -> header(name, value) }
    }

    /**
     * 批量添加请求头。
     *
     * @param headerMap 请求头 Map，key 为名称，value 为字符串值。
     */
    fun headers(headerMap: Map<String, String>) {
        headers.putAll(headerMap)
    }

    /** 根据当前配置构建不可变的 [HttpRequest] 实例，供引擎发起请求。 */
    internal fun build(): HttpRequest {
        return HttpRequest(
            key = key,
            baseUrl = baseUrl,
            url = url,
            headers = headers,
        )
    }
}

/**
 * 通过 path 与单值 query 参数设置请求 URL 的便捷重载。
 *
 * 将 [parameters] 的每个 value 转为单元素列表后调用 [HttpRequestBuilder.url] 的三参数版本，
 * 适用于常见「一个 key 对应一个 value」的 query 场景。
 *
 * @param path URL 路径。
 * @param parameters query 参数，value 会转为字符串；同一 key 仅保留一个值。
 * @param baseUrl 可选的基础 URL。
 */
@ParserDsl
fun HttpRequestBuilder.url(
    path: String = "",
    parameters: Map<String, Any> = mapOf(),
    baseUrl: String = this@url.baseUrl,
) {
    url(
        path = path,
        parameters = parameters.mapValues { entry -> listOf(entry.value.toString()) },
        baseUrl = baseUrl,
    )
}

/**
 * 将多组键值对转为 [MutableMap]<String, Any>，便于作为 [HttpRequestBuilder.url] 的 `parameters` 参数传入（扩展重载会将其转为单值 list）。
 */
fun parametersOf(vararg pairs: Pair<String, Any>) = mutableMapOf(*pairs)

/**
 * 请求阶段产出的结果类型枚举。
 *
 * 与 [RequestScope] 的 html/json/xml 一一对应：每种类型注册的请求其 [HttpRequest.key] 为该枚举值的 ordinal，
 * 响应阶段 [ResponseScope] 按此 key 从 [ResponseScope.responses] 中取 [HttpResponse]。
 */
enum class ResultType(val id: Int) {
    /** HTML 页面请求，对应 [RequestScope.html]。 */
    Html(0),

    /** JSON API 请求，对应 [RequestScope.json]。 */
    Json(1),

    /** XML 请求，对应 [RequestScope.xml]。 */
    Xml(2)
}

/**
 * 一次 HTTP 请求的不可变描述。
 *
 * 由 [HttpRequestBuilder.build] 产生，解析引擎据此发起实际请求，并将响应封装为 [HttpResponse]。
 *
 * @param key 结果类型 key，对应 [ResultType] 的 ordinal，用于与 [HttpResponse] 按类型匹配。
 * @param baseUrl 站点基础 URL。
 * @param url 请求的完整 URL（含 path、query）。
 * @param headers 请求头键值对。
 */
data class HttpRequest(
    val key: Int,
    val baseUrl: String,
    val url: Url,
    val headers: Map<String, String>,
)

/**
 * 一次 HTTP 响应的不可变描述。
 *
 * 由解析引擎在收到 [HttpRequest] 的响应后构造，传入 [ResponseScope] 供 [TypeScope.response] 中的 DSL 解析 body、
 * 填充 [ParserResult]。请求失败时 [error] 非空，[status] 可能为异常状态码。
 *
 * @param status HTTP 状态码。
 * @param error 请求或读取过程中的异常；成功时为 null。
 * @param request 对应的 [HttpRequest]。
 * @param body 响应体字符串（如 HTML、JSON、XML 原始文本）。
 */
data class HttpResponse(
    val status: HttpStatusCode,
    val error: Throwable?,
    val request: HttpRequest,
    val body: String,
)
