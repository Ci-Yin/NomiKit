package ciyin.sdwebui.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * `sdapi/v1/sd-models` 中的单个 checkpoint 描述。
 */
@Serializable
data class ModelResponse(
    @SerialName("title") val title: String,
    @SerialName("model_name") val name: String,
    @SerialName("hash") val hash: String,
    @SerialName("sha256") val sha256: String,
    @SerialName("filename") val filename: String,
    @SerialName("config") val config: String?,
)
