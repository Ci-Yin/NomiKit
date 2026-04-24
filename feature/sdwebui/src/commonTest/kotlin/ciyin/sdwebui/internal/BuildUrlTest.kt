package ciyin.sdwebui.internal

import ciyin.sdwebui.internal.extension.buildUrl
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * [buildUrl] 内部工具的单元测试，覆盖 host/port/https 三种维度组合。
 */
class BuildUrlTest {

    @Test
    fun should_build_default_local_http_url() {
        val url = buildUrl(host = "127.0.0.1", port = 7860, useHttps = false)

        assertEquals("http://127.0.0.1:7860", url)
    }

    @Test
    fun should_build_https_url_when_use_https_is_true() {
        val url = buildUrl(host = "example.com", port = 443, useHttps = true)

        assertEquals("https://example.com", url, "443 是 https 默认端口，URLBuilder 应省略端口")
    }

    @Test
    fun should_keep_explicit_non_default_port() {
        val url = buildUrl(host = "example.com", port = 8443, useHttps = true)

        assertEquals("https://example.com:8443", url)
    }

    @Test
    fun should_omit_default_http_port_80() {
        val url = buildUrl(host = "example.com", port = 80, useHttps = false)

        assertEquals("http://example.com", url, "80 是 http 默认端口，URLBuilder 应省略端口")
    }
}
