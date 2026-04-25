package ciyin.sdwebui

import ciyin.sdwebui.client.Client
import ciyin.sdwebui.client.DefaultClient
import ciyin.sdwebui.internal.extension.buildUrl
import ciyin.sdwebui.service.ControlNetService
import ciyin.sdwebui.service.ControlNetServiceImpl
import ciyin.sdwebui.service.CoreService
import ciyin.sdwebui.service.CoreServiceImpl
import ciyin.sdwebui.service.ReActorService
import ciyin.sdwebui.service.ReActorServiceImpl
import ciyin.sdwebui.service.StableDiffusionService
import ciyin.sdwebui.service.StableDiffusionServiceImpl
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

/**
 * 面向 AUTOMATIC1111 Stable Diffusion WebUI REST API 的入口类型。
 *
 * 通过 [Builder] 配置主机、端口、HTTPS 与可选的 [Client]，构建完成后可访问
 * [core]、[stableDiffusion]、[controlNet]、[reActor] 四类服务门面。
 */
class SdWebUi private constructor(
    private val host: String,
    private val port: Int,
    private val useHttps: Boolean,
    private val client: Client,
    private val json: Json,
) {

    private val baseUrl: String by lazy {
        buildUrl(host, port, useHttps)
    }

    /**
     * 队列与任务相关 API（如 `queue/status`）。
     */
    val core: CoreService by lazy {
        CoreServiceImpl(baseUrl, client, json)
    }

    /**
     * 文生图、图生图、后期处理与 RemBG 等 `sdapi/v1/` 能力。
     */
    val stableDiffusion: StableDiffusionService by lazy {
        StableDiffusionServiceImpl(baseUrl, client, json)
    }

    /**
     * ControlNet 扩展 REST 端点封装。
     */
    val controlNet: ControlNetService by lazy {
        ControlNetServiceImpl(baseUrl, client, json)
    }

    /**
     * ReActor 换脸扩展相关 API。
     */
    val reActor: ReActorService by lazy {
        ReActorServiceImpl(baseUrl, client, json)
    }

    /**
     * 用于组装 [SdWebUi] 实例的流式构建器。
     */
    class Builder {

        private var host: String = DEFAULT_HOST

        private var port: Int = DEFAULT_PORT

        private var useHttps: Boolean = false

        private var client: Client? = null

        /**
         * 指定 WebUI 服务所在主机名或 IP。
         */
        fun host(host: String) = apply {
            this.host = host
        }

        /**
         * 指定 WebUI 监听端口。
         */
        fun port(port: Int) = apply {
            this.port = port
        }

        /**
         * 是否使用 HTTPS 与服务器通信。
         */
        fun useHttps(useHttps: Boolean) = apply {
            this.useHttps = useHttps
        }

        /**
         * 注入自定义 [Client]；未调用时使用 [DefaultClient]。
         */
        fun client(client: Client) = apply {
            this.client = client
        }

        /**
         * 根据当前配置创建 [SdWebUi]。
         */
        fun build() = SdWebUi(
            host = host,
            port = port,
            useHttps = useHttps,
            client = client ?: DefaultClient(json),
            json = json,
        )

        @OptIn(ExperimentalSerializationApi::class)
        private val json: Json by lazy {
            Json {
                isLenient = false
                ignoreUnknownKeys = true
                encodeDefaults = true
                explicitNulls = false
            }
        }
    }

    companion object {

        /**
         * 未在 [Builder] 中指定主机时的默认地址（本机）。
         */
        const val DEFAULT_HOST: String = "127.0.0.1"

        /**
         * WebUI 常用默认端口。
         */
        const val DEFAULT_PORT: Int = 7860

        /**
         * [DefaultClient] 中 HTTP 请求超时时间（毫秒），与长耗时生图任务相匹配。
         */
        const val DEFAULT_TIMEOUT: Long = 50 * 60 * 1000
    }
}
