package ciyin.ai.image.sdwebui.support

import ciyin.sdwebui.client.Client

/**
 * `ai-image-sdwebui-engine` 单测使用的轻量录制客户端。
 *
 * 作用：
 * - 记录每次发出的请求，便于断言路径与 payload 映射；
 * - 按入队顺序回放预设响应，避免真实网络依赖。
 *
 * 实现须**线程安全**：`SdWebUiImageEngine` 在 `async` 生图与轮询 `progress` 时会并发调用 [request]，
 * JVM 上二者可能落在不同线程；若不串行化，对 [responses] / [requests] 的访问会产生竞态，导致单测随机失败。
 */
internal class RecordingClient : Client() {

    private val responses: ArrayDeque<Response> = ArrayDeque()

    private val lock = Any()

    /**
     * 对 `sdapi/v1/progress` 的固定响应，不消耗 [responses] 队列。
     *
     * 生图与进度轮询并发时，若全部走 FIFO，GET 可能误取 POST 的 JSON 体；单测里可设此项模拟进度接口。
     */
    var progressStub: Response? = null

    /** 已记录的请求列表，按调用顺序排列。 */
    val requests: MutableList<Request> = mutableListOf()

    /** 最近一次请求。 */
    val lastRequest: Request
        get() = synchronized(lock) {
            requests.lastOrNull() ?: error("尚未记录任何请求")
        }

    /**
     * 入队一个完整响应。
     */
    fun enqueue(response: Response) {
        synchronized(lock) {
            responses.addLast(response)
        }
    }

    /**
     * 入队一个成功响应。
     */
    fun enqueueSuccess(body: String = "") {
        synchronized(lock) {
            responses.addLast(Response(isSuccess = true, body = body))
        }
    }

    /**
     * 入队一个失败响应。
     */
    fun enqueueFailure(body: String = "") {
        synchronized(lock) {
            responses.addLast(Response(isSuccess = false, body = body))
        }
    }

    override suspend fun request(builder: RequestBuilder.() -> RequestBuilder): Response {
        val request = RequestBuilder().builder().build()
        return synchronized(lock) {
            requests += request
            // 进度轮询与 txt2img 并发时，若此处误走 FIFO，会抢走为 POST 入队的 JSON，导致单测随机失败。
            if (request.path == "sdapi/v1/progress") {
                return@synchronized progressStub
                    ?: Response(
                        isSuccess = true,
                        body = """{"progress":0,"eta_relative":0,"state":{"skipped":false,"interrupted":false,"job":"","job_count":0,"job_timestamp":"0","job_no":0,"sampling_step":0,"sampling_steps":0},"current_image":null,"textinfo":null}""",
                    )
            }
            return@synchronized if (responses.isNotEmpty()) responses.removeFirst() else Response(
                true,
                ""
            )
        }
    }
}
