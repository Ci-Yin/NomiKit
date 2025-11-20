package ciyin.serialization.json

import kotlinx.serialization.InternalSerializationApi

/**
 * 获取当前环境默认的 [JsonCodec] 实现。
 *
 * @param builder 用于配置 JSON 编解码器的 [JsonBuilder]。
 * @return 一个 [JsonCodec] 实例。
 */
expect fun JsonCodec(builder: JsonBuilder = JsonBuilder()): JsonCodec

/**
 * JSON 提供器，用于统一入口访问。
 *
 * 封装了 [JsonCodec] 和 [JsonBuilder] 以提供一个配置好的 JSON 服务。
 * 默认的单例实例可通过 `JsonProvider` 直接访问。
 */
sealed class JsonProvider(
    jsonCodec: (JsonBuilder) -> JsonCodec = ::JsonCodec,
    /**
     * 用于构建 JSON 配置的构建器。
     */
    val builder: JsonBuilder = JsonBuilder()
) {

    /**
     * 底层的 JSON 编解码器。
     */
    @PublishedApi
    internal val codec: JsonCodec = jsonCodec(builder)

    /**
     * 根据 [builder] 构建的当前 JSON 配置。
     */
    val configuration: JsonConfiguration = builder.build()

    /**
     * 解析 JSON 字符串为指定类型的对象。
     *
     * @param T 目标对象的类型。
     * @param json 要解析的 JSON 字符串。
     * @return 解析后的 [T] 类型对象。
     */
    inline fun <reified T : Any> fromJson(json: String): T {
        return if (codec is KotlinxJsonCodec) {
            codec.fromJson(json)
        } else {
            codec.fromJson(json, T::class)
        }
    }

    /**
     * 转换对象为 JSON 字符串。
     *
     * @param value 要转换的对象。
     * @return 生成的 JSON 字符串。
     */
    @OptIn(InternalSerializationApi::class)
    inline fun <reified T : Any> toJson(value: T): String {
        return if (codec is KotlinxJsonCodec) {
            codec.toJsonString(value)
        } else {
            codec.toJson(value)
        }
    }

    /**
     * 提供一个默认配置的 [JsonProvider] 单例实例。
     */
    companion object : JsonProvider()
}

/**
 * [JsonProvider] 的私有实现类。
 */
private class JsonProviderImpl(
    jsonCodec: (JsonBuilder) -> JsonCodec,
    builder: JsonBuilder
) : JsonProvider(jsonCodec, builder)

/**
 * 创建一个自定义配置的 [JsonProvider] 实例。
 *
 * @param jsonCodec 要使用的 JSON 编解码器，默认为 [JsonCodec] 的默认实现。
 * @param builderAction 用于配置 [JsonBuilder] 的 lambda 表达式。
 * @return 一个新的 [JsonProvider] 实例。
 */
fun JsonProvider(
    jsonCodec: (JsonBuilder) -> JsonCodec = ::JsonCodec,
    builderAction: JsonBuilder.() -> Unit = {}
): JsonProvider {
    return JsonProviderImpl(jsonCodec, JsonBuilder().apply(builderAction))
}
