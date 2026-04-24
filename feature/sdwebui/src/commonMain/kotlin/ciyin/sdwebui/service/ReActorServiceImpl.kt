package ciyin.sdwebui.service

import ciyin.sdwebui.client.Client
import ciyin.sdwebui.client.Client.Companion.get
import ciyin.sdwebui.response.ReActorModelsResponse
import ciyin.sdwebui.response.ReActorUpscalersResponse
import kotlinx.serialization.json.Json

class ReActorServiceImpl(
    override val baseUrl: String,
    override val client: Client,
    override val json: Json,
) : Service(), ReActorService {

    override suspend fun getModels(): Result<ReActorModelsResponse> {
        return client.get(json, baseUrl, "reactor/models")
    }

    override suspend fun getUpscalers(): Result<ReActorUpscalersResponse> {
        return client.get(json, baseUrl, "reactor/upscalers")
    }
}
