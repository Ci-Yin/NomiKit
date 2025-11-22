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
 * 把JSON映射成对象
 * 可用注解：[Serializable]
 *
 * @param T      对象的数据类型
</T> */
inline fun <reified T> String.fromJson(): T {
    return JsonProvider.fromJson(this)
}

/**
 * 把任意对象映射成JSON
 * 可用注解：[Serializable]
 *
 * @param isFormat 是否格式化
 */
inline fun <reified T : Any> T.toJsonStr(isFormat: Boolean = false): String {
    return JsonProvider {
        prettyPrint = isFormat
    }.toJson(this)
}

/**
 * 写入文件JSON
 *
 * @param src      对象
 * @param isFormat 是否格式化
 * @return 是否写入成功
 */
inline fun <reified T : Any> File.writeJson(src: T, isFormat: Boolean = false) {
    return writeText(src.toJsonStr(isFormat))
}

/**
 * 读取文件内容转化成对象
 * 不存在时创建新对象
 *
 * @param T
 * @return Bean对象
 */
inline fun <reified T : Any> File.readJson(): T {
    return readText().fromJson()
}


/**
 * 写入文件JSON
 *
 * @param src      对象
 * @param isFormat 是否格式化
 * @return 是否写入成功
 */
@Suppress("JSON_FORMAT_REDUNDANT")
fun <T : Any> File.writeJson(
    serializer: SerializationStrategy<T>,
    src: T,
    isFormat: Boolean = false
) {
    val json = Json { prettyPrint = isFormat }.encodeToString(serializer, src)
    return writeText(json)
}

/**
 * 读取文件内容转化成对象
 * 不存在时创建新对象
 *
 * @param T
 * @return Bean对象
 */
fun <T : Any> File.readJson(deserializer: DeserializationStrategy<T>): T {
    return Json.decodeFromString(deserializer, readText())
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

