package ciyin.parser.site.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive

/**
 * 兼容字段（既可能是字符串也可能是数字）。
 */
internal object NumberAsStringSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("NumberAsString", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): String {
        val jsonDecoder = decoder as? JsonDecoder
            ?: return decoder.decodeString()
        val element = jsonDecoder.decodeJsonElement()
        if (element is JsonPrimitive) {
            return element.content   // 数字或字符串都转成字符串
        }
        throw SerializationException(
            "Expected primitive for Number String, but was: $element"
        )
    }

    override fun serialize(encoder: Encoder, value: String) {
        encoder.encodeString(value)
    }
}