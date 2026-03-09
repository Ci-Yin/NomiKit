package ciyin.parser.scope

import ciyin.parser.model.ParserRequest

/**
 * 类型级 DSL 作用域。
 *
 * 在 [ParserScope.on] 的 `block` 内使用，用于为某一种解析类型（如漫画、图片、电影）配置
 * 「请求如何构建」与「响应如何解析为结果」：通过 [request] 注册 [RequestScope] 下的多路请求
 *（html/json/xml），通过 [response] 注册 [ResponseScope] 下从响应构造 [TResult] 的逻辑。
 *
 * 典型用法（在 `parserScope { on<ComicType> { ... } }` 内）：
 * ```
 * on<ComicType> {
 *     request { req -> html { url(path = "/comic/${req.id}") } }
 *     response { result -> document.select("...").let { ... result } }
 * }
 * ```
 *
 * 同一类型多次调用 [request] 或 [response] 会覆盖前一次注册（各保留一份）。
 *
 * @param TRequest 该类型使用的请求模型，需实现 [ParserRequest]，在 [request] 的 block 中作为参数传入。
 * @param TResult 该类型产出的结果模型，需实现 [ParserResult]，由 [response] 的 block 构建并返回。
 */
@ParserDsl
class TypeScope<TRequest : ParserRequest, TResult : ciyin.parser.model.ParserResult> {

    internal var requestBlock: RequestScope.(TRequest) -> Unit = {}
    internal var responseBlock: ResponseScope.(TResult) -> TResult = { it }

    /**
     * 注册请求构建逻辑。
     *
     * 给定当前类型的 [TRequest] 实例，在 [RequestScope] 中通过 [RequestScope.html]、[RequestScope.json]、
     * [RequestScope.xml] 声明要发起的请求；解析引擎会按这些声明并发请求，并将响应按 key 传入 [response] 的 [ResponseScope]。
     *
     * @param block 接收 [TRequest]、在 [RequestScope] 上声明 html/json/xml 请求的 DSL。
     */
    fun request(block: RequestScope.(TRequest) -> Unit) {
        requestBlock = block
    }

    /**
     * 注册响应解析逻辑。
     *
     * 给定当前类型的 [TResult] 实例（可能是默认或中间结果）以及已请求得到的 [ResponseScope]（内含 [ResponseScope.responses]），
     * 在 block 中通过 [ResponseScope.document]、[ResponseScope.bodyForHtml]、[ResponseScope.bodyForJson] 等读取响应并填充/返回 [TResult]。
     *
     * @param block 接收 [TResult]、在 [ResponseScope] 上解析响应并返回最终 [TResult] 的 DSL。
     */
    fun response(block: ResponseScope.(TResult) -> TResult) {
        responseBlock = block
    }
}