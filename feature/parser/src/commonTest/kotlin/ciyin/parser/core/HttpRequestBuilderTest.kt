package ciyin.parser.core

import io.ktor.http.encodedPath
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * HTTP 请求 URL 构造契约测试。
 */
class HttpRequestBuilderTest {

    /** URLBuilder DSL 必须继承基础地址的协议、主机和非默认端口。 */
    @Test
    fun urlBuilderDslPreservesBaseProtocolHostAndPort() {
        val request = HttpRequestBuilder(
            key = 0,
            baseUrl = "http://127.0.0.1:43210",
        ).apply {
            url {
                encodedPath = "/mirror/posts"
            }
        }.build()

        assertEquals("http", request.url.protocol.name)
        assertEquals("127.0.0.1", request.url.host)
        assertEquals(43210, request.url.port)
        assertEquals("/mirror/posts", request.url.encodedPath)
    }

    /** 相对路径和 query 必须保留基础地址的非默认端口，并只编码一次参数名。 */
    @Test
    fun relativeUrlPreservesBasePortAndEncodesQueryKeyOnce() {
        val request = HttpRequestBuilder(
            key = 0,
            baseUrl = "http://127.0.0.1:43210",
        ).apply {
            url(
                path = "/pools/gallery.json",
                parameters = mapOf("search[name_matches]" to "sample pool"),
            )
        }.build()

        assertEquals(43210, request.url.port)
        assertTrue(request.url.encodedQuery.contains("search%5Bname_matches%5D=sample+pool"))
        assertFalse(request.url.encodedQuery.contains("%255B"))
    }
}
