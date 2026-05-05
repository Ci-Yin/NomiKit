package ciyin.ai.integrate.image

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ParseHttpOriginTest {

    @Test
    fun `http 默认端口 80`() {
        val (h, p, https) = parseHttpOrigin("http://example.com")
        assertEquals("example.com", h)
        assertEquals(80, p)
        assertEquals(false, https)
    }

    @Test
    fun `https 默认端口 443`() {
        val (h, p, https) = parseHttpOrigin("https://example.com/")
        assertEquals("example.com", h)
        assertEquals(443, p)
        assertEquals(true, https)
    }

    @Test
    fun `显式端口`() {
        val (h, p, https) = parseHttpOrigin("http://127.0.0.1:7860")
        assertEquals("127.0.0.1", h)
        assertEquals(7860, p)
        assertEquals(false, https)
    }

    @Test
    fun `缺少 scheme 失败`() {
        assertFailsWith<IllegalArgumentException> {
            parseHttpOrigin("127.0.0.1:7860")
        }
    }
}
