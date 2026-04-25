package ciyin.sdwebui.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * `sdapi/v1/upscalers` 中的放大器及其模型信息。
 */
@Serializable
data class UpscalerResponse(
    @SerialName("name") val name: String,
    @SerialName("model_name") val modelName: String?,
    @SerialName("model_path") val modelPath: String?,
    @SerialName("model_url") val modelUrl: String?,
    @SerialName("scale") val scale: Float,
)
