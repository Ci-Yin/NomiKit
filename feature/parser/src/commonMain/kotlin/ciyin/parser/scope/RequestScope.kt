package ciyin.parser.scope

import ciyin.parser.core.HttpRequestBuilder
import ciyin.parser.core.ResultType

/**
 * 请求构建作用域。
 *
 * 在 [TypeScope.request] 的 `block` 内使用，用于声明当前解析类型下需要发起的 HTTP 请求：
 * 通过 [html]、[json]、[xml] 分别声明 HTML / JSON / XML 类型的请求构建逻辑，每个类型对应一个
 * [ResultType] 的 key，后续在 [ResponseScope] 中通过 [ResponseScope.responseForHtml]、
 * [ResponseScope.responseForJson]、[ResponseScope.responseForXml] 按 key 取回对应响应进行解析。
 *
 * 典型用法（在 `on<SomeType> { request { ... } }` 内）：
 * ```
 * request { req ->
 *     html { url(path = "/page", parameters = mapOf("id" to req.id)) }
 *     json { url(path = "/api/data"); header("Accept", "application/json") }
 * }
 * ```
 *
 * 每种结果类型（Html/Json/Xml）最多注册一个请求；同一 key 多次调用会覆盖前一次配置。
 */
@ParserDsl
class RequestScope {

    /**
     * 单次请求的配置：结果类型 key 与 [HttpRequestBuilder] 构建块。
     *
     * @param key 对应 [ResultType] 的 ordinal，用于在响应阶段按类型取 [HttpResponse]。
     * @param block 在 [HttpRequestBuilder] 上设置 url、headers、isMockWeb 等并最终 build 为 [ciyin.parser.core.HttpRequest]。
     */
    internal data class RequestConfig(
        val key: Int,
        val block: HttpRequestBuilder.() -> Unit = {},
    )

    internal val requests = mutableMapOf<Int, RequestConfig>()

    /**
     * 声明 HTML 请求的构建逻辑。
     *
     * 执行时会在 [HttpRequestBuilder] 上执行 [block]，生成一条 key 为 [ResultType.Html] 的请求；
     * 响应阶段通过 [ResponseScope.responseForHtml] / [ResponseScope.bodyForHtml] 获取该请求的响应体。
     *
     * @param key 结果类型 key，默认 [ResultType.Html.id]；一般无需传入。
     * @param block 请求构建 DSL，可设置 url、headers、isMockWeb 等。
     */
    fun html(key: Int = ResultType.Html.id, block: HttpRequestBuilder.() -> Unit) {
        requests[key] = RequestConfig(key, block)
    }

    /**
     * 声明 JSON 请求的构建逻辑。
     *
     * 执行时会在 [HttpRequestBuilder] 上执行 [block]，生成一条 key 为 [ResultType.Json] 的请求；
     * 响应阶段通过 [ResponseScope.responseForJson] / [ResponseScope.bodyForJson] 获取该请求的响应体或反序列化对象。
     *
     * @param key 结果类型 key，默认 [ResultType.Json.id]；一般无需传入。
     * @param block 请求构建 DSL，可设置 url、headers、isMockWeb 等。
     */
    fun json(key: Int = ResultType.Json.id, block: HttpRequestBuilder.() -> Unit) {
        requests[key] = RequestConfig(key, block)
    }

    /**
     * 声明 XML 请求的构建逻辑。
     *
     * 执行时会在 [HttpRequestBuilder] 上执行 [block]，生成一条 key 为 [ResultType.Xml] 的请求；
     * 响应阶段通过 [ResponseScope.responseForXml] / [ResponseScope.bodyForXml] 获取该请求的响应体。
     *
     * @param key 结果类型 key，默认 [ResultType.Xml.id]；一般无需传入。
     * @param block 请求构建 DSL，可设置 url、headers、isMockWeb 等。
     */
    fun xml(key: Int = ResultType.Xml.id, block: HttpRequestBuilder.() -> Unit) {
        requests[key] = RequestConfig(key, block)
    }

}