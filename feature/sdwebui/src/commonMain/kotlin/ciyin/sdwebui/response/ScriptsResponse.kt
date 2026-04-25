package ciyin.sdwebui.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * `sdapi/v1/scripts` 返回的 txt2img / img2img 可用脚本名称列表。
 */
@Serializable
data class ScriptsResponse(
    @SerialName("txt2img") val txt2img: List<String>,
    @SerialName("img2img") val img2img: List<String>,
)
