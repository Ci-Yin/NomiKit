package ciyin.sdwebui.service

import ciyin.sdwebui.client.Client
import ciyin.sdwebui.client.Client.Companion.get
import ciyin.sdwebui.response.QueueResponse
import kotlinx.serialization.json.Json

/**
 * [CoreService] 的默认实现，请求 `queue/status` 等端点。
 */
class CoreServiceImpl(
    override val baseUrl: String,
    override val client: Client,
    override val json: Json,
) : Service(), CoreService {

    /**
     * 发起 GET `queue/status` 并反序列化为 [QueueResponse]。
     */
    override suspend fun getQueue(): Result<QueueResponse> {
        return client.get(json, baseUrl, "queue/status")
    }
}
