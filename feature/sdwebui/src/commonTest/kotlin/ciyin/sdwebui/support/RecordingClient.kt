package ciyin.sdwebui.support

import ciyin.sdwebui.client.Client

/**
 * 测试专用的 [Client] 实现：
 * - 拦截每次 [request] 调用，将最终构建出的 [Client.Request] 顺序追加到 [requests]，便于断言；
 * - 按 [enqueue] / [enqueueSuccess] / [enqueueFailure] 入队顺序回放对应的 [Client.Response]；
 * - 当队列耗尽时返回 200 + 空字符串，避免在仅验证调用契约的场景被迫准备完整响应体。
 *
 * 通过这种方式可以在不依赖任何真实 HTTP 引擎的前提下验证 Service 层与 [Client] 抽象之间的契约。
 */
class RecordingClient : Client() {

    private val responses: ArrayDeque<Response> = ArrayDeque()

    /**
     * 已被 [request] 拦截到的请求清单，按调用顺序保留。
     */
    val requests: MutableList<Request> = mutableListOf()

    /**
     * 返回最近一次被记录的请求；若尚未发生任何调用则抛出 [IllegalStateException]。
     */
    val lastRequest: Request
        get() = requests.lastOrNull() ?: error("尚未记录任何请求调用")

    /**
     * 将一个完整 [Client.Response] 入队。
     */
    fun enqueue(response: Response) {
        responses.addLast(response)
    }

    /**
     * 入队一个 `isSuccess = true` 的响应。
     */
    fun enqueueSuccess(body: String = "") {
        responses.addLast(Response(isSuccess = true, body = body))
    }

    /**
     * 入队一个 `isSuccess = false` 的响应。
     */
    fun enqueueFailure(body: String = "") {
        responses.addLast(Response(isSuccess = false, body = body))
    }

    override suspend fun request(builder: RequestBuilder.() -> RequestBuilder): Response {
        val builtRequest = RequestBuilder().builder().build()
        requests += builtRequest
        return if (responses.isNotEmpty()) responses.removeFirst() else Response(isSuccess = true, body = "")
    }
}
