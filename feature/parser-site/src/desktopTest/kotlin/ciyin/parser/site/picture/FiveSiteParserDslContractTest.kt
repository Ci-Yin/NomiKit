package ciyin.parser.site.picture

import ciyin.parser.core.ParserEvent
import ciyin.parser.core.picture.PictureParser
import ciyin.parser.core.picture.PictureParserType
import ciyin.parser.core.picture.model.PictureRequest
import ciyin.parser.core.picture.model.PictureResult
import ciyin.parser.site.PictureSiteId
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.net.InetSocketAddress
import java.net.URLDecoder
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** 五站真实 Feed/Detail DSL、相邻分页边界与错误载荷的离线契约。 */
class FiveSiteParserDslContractTest {

    /** 四个 Booru 站点必须区分 HTML 偏移与 DAPI 页码，并连续取得相邻的 42 条帖子。 */
    @Test
    fun booruFeedUsesAdjacentDapiPagesAndHtmlOffsets() = runBlocking {
        for (site in booruSites) {
            withServer(site = site) { server ->
                val parser = server.parser()
                val pages = (1..3).map { page ->
                    parser.request(
                        PictureRequest(
                            type = PictureParserType.Feed,
                            page = page,
                            tags = listOf("blue_hair", "safe"),
                        )
                    ).first().successResult()
                }
                pages.forEachIndexed { index, result ->
                    assertEquals(
                        (index * 42 + 1..(index + 1) * 42).map(Int::toString),
                        result.contents.map { it.id })
                    assertEquals(index + 1, result.pagination.page)
                    assertEquals(3, result.pagination.totalPages)
                    assertEquals(index < 2, result.pagination.hasNext)
                    assertTrue(result.contents.all { it.site == site.site && it.originalUrl.isNotBlank() })
                }
                pages.zipWithNext().forEach { (previous, next) ->
                    assertEquals(
                        previous.contents.last().id.toInt() + 1,
                        next.contents.first().id.toInt()
                    )
                    assertTrue(previous.contents.map { it.id }
                        .intersect(next.contents.map { it.id }.toSet()).isEmpty())
                }
                val html = server.requests.filter { it.query["page"] == "post" }
                val json = server.requests.filter { it.query["page"] == "dapi" }
                assertEquals(listOf("0", "42", "84"), html.map { it.query["pid"] })
                assertEquals(listOf("0", "1", "2"), json.map { it.query["pid"] })
                assertEquals(6, server.requests.size)
                server.requests.forEach { request ->
                    assertEquals("/index.php", request.path)
                    assertEquals("blue_hair safe", request.query["tags"])
                }
                assertTrue(html.all { it.query["s"] == "list" })
                assertTrue(json.all { it.query["s"] == "post" && it.query["q"] == "index" && it.query["json"] == "1" && it.query["limit"] == "42" })
            }
        }
    }

    /** 五站详情必须按 ID 查询而不受列表页码干扰，并映射身份与媒体字段。 */
    @Test
    fun fiveSiteDetailsMapIdentityAndMedia() = runBlocking {
        for (site in fiveSites) {
            withServer(site = site) { server ->
                val result = server.parser().request(
                    PictureRequest(
                        type = PictureParserType.Detail,
                        id = "901",
                        page = 2,
                    )
                ).first().successResult()
                val picture = result.contents.single()
                assertEquals("901", picture.id)
                assertEquals(site.site, picture.site)
                assertEquals("https://img.test/901.jpg", picture.originalUrl)
                assertEquals(640, picture.width)
                assertEquals(480, picture.height)
                assertEquals(listOf("blue_hair"), picture.tags.map { it.tag })
                assertEquals(2, server.requests.size)
                when (site) {
                    PictureSiteId.Zerochan -> {
                        assertTrue(
                            picture.children.isEmpty(),
                            "Zerochan 不应把当前文件名伪造为关联子图"
                        )
                        assertTrue(server.requests.all { it.path == "/901" })
                        assertEquals(1, server.requests.count { "json" in it.query })
                    }

                    else -> {
                        assertTrue(server.requests.all { it.path == "/index.php" && it.query["id"] == "901" })
                        assertEquals(
                            "view",
                            server.requests.single { it.query["page"] == "post" }.query["s"]
                        )
                        assertTrue("pid" !in server.requests.single { it.query["page"] == "post" }.query)
                        assertEquals(
                            "0",
                            server.requests.single { it.query["page"] == "dapi" }.query["pid"]
                        )
                    }
                }
            }
        }
    }

    /** Zerochan 标签路径与页码必须传到 HTML/JSON 两路，返回相邻且不重叠的列表。 */
    @Test
    fun zerochanFeedUsesTagPathAndAdjacentPages() = runBlocking {
        withServer(site = PictureSiteId.Zerochan) { server ->
            val parser = server.parser()
            val pages = (1..2).map { page ->
                parser.request(
                    PictureRequest(
                        type = PictureParserType.Feed,
                        page = page,
                        tags = listOf("Blue Hair", "Safe"),
                    )
                ).first().successResult()
            }
            assertEquals(listOf("1", "2"), pages.flatMap { it.contents }.map { it.id })
            assertEquals(listOf(1, 2), pages.map { it.pagination.page })
            assertTrue(server.requests.all { it.path == "/blue+hair,safe" })
            assertEquals(
                listOf("1", "2"),
                server.requests.filter { "json" in it.query }.map { it.query["p"] })
            assertEquals(
                listOf("1", "2"),
                server.requests.filter { "json" !in it.query }.map { it.query["p"] })
        }
    }

    /** HTTP 200 中的错误对象、空文本与错误类型不能冒充空成功，包括详情。 */
    @Test
    fun errorPayloadsFailInsteadOfProducingEmptySuccess() = runBlocking {
        for (site in fiveSites) {
            for (payload in listOf(
                "",
                "{}",
                """{"success":false,"message":"login required"}""",
                """{"error":"rate limited"}"""
            )) {
                withServer(site = site, jsonOverride = payload) { server ->
                    val parser = server.parser()
                    for (type in listOf(PictureParserType.Feed, PictureParserType.Detail)) {
                        val event = parser.request(PictureRequest(type = type, id = "901")).first()
                        assertTrue(
                            event is ParserEvent.Failure,
                            "$site/$type 错误载荷不得成功：$payload"
                        )
                        assertTrue(event.errors.isNotEmpty())
                    }
                }
            }
        }
        val malformed = mapOf(
            PictureSiteId.Gelbooru to listOf(
                """{"post":{}}""",
                """{"post":null}""",
                """{"post":[{}]}"""
            ),
            PictureSiteId.Zerochan to listOf(
                """{"items":{}}""",
                """{"items":null}""",
                """{"items":[{}]}"""
            ),
        )
        for ((site, payloads) in malformed) {
            for (payload in payloads) {
                withServer(site = site, jsonOverride = payload) { server ->
                    val event =
                        server.parser().request(PictureRequest(type = PictureParserType.Feed))
                            .first()
                    assertTrue(event is ParserEvent.Failure, "$site 非法数据结构不得成功：$payload")
                }
            }
        }
    }

    /** 合法空列表仍是成功，不应因收紧错误载荷校验而拒绝无搜索结果。 */
    @Test
    fun explicitEmptyFeedsRemainSuccessful() = runBlocking {
        val emptyPayloads = mapOf(
            PictureSiteId.Gelbooru to listOf(
                "[]",
                """{"post":[]}""",
                """{"@attributes":{"count":0}}"""
            ),
            PictureSiteId.Hypnohub to listOf("[]"),
            PictureSiteId.Xbooru to listOf("[]"),
            PictureSiteId.Safebooru to listOf("[]"),
            PictureSiteId.Zerochan to listOf("""{"items":[]}"""),
        )
        for ((site, payloads) in emptyPayloads) {
            for (payload in payloads) {
                withServer(site = site, jsonOverride = payload) { server ->
                    val result =
                        server.parser().request(PictureRequest(type = PictureParserType.Feed))
                            .first().successResult()
                    assertTrue(result.contents.isEmpty())
                }
            }
        }
    }

    /** 用本机 HTTP fixture 服务穿过生产请求链，结束后关闭监听端口。 */
    private suspend fun withServer(
        site: PictureSiteId,
        jsonOverride: String? = null,
        block: suspend (FiveSiteFixtureServer) -> Unit,
    ) {
        val server = FiveSiteFixtureServer(site = site, jsonOverride = jsonOverride)
        server.start()
        try {
            block(server)
        } finally {
            server.stop()
        }
    }

    /** 提取成功事件，保留错误原因供测试诊断。 */
    private fun ParserEvent<PictureResult>.successResult(): PictureResult = when (this) {
        is ParserEvent.Success -> result
        is ParserEvent.Failure -> error(errors)
    }

    /** 当前批准接入、复用 Booru DAPI 的四站集合。 */
    private val booruSites = listOf(
        PictureSiteId.Gelbooru,
        PictureSiteId.Hypnohub,
        PictureSiteId.Xbooru,
        PictureSiteId.Safebooru
    )

    /** 本次批准接入的五站，不包含未实现的站点。 */
    private val fiveSites = booruSites + PictureSiteId.Zerochan
}

/** 按真实站点请求参数选取确定性响应的离线服务，不访问外网。 */
private class FiveSiteFixtureServer(
    /** 当前 fixture 对应的站点。 */
    private val site: PictureSiteId,
    /** 用于错误及空列表用例的 JSON 覆盖响应。 */
    private val jsonOverride: String?,
) {
    /** 记录 HTML 和 JSON 请求，允许生产解析器并发发送。 */
    val requests = CopyOnWriteArrayList<FiveSiteRequest>()

    /** 绑定临时本机端口的底层服务。 */
    private val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply {
        createContext("/") { exchange -> exchange.respond() }
    }

    /** 启动本机服务。 */
    fun start() = server.start()

    /** 关闭本机服务。 */
    fun stop() = server.stop(0)

    /** 使用生产解析器现有基础地址覆盖入口，不替换 DSL 或解析 helper。 */
    fun parser(): PictureParser {
        val baseUrl = "http://127.0.0.1:${server.address.port}"
        return when (site) {
            PictureSiteId.Gelbooru -> GelbooruParser(baseUrlOverride = baseUrl)
            PictureSiteId.Hypnohub -> HypnohubParser(baseUrlOverride = baseUrl)
            PictureSiteId.Xbooru -> XbooruParser(baseUrlOverride = baseUrl)
            PictureSiteId.Safebooru -> SafebooruParser(baseUrlOverride = baseUrl)
            PictureSiteId.Zerochan -> ZerochanParser(baseUrlOverride = baseUrl)
            else -> error("未批准的 fixture 站点：$site")
        }
    }

    /** 使用 DAPI 页码乘以 limit 选取连续帖子，错误使用 HTML offset 会触发边界断言。 */
    private fun HttpExchange.respond() {
        val query =
            requestURI.rawQuery.orEmpty().split('&').filter(String::isNotEmpty).associate { part ->
                URLDecoder.decode(part.substringBefore('='), "UTF-8") to
                        URLDecoder.decode(part.substringAfter('=', ""), "UTF-8")
            }
        requests += FiveSiteRequest(path = requestURI.path, query = query)
        val isJson = "json" in query
        val body = if (!isJson) {
            """<div class="pagination"><a href="?pid=84">3</a></div>"""
        } else {
            jsonOverride ?: responseJson(query = query, path = requestURI.path)
        }
        // 空响应也须真实结束 body，避免本机 HTTP 服务把长度 0 误当成无终止分块。
        val bytes = body.encodeToByteArray()
        responseHeaders.add(
            "Content-Type",
            if (isJson) "application/json; charset=utf-8" else "text/html; charset=utf-8"
        )
        sendResponseHeaders(200, if (bytes.isEmpty()) -1L else bytes.size.toLong())
        responseBody.use { it.write(bytes) }
    }

    /** 按站点信封返回 Feed/Detail JSON，普通列表始终由查询中的真实分页参数决定。 */
    private fun responseJson(query: Map<String, String>, path: String): String {
        val detailId = query["id"]?.toIntOrNull() ?: path.removePrefix("/").toIntOrNull()
        val ids = if (detailId != null) listOf(detailId) else when (site) {
            PictureSiteId.Zerochan -> listOf(query["p"]?.toIntOrNull() ?: 1)
            else -> {
                val offset =
                    (query["pid"]?.toIntOrNull() ?: 0) * (query["limit"]?.toIntOrNull() ?: 42)
                (offset + 1..offset + 42).toList()
            }
        }
        val posts = ids.joinToString(",") { postJson(it) }
        return when (site) {
            PictureSiteId.Gelbooru -> """{"@attributes":{"count":126,"limit":42},"post":[$posts]}"""
            PictureSiteId.Zerochan -> if (detailId != null) postJson(detailId) else """{"items":[$posts]}"""
            else -> "[$posts]"
        }
    }

    /** 同一语义帖子分别使用 Zerochan 与 Booru 的真实字段类型。 */
    private fun postJson(id: Int): String = if (site == PictureSiteId.Zerochan) {
        """{"id":$id,"full":"https://img.test/$id.jpg","width":640,"height":480,"tags":["Blue Hair"]}"""
    } else {
        """{"id":$id,"file_url":"https://img.test/$id.jpg","preview_url":"https://img.test/thumb-$id.jpg","width":640,"height":480,"rating":"s","tags":"blue_hair","md5":"hash-$id","hash":"hash-$id"}"""
    }
}

/** 本机服务收到的已解码路径和查询参数。 */
private data class FiveSiteRequest(
    /** 未包含查询的请求路径。 */
    val path: String,
    /** 解码一次的查询参数，保留空值。 */
    val query: Map<String, String>,
)
