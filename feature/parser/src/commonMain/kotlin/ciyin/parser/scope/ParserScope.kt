package ciyin.parser.scope

import ciyin.parser.core.BaseParser
import ciyin.parser.core.EmptyParserId
import ciyin.parser.core.ParserId
import ciyin.parser.core.ParserType
import ciyin.parser.core.engine.ParserEngine
import ciyin.parser.model.ParserRequest
import ciyin.parser.model.ParserResult
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory

/**
 * 站点级 DSL 作用域。
 *
 * 在 [BaseParser.setup] 中通过 `parserScope { ... }` 进入此作用域，用于配置单个解析站点的
 * 身份、基础 URL、HTTP 客户端以及各解析类型（如漫画、图片、电影）的请求/响应逻辑。
 *
 * 典型用法：
 * ```
 * override fun ParserScope<*, *, *>.setup() {
 *     id = SomeSiteId
 *     baseUrl = "https://example.com"
 *     onResultRevise { ... }
 *     on<ComicType> {
 *         request { ... }
 *         response { ... }
 *     }
 * }
 * ```
 *
 * @param TType 解析类型密封类，对应 [ParserType] 的子类型（如 [ComicParserType]、[PictureParserType]）。
 * @param TRequest 该站点使用的请求模型类型，需实现 [ParserRequest]。
 * @param TResult 该站点产出的结果模型类型，需实现 [ParserResult]。
 * @param parser 当前作用域所配置的解析器实例，由 [BaseParser] 在构造 [ParserScope] 时传入。
 */
@ParserDsl
class ParserScope<TType : ParserType, TRequest : ParserRequest, TResult : ParserResult>(
    @PublishedApi
    internal val parser: BaseParser<TType, TRequest, TResult>,
) {

    /**
     * 是否启用当前站点。
     *
     * 为 `false` 时，[BaseParser.request] 将不发起 HTTP 请求，直接返回 [BaseParser.defTResult]。
     * 默认为 `true`。
     */
    var enable: Boolean = true

    /**
     * 站点身份标识。
     *
     * 必须在 [BaseParser.setup] 中赋值为非 [EmptyParserId] 的值，否则解析器初始化时会抛出异常。
     * 用于在多解析器场景下区分不同站点（如具体站点 ID 或自定义 [ParserId] 实现）。
     */
    var id: ParserId = EmptyParserId

    /**
     * 站点基础 URL。
     *
     * 必须在 [BaseParser.setup] 中赋值为非空字符串，否则解析器初始化时会抛出异常。
     * 请求中的相对路径会基于此 URL 进行解析。
     */
    var baseUrl: String = ""

    internal var httpClientEngineFactory: HttpClientEngineFactory<HttpClientEngineConfig> =
        ParserEngine

    internal var httpClientBlock: HttpClientConfig<HttpClientEngineConfig>.() -> Unit = {}

    internal var resultReviseBlockList: MutableList<TResult.() -> TResult> = mutableListOf()

    /**
     * 配置当前站点使用的 HTTP 客户端引擎与选项。
     *
     * 可指定 Ktor 的 [HttpClientEngineFactory]（如 OkHttp、CIO 等）以及 [HttpClientConfig] 的
     * 扩展配置（超时、重试、拦截器等）。不调用时使用 [ParserEngine] 默认引擎。
     *
     * @param T 引擎配置类型，需继承 [HttpClientEngineConfig]。
     * @param engineFactory 使用的 HTTP 引擎工厂。
     * @param block 可选的客户端配置 DSL，用于配置超时、重试、默认头等。
     */
    fun <T : HttpClientEngineConfig> httpClient(
        engineFactory: HttpClientEngineFactory<T>,
        block: HttpClientConfig<T>.() -> Unit = {},
    ) {
        httpClientEngineFactory = engineFactory
        httpClientBlock = block as HttpClientConfig<HttpClientEngineConfig>.() -> Unit
    }

    /**
     * 按解析类型注册 DSL 配置。
     *
     * 在 [TypeScope] 中通过 [TypeScope.request] 与 [TypeScope.response] 分别注册该类型下的
     * 请求构建逻辑与响应解析逻辑。同一类型多次调用 [on] 会覆盖之前的注册（每种类型仅保留最后一次）。
     *
     * @param T 具体的解析类型，必须是 [TType] 的子类型（如漫画、图片、电影等）。
     * @param block 类型作用域块，在 [TypeScope] 中配置 request/response。
     */
    inline fun <reified T : TType> on(noinline block: TypeScope<TRequest, TResult>.() -> Unit) {
        val type = parser.resolveType(T::class)
        val typeScope = TypeScope<TRequest, TResult>().apply(block)
        parser.register(type, typeScope)
    }

    /**
     * 添加结果后处理（修订）逻辑。
     *
     * 在解析得到 [TResult] 之后、返回给调用方之前，会按添加顺序依次执行已注册的修订块。
     * 可用于统一补全字段、过滤条目、修正 URL 等。多次调用会追加多个修订块，按添加顺序执行。
     *
     * @param block 接收当前结果、返回修订后的结果的函数；可对 [TResult] 进行修改后返回同一或新实例。
     */
    fun onResultRevise(block: TResult.() -> TResult) {
        resultReviseBlockList += block
    }

}

