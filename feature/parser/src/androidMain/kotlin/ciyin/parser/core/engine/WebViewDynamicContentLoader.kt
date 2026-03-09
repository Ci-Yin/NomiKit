package ciyin.parser.core.engine

import android.content.Context
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import ciyin.parser.core.HttpRequest
import ciyin.parser.core.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import kotlin.coroutines.resume

/**
 * 使用 WebView 加载动态内容 - 协程版本
 */
class WebViewContentLoader(private val context: Context, val waitTime: Long = 200L) {

    private var webView: WebView? = null
    private val mainScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val httpRequest = HttpRequest(
        key = 0,
        baseUrl = "",
        url = Url(""),
        headers = mapOf(),
        isMockWeb = false
    )

    /**
     * 加载动态网页内容
     */
    suspend fun loadDynamicContent(
        url: String,
        executeScript: String? = null,
    ): HttpResponse = suspendCancellableCoroutine { continuation ->

        // 启动主线程协程来创建 WebView
        mainScope.launch {
            try {
                continuation.setupWebView(executeScript, url)
            } catch (e: Exception) {
                if (continuation.isActive) {
                    continuation.resume(
                        HttpResponse(
                            status = HttpStatusCode.NotFound,
                            body = "",
                            error = IOException("Failed to extract content", e),
                            request = httpRequest
                        )
                    )
                }
            }
        }

        // 设置取消回调
        continuation.invokeOnCancellation {
            cleanup()
        }
    }

    private fun CancellableContinuation<HttpResponse>.setupWebView(
        executeScript: String?,
        url: String
    ) {
        webView = WebView(this@WebViewContentLoader.context).apply {

            this.settings.apply {
                this.javaScriptEnabled = true
                this.domStorageEnabled = true
                this.loadWithOverviewMode = true
                this.useWideViewPort = true
                setSupportZoom(false)
                this.cacheMode = WebSettings.LOAD_NO_CACHE
                this.userAgentString =
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
            }

            this.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    // 启动协程来处理页面加载完成后的逻辑
                    mainScope.launch {
                        try {
                            // 等待 JavaScript 执行
                            delay(waitTime)

                            // 执行额外的脚本
                            executeScript?.let { script ->
                                executeJavaScript(script)
                                //delay(1000L) // 等待脚本执行完成
                            }

                            // 提取内容
                            extractContent(this@setupWebView)
                        } catch (e: Exception) {
                            if (isActive) {
                                this@setupWebView.resume(
                                    HttpResponse(
                                        status = HttpStatusCode.NotFound,
                                        body = "",
                                        error = IOException("Failed to extract content", e),
                                        request = httpRequest
                                    )
                                )
                            }
                        }
                    }
                }

                override fun onReceivedError(
                    view: WebView?,
                    errorCode: Int,
                    description: String?,
                    failingUrl: String?,
                ) {
                    if (isActive) {
                        this@setupWebView.resume(
                            HttpResponse(
                                status = HttpStatusCode.fromValue(errorCode),
                                body = "",
                                error = IOException(
                                    "Failed to extract content with error code $errorCode: $description for url $failingUrl"
                                ),
                                request = httpRequest
                            )
                        )
                    }
                }
            }

            loadUrl(url)
        }
    }

    /**
     * 执行 JavaScript 脚本
     */
    private suspend fun executeJavaScript(script: String) =
        suspendCancellableCoroutine<String?> { cont ->
            webView?.evaluateJavascript(script) { result ->
                if (cont.isActive) {
                    cont.resume(result)
                }
            }
        }

    /**
     * 提取网页内容
     */
    private suspend fun extractContent(continuation: CancellableContinuation<HttpResponse>) {
        try {
            val html = suspendCancellableCoroutine<String?> { cont ->
                webView?.evaluateJavascript("document.documentElement.outerHTML") { result ->
                    if (cont.isActive) {
                        cont.resume(result)
                    }
                }
            }

            val cleanHtml = html?.removeSurrounding("\"")
                ?.replace("\\\"", "\"")
                ?.replace("\\n", "\n")
                ?.decodeAll()
                ?: ""
            if (continuation.isActive) {
                continuation.resume(
                    HttpResponse(
                        body = cleanHtml,
                        status = HttpStatusCode.OK,
                        error = null,
                        request = httpRequest
                    )
                )
            }
        } catch (e: Exception) {
            if (continuation.isActive) {
                continuation.resume(
                    HttpResponse(
                        status = HttpStatusCode.NotFound,
                        body = "",
                        error = IOException("Failed to extract content", e),
                        request = httpRequest
                    )
                )
            }
        } finally {
            // 清理资源
            cleanup()
        }
    }

    /**
     * 清理资源
     */
    fun cleanup() {
        mainScope.launch {
            webView?.destroy()
            webView = null
        }
    }

    /**
     * 关闭协程作用域
     */
    fun close() {
        mainScope.cancel()
        cleanup()
    }
}
