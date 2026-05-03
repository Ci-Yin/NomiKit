package ciyin.ai.image.sdwebui.mapper

import ciyin.ai.core.image.GeneratedImage
import ciyin.ai.core.image.ImageResult
import ciyin.sdwebui.response.ExtraSingleImageResponse
import ciyin.sdwebui.response.GenerateProcessResponse
import ciyin.sdwebui.response.RemBGResponse
import ciyin.serialization.json.toJsonElement
import ciyin.serialization.json.toJsonStr
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * 把 SD WebUI 响应统一映射为 `ai-core` 的 [ImageResult]。
 */
internal fun GenerateProcessResponse.toImageResult(): ImageResult {
    // ⚠️ 关键：info 是字符串，需要 decode
    val json = Json.parseToJsonElement(info).jsonObject

    val seeds = json["all_seeds"]
        ?.jsonArray
        ?.map { it.jsonPrimitive.long }
        ?: emptyList()

    val infotexts = json["infotexts"]
        ?.jsonArray
        ?.map { it.jsonPrimitive.content.trim() }
        ?: emptyList()

    return ImageResult(
        images = images.mapIndexed { index, encoded ->
            GeneratedImage(
                seed = seeds.getOrNull(index) ?: -1,
                infotext = infotexts.getOrNull(index) ?: "",
                bytes = encoded.fromSdWebUiBase64(),
                mimeType = "image/png",
            )
        },
        info = mapOf("info" to info),
    )
}
/**
 * 把 RemBG 单图响应映射为 [GeneratedImage]。
 */
internal fun RemBGResponse.toGeneratedImage(): GeneratedImage = GeneratedImage(
    bytes = image.fromSdWebUiBase64(),
    mimeType = "image/png",
)

/**
 * 把单图超分响应映射为 [GeneratedImage]。
 */
internal fun ExtraSingleImageResponse.toGeneratedImage(): GeneratedImage = GeneratedImage(
    bytes = image.fromSdWebUiBase64(),
    mimeType = "image/png",
)

/**
 * 把原始字节编码成 SD WebUI 可接受的 base64 字符串。
 */
@OptIn(ExperimentalEncodingApi::class)
internal fun ByteArray.toSdWebUiBase64(): String = Base64.encode(this)

/**
 * 把 SD WebUI 返回的 base64 字符串解码为原始字节。
 *
 * 部分后端可能返回带 `data:image/...;base64,` 前缀的数据 URI，这里统一做兼容清洗。
 */
@OptIn(ExperimentalEncodingApi::class)
internal fun String.fromSdWebUiBase64(): ByteArray {
    val payload = substringAfter("base64,", this)
    return Base64.decode(payload)
}
