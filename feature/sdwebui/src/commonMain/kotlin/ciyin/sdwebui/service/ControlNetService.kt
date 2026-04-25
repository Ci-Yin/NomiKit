package ciyin.sdwebui.service

import ciyin.sdwebui.response.ControlNetControlTypesResponse
import ciyin.sdwebui.response.ControlNetModelsResponse
import ciyin.sdwebui.response.ControlNetModulesResponse
import ciyin.sdwebui.response.ControlNetVersionResponse

/**
 * ControlNet 扩展插件暴露的 REST 能力（版本、模型、预处理器等）。
 */
interface ControlNetService {

    /** 查询 ControlNet API 版本：`controlnet/version`。 */
    suspend fun getVersion(): Result<ControlNetVersionResponse>

    /** 查询可用模型列表：`controlnet/model_list`。 */
    suspend fun getModels(): Result<ControlNetModelsResponse>

    /** 查询预处理器模块：`controlnet/module_list`。 */
    suspend fun getModules(): Result<ControlNetModulesResponse>

    /** 查询控制类型及默认选项：`controlnet/control_types`。 */
    suspend fun getControlTypes(): Result<ControlNetControlTypesResponse>

    /** 查询 ControlNet 设置 JSON：`controlnet/settings`。 */
    suspend fun getSettings(): Result<String>
}
