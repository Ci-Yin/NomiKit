package ciyin.serialization.json

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.reflect.KClass

@OptIn(ExperimentalSerializationApi::class)
class KotlinxJsonCodec(override val builder: JsonBuilder) : JsonCodec {

    @PublishedApi
    internal val jsonParser = Json {
        builder.map(this)
    }

    @OptIn(InternalSerializationApi::class)
    override fun <T : Any> fromJson(json: String, type: KClass<T>): T {
        error("Please use inline fun <reified T : Any> fromJson(json: String)")
    }

    @OptIn(InternalSerializationApi::class)
    override fun <T : Any> toJson(value: T): String {
        error("Please use inline fun <reified T : Any> toJsonString(value: T)")
    }

    /**
     * 解析 JSON 字符串为指定类型的对象。
     *
     * @param T 目标对象的类型。
     * @param json 要解析的 JSON 字符串。
     * @return 解析后的 [T] 类型对象。
     */
    inline fun <reified T : Any> fromJson(json: String): T {
        return jsonParser.decodeFromString(json)
    }

    /**
     * 转换对象为 JSON 字符串。
     *
     * @param value 要转换的对象。
     * @return 生成的 JSON 字符串。
     */
    @OptIn(InternalSerializationApi::class)
    inline fun <reified T : Any> toJsonString(value: T): String {
        return jsonParser.encodeToString(value)
    }

}