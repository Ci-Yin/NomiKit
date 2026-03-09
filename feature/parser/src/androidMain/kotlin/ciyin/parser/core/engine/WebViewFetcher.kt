package ciyin.parser.core.engine

import android.content.Context
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.HttpClientEngineCapability
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpProtocolVersion
import io.ktor.util.AttributeKey
import io.ktor.util.date.GMTDate
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.InternalAPI
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

/**
 * WebView Engine 配置
 */
class WebViewEngineConfig : HttpClientEngineConfig() {
    var context: Context? = null
    var fallbackEngine: HttpClientEngineFactory<*>? = null
    var executeScript: ((String) -> String?)? = null

    /**
     * 设置Android Context
     */
    fun context(context: Context) {
        this.context = context
    }

    /**
     * 设置备用引擎（用于非WebView请求）
     */
    fun fallbackEngine(engine: HttpClientEngineFactory<*>) {
        this.fallbackEngine = engine
    }

    /**
     * 设置自定义脚本执行逻辑
     */
    fun scriptExecutor(block: (url: String) -> String?) {
        this.executeScript = block
    }
}

/**
 * 用于标识是否使用WebView加载的属性键
 */
val WebViewLoadingKey = AttributeKey<Boolean>("WebViewLoading")

/**
 * WebView HttpClientEngine
 */
class WebViewEngine(
    override val config: WebViewEngineConfig
) : HttpClientEngine {

    private val context = requireNotNull(config.context) {
        "Context must be provided for WebViewEngine"
    }

    private val loader = WebViewContentLoader(context)

    private val scriptExecutor = config.executeScript

    @Volatile
    private var closed = false

    // 备用引擎，用于普通HTTP请求
    private val fallbackEngine: HttpClientEngine? = config.fallbackEngine?.create {
        pipelining = config.pipelining
    }

    override val dispatcher: CoroutineDispatcher = Dispatchers.IO

    override val coroutineContext: CoroutineContext = dispatcher

    override val supportedCapabilities: Set<HttpClientEngineCapability<*>> = emptySet()

    @InternalAPI
    override suspend fun execute(data: HttpRequestData): HttpResponseData {
        // 检查是否使用WebView
        val useWebView = data.attributes[WebViewLoadingKey]

        return if (useWebView) {
            // 使用WebView加载，不发起HTTP请求
            executeWithWebView(data)
        } else {
            // 使用备用引擎发起正常HTTP请求
            fallbackEngine?.execute(data) ?: throw IllegalStateException(
                "No fallback engine configured for non-WebView requests"
            )
        }
    }

    private suspend fun executeWithWebView(data: HttpRequestData): HttpResponseData {
        val url = data.url.toString()
        val script = scriptExecutor?.invoke(url)

        // 使用WebView加载内容
        val multiResponse = loader.loadDynamicContent(url, script)

        // 如果加载失败，抛出异常
        if (multiResponse.error != null) {
            throw multiResponse.error
        }

        // 构造响应数据
        return HttpResponseData(
            statusCode = multiResponse.status,
            requestTime = GMTDate(),
            headers = Headers.build {
                append(HttpHeaders.ContentType, "text/html; charset=utf-8")
                append(HttpHeaders.ContentLength, multiResponse.body.length.toString())
            },
            version = HttpProtocolVersion.HTTP_1_1,
            body = ByteReadChannel(multiResponse.body.toByteArray()),
            callContext = data.executionContext
        )
    }

    override fun close() {
        if (closed) return
        closed = true
        fallbackEngine?.close()
        loader.close()
    }

    companion object : HttpClientEngineFactory<WebViewEngineConfig> {
        override fun create(block: WebViewEngineConfig.() -> Unit): HttpClientEngine {
            val config = WebViewEngineConfig().apply(block)
            return WebViewEngine(config)
        }
    }
}

/**
 * 扩展函数：为特定请求启用WebView加载
 */
fun HttpRequestBuilder.useWebView() {
    attributes.put(WebViewLoadingKey, true)
}
