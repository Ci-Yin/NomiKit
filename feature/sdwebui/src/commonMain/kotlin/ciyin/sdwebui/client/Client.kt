package ciyin.sdwebui.client

import io.ktor.util.reflect.*
import kotlinx.serialization.json.Json

/**
 * SD WebUI HTTP 访问的抽象客户端，屏蔽具体 Ktor 引擎实现。
 *
 * 业务侧应通过伴生对象提供的 `get`、`post`、`body`、`load` 等扩展简化调用，
 * 或在测试中替换为假实现（如 RecordingClient）。
 */
abstract class Client {

    /**
     * 执行一次 HTTP 请求；由平台 [DefaultClient] 或测试替身实现。
     */
    abstract suspend fun request(builder: RequestBuilder.() -> RequestBuilder): Response

    /**
     * 以 DSL 形式拼装单次请求的基址、路径、方法与可选 JSON 请求体。
     */
    class RequestBuilder {

        private var baseUrl: String = ""

        private var path: String = ""

        private var method: Method = Method.GET

        var body: Any? = null

        var bodyType: TypeInfo? = null

        /**
         * 设置请求基址（不含尾斜杠），例如 `http://127.0.0.1:7860`。
         */
        fun baseUrl(baseUrl: String) = apply {
            this.baseUrl = baseUrl
        }

        /**
         * 设置相对路径，例如 `sdapi/v1/txt2img`。
         */
        fun path(path: String) = apply {
            this.path = path
        }

        /**
         * 设置 HTTP 方法，默认为 [Method.GET]。
         */
        fun method(method: Method) = apply {
            this.method = method
        }

        /**
         * 生成不可变的 [Request] 快照。
         */
        fun build() = Request(
            baseUrl = baseUrl,
            path = path,
            method = method,
            body = body,
            bodyType = bodyType,
        )
    }

    /**
     * 已由 [RequestBuilder] 物化后的请求描述。
     */
    data class Request(
        val baseUrl: String,
        val path: String,
        val method: Method,
        val body: Any?,
        val bodyType: TypeInfo?,
    )

    /**
     * 原始 HTTP 层结果：是否 2xx 与响应体字符串（通常为 JSON）。
     */
    data class Response(
        val isSuccess: Boolean,
        val body: String,
    )

    /**
     * 表示 HTTP 非成功或业务层约定的失败载体，携带原始响应体文本。
     */
    data class Error(val body: String) : Throwable(body)

    /**
     * 支持的 HTTP 动词子集。
     */
    enum class Method {
        /** GET 请求。 */
        GET,

        /** POST 请求。 */
        POST
    }

    companion object {

        /**
         * 对指定路径发起 GET，并将 JSON 响应体反序列化为 [T]。
         */
        suspend inline fun <reified T> Client.get(json: Json, baseUrl: String, path: String): Result<T> = request {
            baseUrl(baseUrl)
            path(path)
        }.load(json)

        /**
         * 在 [builder] 中配置路径与 body 后，以 POST 发起请求并反序列化响应。
         */
        suspend inline fun <reified T> Client.post(
            json: Json,
            noinline builder: RequestBuilder.() -> RequestBuilder,
        ): Result<T> = request {
            builder()
            method(Method.POST)
        }.load(json)

        /**
         * 将 [Response] 解析为 [Result]：[Unit] 类型在成功或失败体下均视为成功；
         * 其它类型在 `isSuccess == false` 时返回 [Error]；否则尝试 [Json.decodeFromString]。
         */
        inline fun <reified T> Response.load(json: Json): Result<T> {
            if (T::class == Unit::class) {
                return Result.success(Unit as T)
            }
            if (!isSuccess) {
                return Result.failure(Error(body))
            }
            return runCatching {
                json.decodeFromString(body)
            }
        }

        /**
         * 为 [RequestBuilder] 设置序列化请求体及其 [TypeInfo]，供 Ktor 编码使用。
         */
        inline fun <reified T> RequestBuilder.body(body: T): RequestBuilder = apply {
            this.body = body
            this.bodyType = typeInfo<T>()
        }
    }
}
