package ciyin.sdwebui.service

import ciyin.sdwebui.response.ControlNetControlTypesResponse
import ciyin.sdwebui.response.ControlNetModelsResponse
import ciyin.sdwebui.response.ControlNetModulesResponse
import ciyin.sdwebui.response.ControlNetVersionResponse

interface ControlNetService {

    suspend fun getVersion(): Result<ControlNetVersionResponse>

    suspend fun getModels(): Result<ControlNetModelsResponse>

    suspend fun getModules(): Result<ControlNetModulesResponse>

    suspend fun getControlTypes(): Result<ControlNetControlTypesResponse>

    suspend fun getSettings(): Result<String>
}
