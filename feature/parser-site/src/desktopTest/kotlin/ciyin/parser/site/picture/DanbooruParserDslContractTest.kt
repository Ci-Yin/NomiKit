package ciyin.parser.site.picture

import ciyin.parser.core.ParserEvent
import ciyin.parser.core.picture.PictureParserType
import ciyin.parser.core.picture.model.PictureRequest
import ciyin.parser.core.picture.model.PictureResult
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Danbooru 生产解析器注册、请求 URL 与响应 DSL 的离线契约测试。
 */
class DanbooruParserDslContractTest {

    /** Pools 必须通过真实注册表解析 JSON 摘要并合并 HTML 首图。 */
    @Test
    fun poolsRegistrationExecutesPoolSummaryParser() = withLocalDanbooru { server ->
        val result = DanbooruParser(siteBaseUrl = server.baseUrl)
            .request(PictureRequest(type = PictureParserType.Pools, page = 1, search = "sample pool"))
            .first()
            .successResult()

        assertEquals(1, result.contents.size)
        assertEquals(42, result.contents.single().poolSummary?.poolId)
        assertEquals("sample_pool", result.contents.single().poolSummary?.title)
        assertEquals("https://img.test/360x360/cover.jpg", result.contents.single().thumbnailUrl)
        val query = server.rawQueries.getValue("/pools/gallery.json")
        assertTrue(query.contains("search%5Bname_matches%5D=sample+pool"))
        assertFalse(query.contains("%255B"))
    }

    /** Pool 必须通过真实注册表解析单个画集详情中的帖子，而不是摘要数组。 */
    @Test
    fun poolRegistrationExecutesPoolDetailParser() = withLocalDanbooru { server ->
        val result = DanbooruParser(siteBaseUrl = server.baseUrl)
            .request(PictureRequest(type = PictureParserType.Pool, id = "42"))
            .first()
            .successResult()

        assertEquals(1, result.contents.size)
        assertEquals(901, result.contents.single().id)
        assertNull(result.contents.single().poolSummary)
    }

    /** 在本机临时服务内执行测试并确保资源释放。 */
    private fun withLocalDanbooru(block: suspend (LocalDanbooruServer) -> Unit) = runBlocking {
        val server = LocalDanbooruServer()
        server.start()
        try {
            block(server)
        } finally {
            server.stop()
        }
    }

    /** 提取解析成功结果，失败事件直接终止测试。 */
    private fun ParserEvent<PictureResult>.successResult(): PictureResult = when (this) {
        is ParserEvent.Success -> result
        is ParserEvent.Failure -> error(errors)
    }
}

/**
 * 为 Danbooru DSL 测试提供固定 HTML/JSON 响应的本机 HTTP 服务。
 */
private class LocalDanbooruServer {

    /** 已收到请求的原始 query，按路径索引。 */
    val rawQueries = ConcurrentHashMap<String, String>()

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

    /** 按请求路径返回固定测试响应并记录原始 query。 */
    private fun HttpExchange.respond() {
        rawQueries[requestURI.path] = requestURI.rawQuery.orEmpty()
        val body = when (requestURI.path) {
            "/pools/gallery" -> """
                <div class="posts-container">
                  <article class="post-preview" data-id="900">
                    <a class="post-preview-link" href="/pools/42">
                      <img src="https://img.test/180x180/cover.jpg" width="300" height="200">
                    </a>
                  </article>
                </div>
            """.trimIndent()
            "/pools/gallery.json" -> """[{"id":42,"name":"sample_pool","post_count":3}]"""
            "/pools/42" -> """
                <div class="posts-container">
                  <article class="post-preview" data-id="901" data-tags="safe">
                    <a class="post-preview-link" href="/posts/901">
                      <img src="https://img.test/180x180/post.jpg" width="200" height="300">
                    </a>
                    <p class="desc">detail post</p>
                  </article>
                </div>
            """.trimIndent()
            "/pools/42.json" -> """{"id":42,"name":"sample_pool","post_count":3}"""
            else -> error("未注册测试路径：${requestURI.path}")
        }
        val bytes = body.encodeToByteArray()
        responseHeaders.add("Content-Type", "text/html; charset=utf-8")
        sendResponseHeaders(200, bytes.size.toLong())
        responseBody.use { output -> output.write(bytes) }
    }
}
