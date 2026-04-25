package ciyin.sdwebui.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * `sdapi/v1/face-restorers` 中的面部修复器名称与路径。
 */
@Serializable
data class FaceRestorerResponse(
    @SerialName("name") val name: String,
    @SerialName("cmd_dir") val cmdDir: String?,
)
