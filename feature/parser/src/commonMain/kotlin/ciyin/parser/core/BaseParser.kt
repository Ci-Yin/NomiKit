package ciyin.parser.core

import ciyin.parser.core.engine.ParserEngine
import ciyin.parser.model.ParserConfigure
import ciyin.parser.model.ParserRequest
import ciyin.parser.model.ParserResult
import ciyin.parser.scope.ParserScope
import ciyin.parser.scope.RequestScope
import ciyin.parser.scope.ResponseScope
import ciyin.parser.scope.TypeScope
import ciyin.platform.thisLogger
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * 通用解析器基类，提供「站点配置 + 类型注册 + 请求执行 + 响应解析」的完整骨架。
 *
 * 子类在 [setup] 中通过 [ParserScope] 配置站点 [ParserScope.id]、[ParserScope.baseUrl]，
 * 并通过 [ParserScope.on] 为各 [TType] 注册 [TypeScope]（在 [RequestScope]/[ResponseScope] 中声明
 * 如何构建请求、如何从响应解析出 [TResult]）。调用 [request] 时根据 [TRequest.type] 选择对应
 * [TypeScope]，构建 [HttpRequest]、并发请求、按 DSL 解析为 [TResult]，并通过 [Flow] 发送 [ParserEvent]。
 *
 * 典型用法：
 * 1. 继承本类并实现 [setup]、[defTResult]。
 * 2. 在 [setup] 中设置 `id`、`baseUrl`，并用 `on(SomeType) { request { ... }; response { ... } }` 注册各类型（[SomeType] 为 [TType] 的单例）。
 * 3. 调用 [request] 传入 [TRequest]，收集 [ParserEvent.Success] 或 [ParserEvent.Failure] 得到结果。
 *
 * @param baseUrlOverride 可选的站点基础地址覆盖值，供镜像端点与离线契约测试使用。
 * @param TType 解析类型，需实现 [ParserType]，用于区分漫画/图片/电影等不同解析流程。
 * @param TRequest 解析请求类型，需实现 [ParserRequest]；[ParserRequest.type] 决定使用的 [TypeScope]。
 * @param TResult 解析结果类型，需实现 [ParserResult]；由各类型的 response DSL 构建并可通过 [ParserScope.onResultRevise] 修订。
 */
abstract class BaseParser<TType : ParserType, TRequest : ParserRequest, TResult : ParserResult>(
    private val baseUrlOverride: String? = null,
) {

    /** 当前解析器使用的日志实例，供子类记录请求/响应或错误。 */
    protected val logger by lazy { thisLogger() }

    /** 类型注册表：将 [TType] 映射到对应的 [TypeScope]，在 [setup] 中通过 [ParserScope.on] 填充。 */
    private val mutableRegistry = linkedMapOf<TType, TypeScope<TRequest, TResult>>()

    /** 类型注册表只读视图，用于根据 [TRequest.type] 查找 [TypeScope]。 */
    private val registry: Map<TType, TypeScope<TRequest, TResult>> get() = mutableRegistry

    /** 解析器作用域，在 [setup] 中用于配置站点与各类型的 DSL。 */
    private val parserScope = ParserScope(this)

    /**
     * 当前解析配置，包含站点 [ParserConfigure.id]、[ParserConfigure.baseUrl] 以及本次请求与解析结果。
     * 在 [execute] 中会随 [request] 与解析得到的 [TResult] 更新；初始化时若 [setup] 未设置 id/baseUrl 会抛异常。
     */
    var configure: ParserConfigure<TRequest, TResult> = parserScope.run {
        setup()
        baseUrlOverride?.let { baseUrl = it }
        require(id != EmptyParserId) {
            "站点未设置 ID，在 ParserScope<*, *, *>.setup() {...} 里调用 id = \"...\""
        }
        require(baseUrl.isNotBlank()) {
            "站点未设置 BaseUrl，在 ParserScope<*, *, *>.setup() {...} 里调用 baseUrl = \"...\""
        }
        ParserConfigure(
            id = id,
            baseUrl = baseUrl,
            request = null,
            result = defTResult,
        )
    }
        private set

    /** 当前解析类型的默认结果实例；当 [enable] 为 false 或未发起请求/解析未产出结果时返回。 */
    internal abstract val defTResult: TResult

    /** 是否启用该解析器；为 false 时 [request] 不发起请求，直接返回 [defTResult] 并发送 [ParserEvent.Success]。 */
    var enable by parserScope::enable

    /**
     * 解析使用的 HTTP 客户端。
     * 使用 [ParserScope] 中配置的引擎（默认 [ParserEngine]），并安装超时、重试等插件；可在 [ParserScope.httpClient] 中覆盖。
     */
    private val httpClient: HttpClient = HttpClient(parserScope.httpClientEngineFactory) {

        install(HttpTimeout) {
            requestTimeoutMillis = 10_000 // 10s
            connectTimeoutMillis = 10_000 // 10s
            socketTimeoutMillis = 10_000
        }

        install(HttpRequestRetry) {
            retryOnServerErrors(maxRetries = 3)
            constantDelay(millis = 100, randomizationMs = 100)
        }

        parserScope.httpClientBlock(this)
    }

    /** 当前执行轮次中已构建的请求列表，key 为请求序号；[buildRequests] 写入，[executeRequests] 使用。 */
    private val requests = mutableMapOf<Int, HttpRequest>()

    /** 当前执行轮次中已收到的响应列表，key 与 [requests] 一致；[executeRequests] 写入，[execute] 中 response DSL 使用。 */
    private val responses = mutableMapOf<Int, HttpResponse>()

    /**
     * 根据 [TRequest.type] 从 [registry] 取对应的 [TypeScope]；若该类型未在 [setup] 中注册则抛错。
     */
    @Suppress("UNCHECKED_CAST")
    private val TRequest.typeScope: TypeScope<TRequest, TResult>
        get() = registry.getOrElse(type as TType) { error("未注册类型：${type}") }


    /**
     * 站点 DSL 配置入口，子类必须实现。
     * 在此通过 [ParserScope] 设置 [id][ParserScope.id]、[baseUrl][ParserScope.baseUrl]，并调用
     * [ParserScope.on] 为各 [TType] 注册 [TypeScope]（在 [TypeScope.request]/[TypeScope.response] 中配置请求构建与响应解析）。
     */
    protected abstract fun ParserScope<TType, TRequest, TResult>.setup()

    /**
     * 注册某一 [TType] 的 [TypeScope]。
     * 由 [ParserScope.on] 内部调用，子类一般无需直接使用。
     *
     * @param type 解析类型实例。
     * @param typeScope 该类型对应的请求/响应 DSL 配置。
     */
    @PublishedApi
    internal fun register(type: TType, typeScope: TypeScope<TRequest, TResult>) {
        mutableRegistry[type] = typeScope
    }

    /**
     * 执行指定类型的完整解析流程。
     *
     * 1. 根据当前 [TRequest.type] 对应的 [TypeScope] 在 [RequestScope] 中构建 [HttpRequest] 列表。
     * 2. 并发发送所有请求，将 [HttpResponse] 填入 [responses]。
     * 3. 在 [ResponseScope] 中执行该类型的 response DSL，将响应解析为 [TResult]，再依次执行 [ParserScope.onResultRevise] 修订块。
     *
     * @param request 本次解析请求，其 [ParserRequest.type] 决定使用的 [TypeScope]。
     * @return 解析得到的 [TResult]；若 [enable] 为 false 则直接返回 [defTResult]。
     */
    internal open suspend fun execute(request: TRequest): TResult {
        configure = configure.copy(request = request)

        if (!enable) return defTResult

        // 1. 按当前请求类型构建需要的 HttpRequest 列表
        buildRequests(request)

        // 2. 发送网络请求，填充各 HttpRequest 的 response
        executeRequests()

        // 3. 在 ResponseScope 中执行响应解析 DSL
        return ResponseScope(responses).run {
            val result = request.typeScope.responseBlock(this, defTResult)
            parserScope.resultReviseBlockList.fold(result) { acc, block ->
                acc.block()
            }.apply {
                configure = configure.copy(result = this)
            }
        }
    }

    /**
     * 根据请求执行解析，并通过 [Flow] 发送 [ParserEvent]。
     *
     * 流程：按 [request.type] 选择 [TypeScope] → 构建 [HttpRequest] → 并发请求 → 按 response DSL 解析 → 执行结果修订 → 发送 [ParserEvent.Success] 或 [ParserEvent.Failure]。
     * 若 [enable] 为 false，不发起请求，直接以 [defTResult] 发送 [ParserEvent.Success]。
     *
     * @param request 解析请求，其 [ParserRequest.type] 决定使用的 [TypeScope]。
     * @return 冷 [Flow]，每次收集执行一次解析并发送一个 [ParserEvent.Success] 或 [ParserEvent.Failure]。
     * @throws IllegalArgumentException 若 [setup] 中未设置 id/baseUrl，或当前 [request.type] 未注册任何请求。
     */
    fun request(request: TRequest): Flow<ParserEvent<TResult>> = callbackFlow {
        val result = execute(request)
        val errors = responses.mapNotNull { it.value.error }
        if (errors.isEmpty()) {
            send(ParserEvent.Success(result))
        } else {
            send(ParserEvent.Failure(errors))
        }
        close()
        awaitClose { }
    }

    /**
     * 根据当前 [request] 和其 [TypeScope] 的 request DSL 构建 [HttpRequest] 列表并写入 [requests]。
     * 要求该类型至少注册过一次 html/json/xml 请求，否则抛 [IllegalArgumentException]。
     *
     * @param request 当前解析请求，用于在 [RequestScope] 中执行对应类型的 [TypeScope.request] DSL。
     */
    private fun buildRequests(request: TRequest) {

        // 清空
        requests.clear()

        // 先在 RequestScope 中执行注册的 request DSL，配置好 html/json 请求配置
        val requestScope = RequestScope().apply {
            request.typeScope.requestBlock(this, request)
        }

        require(requestScope.requests.isNotEmpty()) {
            "${request.type} 未注册任何请求，至少要在 request{...} 里注册一次 html{...} 或 json{...} 或 xml{...}"
        }

        // 使用 HttpRequestBuilder 构建 JSON 请求
        requestScope.requests.forEach { (key, requestConfig) ->
            HttpRequestBuilder(
                key, configure.baseUrl
            ).apply {
                requestConfig.block(this)
            }.build().also { httpRequest ->
                logger.d { "request: $httpRequest" }
                requests[key] = httpRequest
            }
        }


    }

    /**
     * 并发发送 [requests] 中所有 [HttpRequest]，并将得到的 [HttpResponse] 写入 [responses]。
     */
    private suspend fun executeRequests() {
        if (requests.isEmpty()) return

        val updated = coroutineScope {
            requests.values.map { request ->
                async { executeSingleRequest(request) }
            }.awaitAll()
        }

        updated.forEach { response ->
            logger.d { "response: $response".lineSequence().joinToString(" ") { it.trim() } }
            responses[response.request.key] = response
        }
    }

    /**
     * 执行单次 HTTP GET 请求并封装为 [HttpResponse]。
     * 成功时 [HttpResponse.error] 为 null；异常时 [HttpResponse.status] 为 [HttpStatusCode.InternalServerError]，[HttpResponse.error] 为异常。
     *
     * @param request 要发送的 [HttpRequest]。
     * @return 包含请求、状态码、响应体或异常信息的 [HttpResponse]。
     */
    private suspend fun executeSingleRequest(request: HttpRequest): HttpResponse {
        return try {
            val response = httpClient.get(request.url) {
                request.headers.forEach { (name, value) -> header(name, value) }
            }
            HttpResponse(
                request = request,
                status = response.status,
                error = null,
                body = response.bodyAsText(),
            )
        } catch (t: Throwable) {
            HttpResponse(
                request = request,
                status = HttpStatusCode.InternalServerError,
                error = t,
                body = "",
            )
        }
    }

}

