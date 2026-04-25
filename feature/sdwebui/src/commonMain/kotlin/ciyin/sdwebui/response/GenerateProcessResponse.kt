package ciyin.sdwebui.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 文生图 / 图生图成功后的 Base64 图像列表与附加 `info` JSON 字符串。
 */
@Serializable
data class GenerateProcessResponse(
    @SerialName("images") val images: List<String>,
    @SerialName("info") val info: String,
)
