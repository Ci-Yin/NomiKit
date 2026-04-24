package ciyin.sdwebui.service

import ciyin.sdwebui.response.ReActorModelsResponse
import ciyin.sdwebui.response.ReActorUpscalersResponse

interface ReActorService {

    suspend fun getModels(): Result<ReActorModelsResponse>

    suspend fun getUpscalers(): Result<ReActorUpscalersResponse>
}
