package ciyin.sdwebui.response

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder

/**
 * `sdapi/v1/loras` 返回的单个 LoRA 条目。
 *
 * @property name LoRA 名称。
 * @property alias LoRA 别名。
 * @property path LoRA 文件路径。
 * @property metadata LoRA 元数据对象的紧凑 JSON 字符串。
 */
@Serializable
data class LoraResponse(
    @SerialName("name") val name: String,
    @SerialName("alias") val alias: String,
    @SerialName("path") val path: String,
    @Serializable(with = LoraMetadataStringSerializer::class)
    @SerialName("metadata") val metadata: String,
)

/**
 * 将 LoRA metadata 对象保存为紧凑 JSON 字符串，避免为厂商元数据继续拆分模型。
 */
private object LoraMetadataStringSerializer : KSerializer<String> {

    /** 以字符串类型暴露给 kotlinx.serialization 的序列化描述。 */
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LoraMetadata", PrimitiveKind.STRING)

    /** 将 JSON metadata 对象读取为紧凑 JSON 字符串。 */
    override fun deserialize(decoder: Decoder): String {
        val input = decoder as JsonDecoder
        return input.decodeJsonElement().toString()
    }

    /** 将已保存的 metadata 字符串按普通字符串写出。 */
    override fun serialize(encoder: Encoder, value: String) {
        encoder.encodeString(value)
    }
}
