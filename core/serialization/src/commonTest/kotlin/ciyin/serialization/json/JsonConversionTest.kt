package ciyin.serialization.json

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * [JsonConversion.kt] 中 JSON 转换扩展的单元测试。
 */
class JsonConversionTest {

    @Serializable
    private data class SerializableUser(
        val name: String,
        val age: Int
    )

    /**
     * 标记一个不可序列化类型，用于验证错误路径。
     */
    private class UnsupportedValue

    @Test
    fun `toJsonElement converts null and primitive values`() {
        val nullValue: String? = null

        assertEquals(JsonNull, nullValue.toJsonElement())
        assertEquals(JsonPrimitive("hello"), "hello".toJsonElement())
        assertEquals(JsonPrimitive(42), 42.toJsonElement())
        assertEquals(JsonPrimitive(3.14), 3.14.toJsonElement())
        assertEquals(JsonPrimitive(true), true.toJsonElement())
        assertEquals(JsonPrimitive("A"), 'A'.toJsonElement())
    }

    @Test
    fun `toJsonElement returns JsonElement instances directly`() {
        val primitive = JsonPrimitive("value")
        val array = jsonArrayOf(1, 2)
        val obj = jsonObjectOf("name" to "Alice")

        assertSame(primitive, primitive.toJsonElement())
        assertSame(array, array.toJsonElement())
        assertSame(obj, obj.toJsonElement())
    }

    @Test
    fun `toJsonElement converts collections arrays and primitive arrays`() {
        val iterableElement = listOf(1, "two", true, null).toJsonElement()
        val arrayElement = arrayOf("a", "b").toJsonElement()
        val intArrayElement = intArrayOf(1, 2, 3).toJsonElement()
        val charArrayElement = charArrayOf('x', 'y').toJsonElement()

        assertEquals(
            jsonArrayOf(1, "two", true, null),
            iterableElement
        )
        assertEquals(jsonArrayOf("a", "b"), arrayElement)
        assertEquals(jsonArrayOf(1, 2, 3), intArrayElement)
        assertEquals(jsonArrayOf("x", "y"), charArrayElement)
    }

    @Test
    fun `toJsonElement converts maps and serializable objects`() {
        val mapElement = mapOf(
            "name" to "Alice",
            "profile" to mapOf("age" to 18),
            "tags" to listOf("new", "vip")
        ).toJsonElement()
        val userElement = SerializableUser("Bob", 20).toJsonElement()

        assertEquals(
            jsonObjectOf(
                "name" to "Alice",
                "profile" to jsonObjectOf("age" to 18),
                "tags" to jsonArrayOf("new", "vip")
            ),
            mapElement
        )
        assertTrue(userElement is JsonObject)
        assertEquals(JsonPrimitive("Bob"), userElement["name"])
        assertEquals(JsonPrimitive(20), userElement["age"])
    }

    @Test
    fun `toJsonPrimitive converts supported primitive values`() {
        assertEquals(JsonPrimitive("hello"), "hello".toJsonPrimitive())
        assertEquals(JsonPrimitive(42), 42.toJsonPrimitive())
        assertEquals(JsonPrimitive(42L), 42L.toJsonPrimitive())
        assertEquals(JsonPrimitive(1.5f), 1.5f.toJsonPrimitive())
        assertEquals(JsonPrimitive(2.5), 2.5.toJsonPrimitive())
        assertEquals(JsonPrimitive(false), false.toJsonPrimitive())
        assertEquals(JsonPrimitive("Z"), 'Z'.toJsonPrimitive())
    }

    @Test
    fun `toJsonPrimitive rejects unsupported values`() {
        val error = assertFailsWith<IllegalArgumentException> {
            listOf(1, 2).toJsonPrimitive()
        }

        assertTrue(error.message.orEmpty().contains("JsonPrimitive"))
    }

    @Test
    fun `toJsonObject ignores null keys and converts nested values`() {
        val element = mapOf<String?, Any?>(
            "name" to "Alice",
            null to "ignored",
            "scores" to intArrayOf(1, 2)
        ).toJsonObject()

        assertEquals(2, element.size)
        assertEquals(JsonPrimitive("Alice"), element["name"])
        assertEquals(jsonArrayOf(1, 2), element["scores"])
    }

    @Test
    fun `toJsonArray converts iterable and object arrays`() {
        val iterableArray = listOf<Any?>("a", 1, null).toJsonArray()
        val objectArray = arrayOf<Any?>(true, "b").toJsonArray()

        assertEquals(jsonArrayOf("a", 1, null), iterableArray)
        assertEquals(jsonArrayOf(true, "b"), objectArray)
    }

    @Test
    fun `jsonObjectOf and jsonArrayOf create nested elements`() {
        val obj = jsonObjectOf(
            "name" to "Alice",
            "children" to jsonArrayOf(
                jsonObjectOf("id" to 1),
                jsonObjectOf("id" to 2)
            ),
            "empty" to null
        )

        assertEquals(JsonPrimitive("Alice"), obj["name"])
        assertEquals(JsonNull, obj["empty"])
        assertEquals(2, (obj["children"] as JsonArray).size)
    }

    @Test
    fun `toJsonElement propagates serialization errors for unsupported objects`() {
        assertFailsWith<SerializationException> {
            UnsupportedValue().toJsonElement()
        }
    }
}
