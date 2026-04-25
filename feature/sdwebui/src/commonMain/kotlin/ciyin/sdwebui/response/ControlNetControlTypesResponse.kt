package ciyin.sdwebui.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * `control_types` 返回的各控制类型及其默认模型/预处理器。
 */
@Serializable
data class ControlNetControlTypesResponse(
    @SerialName("control_types") val controlTypes: Map<String, ControlType>,
) {

    /**
     * 某一控制类型下可选模块与模型列表及默认值。
     */
    @Serializable
    data class ControlType(
        @SerialName("module_list") val modules: List<String>,
        @SerialName("model_list") val models: List<String>,
        @SerialName("default_option") val defaultOption: String,
        @SerialName("default_model") val defaultModel: String
    )
}
