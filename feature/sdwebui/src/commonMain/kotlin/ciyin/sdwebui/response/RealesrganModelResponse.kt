package ciyin.sdwebui.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * `sdapi/v1/realesrgan-models` 返回的 Real-ESRGAN 模型条目。
 */
@Serializable
data class RealesrganModelResponse(
    @SerialName("name") val name: String,
    @SerialName("path") val path: String,
    @SerialName("scale") val scale: Int,
)
