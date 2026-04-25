package ciyin.sdwebui.service

import ciyin.sdwebui.response.ReActorModelsResponse
import ciyin.sdwebui.response.ReActorUpscalersResponse

/**
 * ReActor 扩展提供的模型列表与放大器列表查询。
 */
interface ReActorService {

    /** 查询 ReActor 可用模型：`reactor/models`。 */
    suspend fun getModels(): Result<ReActorModelsResponse>

    /** 查询 ReActor 放大器名称：`reactor/upscalers`。 */
    suspend fun getUpscalers(): Result<ReActorUpscalersResponse>
}
