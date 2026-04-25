package ciyin.sdwebui.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * `sdapi/v1/latent-upscale-modes` 中的潜空间放大模式名称。
 */
@Serializable
data class LatentUpscaleModeResponse(
    @SerialName("name") val name: String,
)
