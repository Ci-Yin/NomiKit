package ciyin.sdwebui.payload.script

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

/**
 * 描述 WebUI `alwayson_scripts` 中某项脚本的 `args` 形态：单对象、多对象或原始 JSON 数组。
 */
@Serializable(with = ScriptSerializer::class)
sealed class ScriptPayload {

    /**
     * 单个 `args` 对象，适用于仅含一组参数的脚本。
     */
    @Serializable
    data class Single(
        @SerialName("args") val args: ScriptArgs,
    ) : ScriptPayload()

    /**
     * `args` 为对象数组，例如多单元 ControlNet。
     */
    @Serializable
    data class Multiple(
        @SerialName("args") val args: List<ScriptArgs>,
    ) : ScriptPayload()

    /**
     * `args` 为 JSON 数组（如 ReActor 的扁平参数列表）。
     */
    @Serializable
    data class Array(
        @SerialName("args") val args: List<JsonPrimitive>,
    ) : ScriptPayload()
}

/**
 * 可嵌入 [ScriptPayload.Single] / [ScriptPayload.Multiple] 的具体脚本参数类型联合。
 */
@Serializable(with = ScriptArgsSerializer::class)
sealed interface ScriptArgs

/**
 * 根据 JSON 结构在 [ScriptPayload] 子类型间多态反序列化。
 */
object ScriptSerializer : JsonContentPolymorphicSerializer<ScriptPayload>(ScriptPayload::class) {

    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<ScriptPayload> {
        return when (element::class) {
            ScriptPayload.Single::class -> ScriptPayload.Single.serializer()
            ScriptPayload.Multiple::class -> ScriptPayload.Multiple.serializer()
            ScriptPayload.Array::class -> ScriptPayload.Array.serializer()
            else -> throw Exception("ERROR: No Serializer found. Serialization failed.")
        }
    }
}

/**
 * 在 [ControlNetScriptArgs] 与 [ADetailerScriptArgs] 之间选择反序列化策略。
 */
object ScriptArgsSerializer : JsonContentPolymorphicSerializer<ScriptArgs>(ScriptArgs::class) {

    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<ScriptArgs> {
        return when (element::class) {
            ControlNetScriptArgs::class -> ControlNetScriptArgs.serializer()
            ADetailerScriptArgs::class -> ADetailerScriptArgs.serializer()
            else -> throw Exception("ERROR: No Serializer found. Serialization failed.")
        }
    }
}
