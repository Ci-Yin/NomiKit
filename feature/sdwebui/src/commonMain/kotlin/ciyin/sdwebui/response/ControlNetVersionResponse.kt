package ciyin.sdwebui.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * ControlNet API 版本号。
 */
@Serializable
data class ControlNetVersionResponse(
    @SerialName("version") val version: Int,
)
