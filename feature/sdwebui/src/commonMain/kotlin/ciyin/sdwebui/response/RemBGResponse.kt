package ciyin.sdwebui.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * RemBG 扩展返回的抠图结果（Base64）。
 */
@Serializable
data class RemBGResponse(
    @SerialName("image") val image: String,
)