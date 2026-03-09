package ciyin.parser.core.engine

import androidx.test.platform.app.InstrumentationRegistry
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.url
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.InternalAPI
import io.ktor.utils.io.core.readText
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WebViewEngineTest {

    @Test
    fun useWebView_should_set_webview_loading_attribute() {
        val builder = HttpRequestBuilder()

        builder.useWebView()

        assertTrue(builder.attributes.contains(WebViewLoadingKey))
        val value = builder.attributes.getOrNull(WebViewLoadingKey)
        assertNotNull(value)
        assertTrue(value == true)
    }

    @Test
    fun mockWebEngine_should_apply_config_block() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        val engine = WebViewEngine.create {
            context(appContext)
            scriptExecutor { url -> "script for $url" }
        } as WebViewEngine

        val config = engine.config

        assertEquals(appContext, config.context)
        val output = config.executeScript?.invoke("https://example.com")
        assertEquals("script for https://example.com", output)
    }

    /**
     * 集成测试：通过 Ktor HttpClient + WebViewEngine 实际加载页面 HTML。
     *
     * 依赖真机 / 模拟器网络环境，以及 Android WebView 组件。
     */
    @OptIn(InternalAPI::class)
    @Test
    fun webview_engine_execute_should_load_page_html() = runTest {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        val engine = WebViewEngine(
            WebViewEngineConfig().apply {
                context(appContext)
            }
        )

        val requestBuilder = HttpRequestBuilder().apply {
            url("https://www.baidu.com/")
            useWebView()
        }

        val response = engine.execute(requestBuilder.build())

        assertEquals(HttpStatusCode.OK, response.statusCode)
        val channel = response.body as ByteReadChannel
        val bodyText = channel.readRemaining().readText()
        assertTrue(bodyText.isNotBlank())
        println(bodyText)
        engine.close()
    }
}

