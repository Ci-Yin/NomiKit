package ciyin.serialization

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.longOrNull

val JsonElement.jsonObjectOrNull get() = (this as? JsonObject)

fun JsonObject.getJsonObjectOrNull(key: String): JsonObject? {
    return get(key)?.jsonObjectOrNull
}

fun JsonObject.getJsonObject(key: String): JsonObject {
    return getJsonObjectOrNull(key) ?: throw NoSuchElementException("key $key not found")
}

fun JsonObject.getJsonArrayOrNull(key: String): JsonArray? {
    return get(key)?.jsonArrayOrNull
}

fun JsonObject.getJsonArray(key: String): JsonArray {
    return getJsonArrayOrNull(key) ?: throw NoSuchElementException("key $key not found")
}

fun JsonObject.getInt(key: String): Int {
    val field = get(key)?.jsonPrimitiveOrNull ?: throw NoSuchElementException("key $key not found")
    return field.intOrNull ?: throw IllegalStateException("Field $key is not an int: $field")
}

fun JsonObject.getIntOrNull(key: String): Int? {
    val field = get(key)?.jsonPrimitiveOrNull
    return field?.intOrNull
}

fun JsonObject.getLong(key: String): Long {
    val field = get(key)?.jsonPrimitiveOrNull ?: throw NoSuchElementException("key $key not found")
    return field.longOrNull ?: throw IllegalStateException("Field $key is not a long: $field")
}

fun JsonObject.getLongOrNull(key: String): Long? {
    val field = get(key)?.jsonPrimitiveOrNull
    return field?.longOrNull
}

fun JsonObject.getStringOrNull(key: String): String? {
    return get(key)?.jsonPrimitiveOrNull?.content
}

fun JsonObject.getString(key: String): String {
    return getStringOrNull(key) ?: throw NoSuchElementException("key $key not found")
}

fun JsonObject.getBoolean(key: String): Boolean {
    val field = get(key)?.jsonPrimitiveOrNull ?: throw NoSuchElementException("key $key not found")
    return field.booleanOrNull ?: throw IllegalStateException("Field $key is not a boolean: $field")
}

fun JsonObject.getBooleanOrNull(key: String): Boolean? {
    val field = get(key)?.jsonPrimitiveOrNull
    return field?.booleanOrNull
}

fun JsonObject.getDouble(key: String): Double {
    val field = get(key)?.jsonPrimitiveOrNull ?: throw NoSuchElementException("key $key not found")
    return field.doubleOrNull ?: throw IllegalStateException("Field $key is not a double: $field")
}

fun JsonObject.getDoubleOrNull(key: String): Double? {
    val field = get(key)?.jsonPrimitiveOrNull
    return field?.doubleOrNull
}

fun JsonObject.getFloat(key: String): Float {
    val field = get(key)?.jsonPrimitiveOrNull ?: throw NoSuchElementException("key $key not found")
    return field.floatOrNull ?: throw IllegalStateException("Field $key is not a float: $field")
}

fun JsonObject.getFloatOrNull(key: String): Float? {
    val field = get(key)?.jsonPrimitiveOrNull
    return field?.floatOrNull
}

// 添加默认值支持
fun JsonObject.getIntOrDefault(key: String, default: Int): Int {
    return getIntOrNull(key) ?: default
}

fun JsonObject.getLongOrDefault(key: String, default: Long): Long {
    return getLongOrNull(key) ?: default
}

fun JsonObject.getStringOrDefault(key: String, default: String): String {
    return getStringOrNull(key) ?: default
}

fun JsonObject.getBooleanOrDefault(key: String, default: Boolean): Boolean {
    return getBooleanOrNull(key) ?: default
}

fun JsonObject.getDoubleOrDefault(key: String, default: Double): Double {
    return getDoubleOrNull(key) ?: default
}

fun JsonObject.getFloatOrDefault(key: String, default: Float): Float {
    return getFloatOrNull(key) ?: default
}

// 添加 JsonArray 的扩展方法
fun JsonArray.getJsonObjectOrNull(index: Int): JsonObject? {
    return getOrNull(index)?.jsonObjectOrNull
}

fun JsonArray.getJsonObject(index: Int): JsonObject {
    return getJsonObjectOrNull(index)
        ?: throw NoSuchElementException("index $index not found or not an object")
}

fun JsonArray.getJsonArrayOrNull(index: Int): JsonArray? {
    return getOrNull(index)?.jsonArray
}

fun JsonArray.getJsonArray(index: Int): JsonArray {
    return getJsonArrayOrNull(index)
        ?: throw NoSuchElementException("index $index not found or not an array")
}

fun JsonArray.getIntOrNull(index: Int): Int? {
    return getOrNull(index)?.jsonPrimitiveOrNull?.intOrNull
}

fun JsonArray.getInt(index: Int): Int {
    return getIntOrNull(index)
        ?: throw NoSuchElementException("index $index not found or not an int")
}

fun JsonArray.getStringOrNull(index: Int): String? {
    return getOrNull(index)?.jsonPrimitiveOrNull?.content
}

fun JsonArray.getString(index: Int): String {
    return getStringOrNull(index)
        ?: throw NoSuchElementException("index $index not found or not a string")
}

fun JsonArray.getBooleanOrNull(index: Int): Boolean? {
    return getOrNull(index)?.jsonPrimitiveOrNull?.booleanOrNull
}

fun JsonArray.getBoolean(index: Int): Boolean {
    return getBooleanOrNull(index)
        ?: throw NoSuchElementException("index $index not found or not a boolean")
}

fun JsonArray.getDoubleOrNull(index: Int): Double? {
    return getOrNull(index)?.jsonPrimitiveOrNull?.doubleOrNull
}

fun JsonArray.getDouble(index: Int): Double {
    return getDoubleOrNull(index)
        ?: throw NoSuchElementException("index $index not found or not a double")
}

// 添加类型检查方法
val JsonElement.isJsonObject: Boolean get() = this is JsonObject
val JsonElement.isJsonArray: Boolean get() = this is JsonArray
val JsonElement.isJsonPrimitive: Boolean get() = this is JsonPrimitive
val JsonElement.isJsonNull: Boolean get() = this is JsonNull

// 添加安全类型转换
val JsonElement.jsonArrayOrNull: JsonArray? get() = this as? JsonArray
val JsonElement.jsonPrimitiveOrNull: JsonPrimitive? get() = this as? JsonPrimitive

// 添加深度搜索方法
fun JsonObject.findByPath(path: String): JsonElement? {
    val keys = path.split('.')
    var current: JsonElement? = this

    for (key in keys) {
        current = when (current) {
            is JsonObject -> current[key]
            else -> null
        }
        if (current == null) break
    }

    return current
}

// 添加合并方法
fun JsonObject.merge(other: JsonObject): JsonObject {
    val merged = this.toMutableMap()
    other.forEach { (key, value) ->
        merged[key] = when {
            merged[key] is JsonObject && value is JsonObject ->
                (merged[key] as JsonObject).merge(value)

            else -> value
        }
    }
    return JsonObject(merged)
}

// 添加判空的便捷方法
fun JsonObject.hasNonNullKey(key: String): Boolean = containsKey(key) && get(key) !is JsonNull

// 添加 JSON 路径支持
fun JsonObject.getByPath(path: String): JsonElement? = findByPath(path)
fun JsonObject.getStringByPath(path: String): String? =
    findByPath(path)?.jsonPrimitiveOrNull?.content

fun JsonObject.getIntByPath(path: String): Int? = findByPath(path)?.jsonPrimitiveOrNull?.intOrNull
fun JsonObject.getBooleanByPath(path: String): Boolean? =
    findByPath(path)?.jsonPrimitiveOrNull?.booleanOrNull