package ciyin.sdwebui.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * `sdapi/v1/sd-vae` 中的单个 VAE 条目。
 */
@Serializable
data class VaeResponse(
    @SerialName("model_name") val modelName: String,
    @SerialName("filename") val filename: String,
)
