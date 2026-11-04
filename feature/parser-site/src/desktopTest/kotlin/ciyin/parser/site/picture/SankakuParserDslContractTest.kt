package ciyin.parser.site.picture

import ciyin.parser.core.ParserEvent
import ciyin.parser.core.ParserHttpException
import ciyin.parser.core.picture.PictureParserType
import ciyin.parser.core.picture.model.PictureRequest
import ciyin.parser.core.picture.model.PictureResult
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.net.InetSocketAddress
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Sankaku 生产解析器注册、keyset 分页、详情请求和 HTTP 错误契约测试。
 */
class SankakuParserDslContractTest {

    /** 首页必须使用 keyset endpoint、固定 limit、站点请求头和标签，不得伪造 page 参数。 */
    @Test
    fun feedUsesKeysetCursorAndRequiredHeaders() = withServer { server ->
        val result = SankakuParser(apiBaseUrl = server.baseUrl)
            .request(
                PictureRequest(
                    type = PictureParserType.Feed,
                    tags = listOf("azur_lane"),
                ),
            )
            .first()
            .successResult()

        assertEquals(listOf("first-post"), result.contents.map { it.id })
        assertEquals("cursor-1", result.pagination.nextCursor)
        assertEquals(true, result.pagination.hasNext)
        assertEquals(1, result.pagination.page)

        val request = server.requests.single()
        assertEquals("/v2/posts/keyset", request.path)
        assertTrue(request.query.contains("limit=100"))
        assertTrue(request.query.contains("tags=azur_lane"))
        assertFalse(request.query.contains("page="))
        assertEquals("application/vnd.sankaku.api+json;v=2", request.accept)
        assertEquals("https://sankaku.app", request.origin)
    }

    /** 下一页必须只携带上一页的 meta.next，并且两页帖子集合不得重叠。 */
    @Test
    fun feedContinuesWithMetaNextWithoutPageParameter() = withServer { server ->
        val parser = SankakuParser(apiBaseUrl = server.baseUrl)
        val firstPage = parser.request(
            PictureRequest(type = PictureParserType.Feed),
        ).first().successResult()
        val secondPage = parser.request(
            PictureRequest(
                type = PictureParserType.Feed,
                page = 2,
                cursor = firstPage.pagination.nextCursor,
            ),
        ).first().successResult()

        val firstIds = firstPage.contents.map { it.id }.toSet()
        val secondIds = secondPage.contents.map { it.id }.toSet()
        assertTrue(firstIds.isNotEmpty())
        assertTrue(secondIds.isNotEmpty())
        assertNotEquals(firstIds, secondIds)
        assertTrue(firstIds.intersect(secondIds).isEmpty())
        assertEquals(2, secondPage.pagination.page)
        assertEquals(null, secondPage.pagination.nextCursor)
        assertEquals(false, secondPage.pagination.hasNext)

        val continuation = server.requests[1]
        assertEquals("/v2/posts/keyset", continuation.path)
        assertTrue(continuation.query.contains("next=cursor-1"))
        assertTrue(continuation.query.contains("limit=100"))
        assertFalse(continuation.query.contains("page="))
    }

    /** 详情动作必须按帖子字符串 ID 查询并保留媒体字段。 */
    @Test
    fun detailUsesPostIdAndMapsRemoteFields() = withServer { server ->
        val result = SankakuParser(apiBaseUrl = server.baseUrl)
            .request(
                PictureRequest(
                    type = PictureParserType.Detail,
                    id = "detail-post",
                ),
            )
            .first()
            .successResult()

        val picture = result.contents.single()
        assertEquals("detail-post", picture.id)
        assertEquals("https://img.test/detail.png", picture.originalUrl)
        assertEquals("png", picture.fileExt)
        assertEquals("https://sankaku.app/posts/detail-post", picture.postUrl)
        assertEquals("https://source.test/detail-post", picture.sourceUrl)
        assertEquals("detail-post", server.requests.single().query.substringAfter("id="))
    }

    /** JSON 数字 ID 也必须转换为不丢失信息的字符串 ID。 */
    @Test
    fun feedMapsNumericIdAsString() = withServer(numericId = true) { server ->
        val result = SankakuParser(apiBaseUrl = server.baseUrl)
            .request(PictureRequest(type = PictureParserType.Feed))
            .first()
            .successResult()

        val picture = result.contents.single()
        assertEquals("12345", picture.id)
    }

    /** 非 2xx 响应必须进入 Failure，并保留 429 与限流响应头。 */
    @Test
    fun rateLimitResponseIsFailureWithHeaders() = withServer(rateLimited = true) { server ->
        val event = SankakuParser(apiBaseUrl = server.baseUrl)
            .request(PictureRequest(type = PictureParserType.Feed))
            .first()

        val failure = when (event) {
            is ParserEvent.Failure -> event
            is ParserEvent.Success -> error("429 响应不应产生成功事件")
        }
        val exception = failure.errors.single() as? ParserHttpException
            ?: error("429 响应错误类型不正确：${failure.errors}")
        assertEquals(429, exception.status.value)
        assertEquals(
            listOf("1700000000"),
            exception.responseHeaders.entries
                .first { it.key.equals("X-RateLimit-Reset", ignoreCase = true) }
                .value,
        )
        assertTrue(exception.message.orEmpty().contains("X-RateLimit-Reset=1700000000"))
        assertEquals("{\"error\":\"rate limited\"}", exception.responseBody)
        assertEquals("/v2/posts/keyset", server.requests.single().path)
    }

    /** 在本机临时 HTTP 服务内执行测试并保证资源释放。 */
    private fun withServer(
        rateLimited: Boolean = false,
        numericId: Boolean = false,
        block: suspend (LocalSankakuServer) -> Unit,
    ) = runBlocking {
        val server = LocalSankakuServer(rateLimited, numericId)
        server.start()
        try {
            block(server)
        } finally {
            server.stop()
        }
    }

    /** 提取解析成功结果，失败事件直接终止当前测试。 */
    private fun ParserEvent<PictureResult>.successResult(): PictureResult = when (this) {
        is ParserEvent.Success -> result
        is ParserEvent.Failure -> error(errors)
    }
}

/**
 * 为 Sankaku DSL 测试提供固定 keyset、详情和限流响应的本机 HTTP 服务。
 */
private class LocalSankakuServer(
    private val rateLimited: Boolean,
    private val numericId: Boolean,
) {

    /** 已收到的请求记录，顺序与服务收到的请求顺序一致。 */
    val requests = CopyOnWriteArrayList<SankakuRequest>()

    /** 底层 JDK HTTP 服务。 */
    private val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply {
        createContext("/") { exchange -> exchange.respond() }
    }

    /** 服务基础 URL。 */
    val baseUrl: String get() = "http://127.0.0.1:${server.address.port}"

    /** 启动本机服务。 */
    fun start() = server.start()

    /** 停止本机服务。 */
    fun stop() = server.stop(0)

    /** 按请求路径和游标返回固定 API 响应。 */
    private fun HttpExchange.respond() {
        requests += SankakuRequest(
            path = requestURI.path,
            query = requestURI.rawQuery.orEmpty(),
            accept = requestHeaders.getFirst("Accept").orEmpty(),
            origin = requestHeaders.getFirst("Origin").orEmpty(),
        )

        val isRateLimited = rateLimited && requestURI.path == "/v2/posts/keyset"
        val body = when {
            isRateLimited -> """{"error":"rate limited"}"""
            requestURI.path == "/v2/posts/keyset" && requestURI.rawQuery.orEmpty()
                .contains("next=cursor-1") -> {
                keysetResponse(id = "second-post", next = null)
            }

            requestURI.path == "/v2/posts/keyset" -> {
                keysetResponse(id = "first-post", next = "cursor-1")
            }

            requestURI.path == "/v2/posts" -> detailResponse()
            else -> error("未注册测试路径：${requestURI.path}")
        }

        val bytes = body.encodeToByteArray()
        responseHeaders.add("Content-Type", "application/json; charset=utf-8")
        if (isRateLimited) {
            responseHeaders.add("X-RateLimit-Reset", "1700000000")
        }
        sendResponseHeaders(if (isRateLimited) 429 else 200, bytes.size.toLong())
        responseBody.use { output -> output.write(bytes) }
    }

    /** 生成一页 Sankaku keyset JSON。 */
    private fun keysetResponse(id: String, next: String?): String = """
        {
          "meta": {"next": ${next?.let { "\"$it\"" } ?: "null"}, "prev": null},
          "data": [${postJson(if (numericId) "12345" else id)}]
        }
    """.trimIndent()

    /** 生成 Sankaku 详情 JSON 数组。 */
    private fun detailResponse(): String = """
        [${postJson("detail-post", fileUrl = "https://img.test/detail.png", fileExt = "png")}]
    """.trimIndent()

    /** 生成满足 Sankaku DTO 映射契约的帖子 JSON。 */
    private fun postJson(
        id: String,
        fileUrl: String = "https://img.test/$id.jpg",
        fileExt: String = "jpg",
    ): String = """
        {
          "id": ${if (numericId && id == "12345") id else "\"$id\""},
          "rating": "s",
          "sample_url": "https://img.test/sample-$id.jpg",
          "preview_url": "https://img.test/preview-$id.jpg",
          "file_url": "$fileUrl",
          "file_ext": "$fileExt",
          "file_size": 1234,
          "width": 640,
          "height": 480,
          "md5": "md5-$id",
          "source": "https://source.test/$id",
          "tag_names": ["sample_tag"],
          "created_at": {"s": 1700000000, "n": 0}
        }
    """.trimIndent()
}

/**
 * 本地测试服务收到的 Sankaku 请求摘要。
 */
private data class SankakuRequest(
    val path: String,
    val query: String,
    val accept: String,
    val origin: String,
)
