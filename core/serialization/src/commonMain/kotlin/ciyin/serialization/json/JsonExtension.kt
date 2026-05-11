package ciyin.serialization.json

import ciyin.io.File
import ciyin.io.readText
import ciyin.io.writeText
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonBuilder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement


/**
 *
 * kotlin文件作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2024/10/20 下午6:06
 */


/**
 * 将 JSON 字符串反序列化为指定对象。
 *
 * 可通过 [builderAction] 调整本次解析使用的 JSON 配置。
 *
 * @param T 目标对象的数据类型。
 * @param builderAction JSON 配置构建回调。
 * @return 解析后的对象。
 */
inline fun <reified T : Any> String.fromJson(
    noinline builderAction: JsonBuilder.() -> Unit = {}
): T {
    val json = Json(builderAction = builderAction)
    return json.decodeFromString(this)
}

/**
 * 将当前对象序列化为 JSON 字符串。
 *
 * @param T 当前对象的数据类型。
 * @param builderAction JSON 配置构建回调。
 * @return 序列化后的 JSON 字符串。
 */
inline fun <reified T : Any> T.toJsonStr(
    noinline builderAction: JsonBuilder.() -> Unit = {}
): String {
    val json = Json(builderAction = builderAction)
    return json.encodeToString(this)
}

/**
 * 将对象序列化为 JSON 并写入当前文件。
 *
 * @param T 写入对象的数据类型。
 * @param src 要写入的对象。
 * @param builderAction JSON 配置构建回调。
 */
inline fun <reified T : Any> File.writeJson(
    src: T,
    noinline builderAction: JsonBuilder.() -> Unit = {}
) {
    return writeText(src.toJsonStr(builderAction = builderAction))
}

/**
 * 读取当前文件内容并反序列化为指定对象。
 *
 * @param T 目标对象的数据类型。
 * @param builderAction JSON 配置构建回调。
 * @return 解析后的对象。
 */
inline fun <reified T : Any> File.readJson(
    noinline builderAction: JsonBuilder.() -> Unit = {}
): T {
    return readText().fromJson(builderAction = builderAction)
}


/**
 * 使用指定序列化器将对象序列化为 JSON 并写入当前文件。
 *
 * @param serializer 对象序列化器。
 * @param src 要写入的对象。
 * @param builderAction JSON 配置构建回调。
 */
@Suppress("JSON_FORMAT_REDUNDANT")
fun <T : Any> File.writeJson(
    serializer: SerializationStrategy<T>,
    src: T,
    builderAction: JsonBuilder.() -> Unit = {}
) {
    val json = Json(builderAction = builderAction).encodeToString(serializer, src)
    return writeText(json)
}

/**
 * 使用指定反序列化器读取当前文件内容并反序列化为对象。
 *
 * @param deserializer 对象反序列化器。
 * @param builderAction JSON 配置构建回调。
 * @return 解析后的对象。
 */
fun <T : Any> File.readJson(
    deserializer: DeserializationStrategy<T>,
    builderAction: JsonBuilder.() -> Unit = {}
): T {
    return Json(builderAction = builderAction).decodeFromString(deserializer, readText())
}

/**
 * 修改对象的指定字段并返回新的实例
 *
 * 此函数将对象序列化为 JSON，应用指定的字段更新，然后反序列化回原类型。
 * 适用于需要部分更新不可变对象（如 data class）的场景。
 *
 * 注意：不存在的字段会被忽略，不会抛出异常。
 *
 * @param T 必须是可序列化的类型（标注 @Serializable）
 * @param updates 要更新的字段映射，key 为字段名，value 为新的 JsonElement 值
 * @return 应用更新后的新对象实例
 * @throws SerializationException 如果类型 T 不可序列化
 * @throws IllegalArgumentException 如果类型 T 序列化后不是 JsonObject（例如基本类型或集合）
 *
 * @sample
 * ```kotlin
 * @Serializable
 * data class User(val name: String, val age: Int, val email: String?)
 *
 * val user = User("张三", 25, "test@example.com")
 * val updated = user.modifyJson(
 *     mapOf(
 *         "age" to JsonPrimitive(26),
 *         "email" to JsonNull
 *     )
 * )
 * // updated: User(name=张三, age=26, email=null)
 * ```
 */
@OptIn(InternalSerializationApi::class)
inline fun <reified T : Any> T.modifyJson(
    updates: Map<String, JsonElement>,
    json: Json = Json { ignoreUnknownKeys = true }
): T {

    // 将当前对象序列化为 JsonElement
    val element = json.encodeToJsonElement(this)

    // 确保序列化结果是 JsonObject
    if (element !is JsonObject) {
        throw IllegalArgumentException(
            "类型 ${T::class.simpleName} 序列化后不是 JsonObject，" +
                    "实际类型为 ${element::class.simpleName}。" +
                    "此函数仅支持可序列化为对象的类型（如 data class）。"
        )
    }

    // 合并原有字段和更新字段
    val updated = JsonObject(element + updates)

    // 反序列化为目标类型
    return json.decodeFromJsonElement(updated)
}


/**
 * 修改对象的指定字段并返回新的实例
 *
 * 此函数将对象序列化为 JsonElement，应用指定的字段更新，然后反序列化回原类型。
 * 适用于需要部分更新不可变对象（如 data class）的场景。
 *
 * 注意：不存在的字段会被忽略，不会抛出异常。
 *
 * @param serializer 需序列化的对象
 * @param updates 要更新的字段映射，key 为字段名，value 为新的 JsonElement 值
 * @param json 配置文件
 * @return 应用更新后的新对象实例
 * @throws SerializationException 如果类型 T 不可序列化
 * @throws IllegalArgumentException 如果类型 T 序列化后不是 JsonObject（例如基本类型或集合）
 *
 */
@OptIn(ExperimentalSerializationApi::class)
fun <T> T.modifyJson(
    serializer: KSerializer<T>,
    updates: Map<String, JsonElement>,
    json: Json = Json { ignoreUnknownKeys = true }
): T {

    // 将当前对象序列化为 JsonElement
    val element = json.encodeToJsonElement(serializer, this)

    // 确保序列化结果是 JsonObject
    if (element !is JsonObject) {
        throw IllegalArgumentException(
            "类型 ${serializer.descriptor.serialName} 序列化后不是 JsonObject，" +
                    "实际类型为 ${element::class.simpleName}。" +
                    "此函数仅支持可序列化为对象的类型（如 data class）。"
        )
    }

    // 合并原有字段和更新字段
    val updated = JsonObject(element + updates)

    // 反序列化为目标类型
    return json.decodeFromJsonElement(serializer, updated)
}
