package ciyin.serialization.json


/**
 *
 * kotlin文件作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2025/11/2 20:16
 */


import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement

/**
 * 将 Any 类型转换为 JsonElement
 *
 * 支持基本类型、集合、Map 和可序列化对象的转换。
 *
 * @receiver 要转换的对象
 * @return 对应的 JsonElement
 * @throws IllegalArgumentException 如果类型不支持转换
 *
 * @sample
 * ```kotlin
 * val str = "hello".toJsonElement()        // JsonPrimitive("hello")
 * val num = 42.toJsonElement()             // JsonPrimitive(42)
 * val list = listOf(1, 2, 3).toJsonElement() // JsonArray([1, 2, 3])
 * val map = mapOf("key" to "value").toJsonElement() // JsonObject({"key": "value"})
 * ```
 */
inline fun <reified T : Any> T?.toJsonElement(): JsonElement = when (this) {
    null -> JsonNull
    is JsonElement -> this
    is String -> JsonPrimitive(this)
    is Number -> JsonPrimitive(this)
    is Boolean -> JsonPrimitive(this)
    is Char -> JsonPrimitive(this.toString())
    is Map<*, *> -> toJsonObject()
    is Iterable<*> -> toJsonArray()
    is Array<*> -> toList().toJsonArray()
    is ByteArray -> toList().toJsonArray()
    is ShortArray -> toList().toJsonArray()
    is IntArray -> toList().toJsonArray()
    is LongArray -> toList().toJsonArray()
    is FloatArray -> toList().toJsonArray()
    is DoubleArray -> toList().toJsonArray()
    is BooleanArray -> toList().toJsonArray()
    is CharArray -> toList().toJsonArray()
    else -> Json.encodeToJsonElement(this)
}

/**
 * 将 Any 类型转换为 JsonPrimitive
 *
 * 仅支持可以直接转换为 JsonPrimitive 的基本类型。
 *
 * @receiver 要转换的对象，必须是基本类型
 * @return 对应的 JsonPrimitive
 * @throws IllegalArgumentException 如果类型不支持转换为 JsonPrimitive
 *
 * @sample
 * ```kotlin
 * val str = "hello".toJsonPrimitive()   // JsonPrimitive("hello")
 * val num = 42.toJsonPrimitive()        // JsonPrimitive(42)
 * val bool = true.toJsonPrimitive()     // JsonPrimitive(true)
 * ```
 */
fun Any.toJsonPrimitive(): JsonPrimitive = when (this) {
    is String -> JsonPrimitive(this)
    is Number -> JsonPrimitive(this)
    is Boolean -> JsonPrimitive(this)
    is Char -> JsonPrimitive(this.toString())
    else -> throw IllegalArgumentException(
        "无法将类型 ${this::class.simpleName} 转换为 JsonPrimitive。" +
                "仅支持：String, Number, Boolean, Char"
    )
}

/**
 * 将 Map 转换为 JsonObject
 */
fun Map<*, *>.toJsonObject(): JsonObject = buildJsonObject {
    forEach { (key, value) ->
        val keyStr = key?.toString() ?: return@forEach
        put(keyStr, value.toJsonElement())
    }
}

/**
 * 将 Iterable 转换为 JsonArray
 */
fun Iterable<*>.toJsonArray(): JsonArray = buildJsonArray {
    forEach { item ->
        add(item.toJsonElement())
    }
}

/**
 * 将 Array 转换为 JsonArray
 */
fun Array<*>.toJsonArray(): JsonArray = toList().toJsonArray()

/**
 * 便捷的 Pair 扩展，用于创建 Map
 */
fun jsonObjectOf(vararg pairs: Pair<String, Any?>): JsonObject = buildJsonObject {
    pairs.forEach { (key, value) ->
        put(key, value.toJsonElement())
    }
}

/**
 * 便捷的创建 JsonArray
 */
fun jsonArrayOf(vararg elements: Any?): JsonArray = buildJsonArray {
    elements.forEach { element ->
        add(element.toJsonElement())
    }
}