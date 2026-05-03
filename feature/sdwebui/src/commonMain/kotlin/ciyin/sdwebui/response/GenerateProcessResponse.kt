package ciyin.sdwebui.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

/**
 * 文生图 / 图生图成功后的 Base64 图像列表与附加 `info` JSON 字符串。
 */
@Serializable
data class GenerateProcessResponse(
    @SerialName("info") val info: String,
    @SerialName("images") val images: List<String>,
)
