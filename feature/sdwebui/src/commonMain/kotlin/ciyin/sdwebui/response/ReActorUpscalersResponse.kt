package ciyin.sdwebui.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * ReActor 可用的放大器名称列表。
 */
@Serializable
data class ReActorUpscalersResponse(
    @SerialName("upscalers") val upscalers: List<String>,
)
