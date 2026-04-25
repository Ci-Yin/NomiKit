package ciyin.sdwebui.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * ReActor 扩展报告的可用 ONNX 模型文件名列表。
 */
@Serializable
data class ReActorModelsResponse(
    @SerialName("models") val models: List<String>,
)
