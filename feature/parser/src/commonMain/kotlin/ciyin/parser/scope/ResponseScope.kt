package ciyin.parser.scope

import ciyin.parser.core.HttpResponse
import ciyin.parser.core.ResultType
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import kotlinx.serialization.json.Json

/**
 * 响应解析作用域。
 *
 * 在 [TypeScope.response] 的 `block` 内使用，用于根据 [request] 阶段注册的 html/json/xml 请求
 * 得到的 [responses] 解析出 [TResult]：通过 [responseForHtml]/[responseForJson]/[responseForXml]
 * 按类型取 [HttpResponse]，再通过 [bodyForHtml]、[bodyForJson]、[bodyForXml] 取响应体或反序列化对象，
 * 结合 [document]（HTML 解析为 Ksoup [Document]）完成从响应到结果模型的填充。
 *
 * 典型用法（在 `on(SomeType) { response { result -> ... } }` 内）：
 * ```
 * response { result ->
 *     result.copy(
 *         title = document.selectFirst("h1")?.text() ?: "",
 *         items = bodyForJson<ItemList>().items
 *     )
 * }
 * ```
 *
 * @param responses 当前类型在 [RequestScope] 中声明的各 key 对应的 [HttpResponse] 映射（key 为 [ResultType] 的 ordinal）。
 */
@ParserDsl
class ResponseScope(
    val responses: Map<Int, HttpResponse>,
) {

    /**
     * 用于 JSON 反序列化的 [Json] 实例。
     *
     * 配置为忽略未知 key、将非法值强制为默认值、不区分 null 与缺失，便于与不稳定的 API 对接。
     * 通过 [bodyForJson] 的扩展 [String.fromJson] 使用。
     */
    val Json by lazy {
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            explicitNulls = false
        }
    }


    /**
     * 当前 HTML 响应解析后的文档对象。
     *
     * 懒加载：首次访问时对 [bodyForHtml] 调用 [Ksoup.parse] 得到 [Document]，用于 CSS 选择器等 HTML 解析。
     * 若未在 [RequestScope] 中注册 html 请求，访问 [bodyForHtml] 会抛错，进而 [document] 也会失败。
     */
    val document: Document by lazy { Ksoup.parse(bodyForHtml()) }

    /**
     * 读取 HTML 类型请求的原始响应体字符串。
     *
     * @return HTML 字符串。
     * @throws IllegalStateException 若未在 [RequestScope] 中注册 html 请求。
     */
    fun bodyForHtml(): String = responseForHtml().body

    /**
     * 读取 JSON 类型请求的原始响应体字符串。
     *
     * @return JSON 字符串。
     * @throws IllegalStateException 若未在 [RequestScope] 中注册 json 请求。
     */
    fun bodyForJson(): String = responseForJson().body

    /**
     * 读取 JSON 类型请求的响应体并反序列化为指定类型 [T]。
     *
     * 使用当前 [Json] 配置进行 [kotlinx.serialization] 反序列化；[T] 需具备可用的 Serializer（如由 @Serializable 生成）。
     *
     * @return 解析后的对象。
     * @throws IllegalStateException 若未在 [RequestScope] 中注册 json 请求。
     */
    inline fun <reified T : Any> bodyForJson(): T = bodyForJson().fromJson()

    /**
     * 读取 XML 类型请求的原始响应体字符串。
     *
     * @return XML 字符串。
     * @throws IllegalStateException 若未在 [RequestScope] 中注册 xml 请求。
     */
    fun bodyForXml(): String = responseForXml().body

    /**
     * 读取 XML 类型请求的响应体并解析为指定类型 [T]。
     *
     * @return 解析后的对象。
     */
    inline fun <reified T> bodyForXml(): T = TODO()

    /**
     * 获取 HTML 类型请求对应的 [HttpResponse]。
     *
     * 用于需要访问状态码、请求信息或原始 body 的场景；仅需 body 时可直接使用 [bodyForHtml]。
     *
     * @return 对应 [ResultType.Html] 的 [HttpResponse]。
     * @throws IllegalStateException 若未在 [RequestScope] 中注册 html 请求。
     */
    fun responseForHtml(): HttpResponse {
        return responses[ResultType.Html.id]
            ?: error("没有找到 HTML 响应，未注册 html 请求，要在 request{...} 里注册 html{...}")
    }

    /**
     * 获取 JSON 类型请求对应的 [HttpResponse]。
     *
     * 用于需要访问状态码、请求信息或原始 body 的场景；仅需 body 或反序列化对象时可直接使用 [bodyForJson]。
     *
     * @return 对应 [ResultType.Json] 的 [HttpResponse]。
     * @throws IllegalStateException 若未在 [RequestScope] 中注册 json 请求。
     */
    fun responseForJson(): HttpResponse {
        return responses[ResultType.Json.id]
            ?: error("没有找到 JSON 响应，未注册 json 请求，要在 request{...} 里注册 json{...}")
    }

    /**
     * 获取 XML 类型请求对应的 [HttpResponse]。
     *
     * 用于需要访问状态码、请求信息或原始 body 的场景；仅需 body 时可直接使用 [bodyForXml]。
     *
     * @return 对应 [ResultType.Xml] 的 [HttpResponse]。
     * @throws IllegalStateException 若未在 [RequestScope] 中注册 xml 请求。
     */
    fun responseForXml(): HttpResponse {
        return responses[ResultType.Xml.id]
            ?: error("没有找到 Xml 响应，未注册 xml 请求，要在 request{...} 里注册 xml{...}")
    }

    /**
     * 将 JSON 字符串反序列化为 [T]。
     *
     * 使用 [ResponseScope.Json] 配置，[T] 需为可序列化类型（如带 @Serializable 的 data class）。
     */
    inline fun <reified T : Any> String.fromJson(): T = Json.decodeFromString(this)
}