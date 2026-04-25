package ciyin.sdwebui.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * ControlNet 可用模型名称列表。
 */
@Serializable
data class ControlNetModelsResponse(
    @SerialName("model_list") val models: List<String>,
)
