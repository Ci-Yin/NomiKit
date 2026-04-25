package ciyin.ai.image.sdwebui.support

import ciyin.sdwebui.client.Client

/**
 * `ai-image-sdwebui-engine` 单测使用的轻量录制客户端。
 *
 * 作用：
 * - 记录每次发出的请求，便于断言路径与 payload 映射；
 * - 按入队顺序回放预设响应，避免真实网络依赖。
 */
internal class RecordingClient : Client() {

    private val responses: ArrayDeque<Response> = ArrayDeque()

    /**
     * 对 `sdapi/v1/progress` 的固定响应，不消耗 [responses] 队列。
     *
     * 生图与进度轮询并行时，若全部走 FIFO，GET 可能误取 POST 的 JSON 体；单测里可设此项模拟进度接口。
     */
    var progressStub: Response? = null

    /** 已记录的请求列表，按调用顺序排列。 */
    val requests: MutableList<Request> = mutableListOf()

    /** 最近一次请求。 */
    val lastRequest: Request
        get() = requests.lastOrNull() ?: error("尚未记录任何请求")

    /**
     * 入队一个完整响应。
     */
    fun enqueue(response: Response) {
        responses.addLast(response)
    }

    /**
     * 入队一个成功响应。
     */
    fun enqueueSuccess(body: String = "") {
        responses.addLast(Response(isSuccess = true, body = body))
    }

    /**
     * 入队一个失败响应。
     */
    fun enqueueFailure(body: String = "") {
        responses.addLast(Response(isSuccess = false, body = body))
    }

    override suspend fun request(builder: RequestBuilder.() -> RequestBuilder): Response {
        val request = RequestBuilder().builder().build()
        requests += request
        if (request.path == "sdapi/v1/progress") {
            progressStub?.let { return it }
        }
        return if (responses.isNotEmpty()) responses.removeFirst() else Response(true, "")
    }
}
