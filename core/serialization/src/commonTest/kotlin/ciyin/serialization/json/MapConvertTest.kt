package ciyin.serialization.json

import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * [MapConvert.kt] 中扁平 Map 与 JSON 元素转换逻辑的单元测试。
 */
class MapConvertTest {

    private enum class SampleEnum {
        FIRST,
        SECOND
    }

    /**
     * 标记一个不支持转换的类型，用于验证错误路径。
     */
    private class UnsupportedValue

    @Test
    fun `convertMapToJsonElement converts flat keys to nested objects`() {
        val json = convertMapToJsonElement(
            mapOf(
                "user.name" to "Alice",
                "user.age" to 18,
                "settings.enabled" to true,
                "tags" to listOf("new", "vip")
            )
        )

        assertEquals("Alice", json.jsonObjectOrNull!!.getStringByPath("user.name"))
        assertEquals(18, json.jsonObjectOrNull!!.getIntByPath("user.age"))
        assertEquals(true, json.jsonObjectOrNull!!.getBooleanByPath("settings.enabled"))
        assertEquals(jsonArrayOf("new", "vip"), json.jsonObjectOrNull!!["tags"])
    }

    @Test
    fun `convertMapToJsonElement rejects path conflicts`() {
        assertFailsWith<IllegalArgumentException> {
            convertMapToJsonElement(
                linkedMapOf(
                    "user" to "Alice",
                    "user.name" to "Bob"
                )
            )
        }
        assertFailsWith<IllegalArgumentException> {
            convertMapToJsonElement(
                linkedMapOf(
                    "user.name" to "Alice",
                    "user" to "Bob"
                )
            )
        }
    }

    @Test
    fun `convertToFinalJsonElement converts supported scalar and collection values`() {
        assertEquals(JsonNull, convertToFinalJsonElement(null))
        assertEquals(JsonPrimitive("text"), convertToFinalJsonElement("text"))
        assertEquals(JsonPrimitive(1), convertToFinalJsonElement(1))
        assertEquals(JsonPrimitive(true), convertToFinalJsonElement(true))
        assertEquals(JsonPrimitive("FIRST"), convertToFinalJsonElement(SampleEnum.FIRST))
        assertEquals(jsonArrayOf(1, "two"), convertToFinalJsonElement(listOf(1, "two")))
        assertEquals(jsonArrayOf("a", "b"), convertToFinalJsonElement(arrayOf("a", "b")))
    }

    @Test
    fun `convertToFinalJsonElement converts nested map values`() {
        val element = convertToFinalJsonElement(
            mapOf(
                "profile" to mapOf(
                    "name" to "Alice",
                    "flags" to listOf(true, false)
                )
            )
        )

        assertEquals("Alice", element.jsonObjectOrNull!!.getStringByPath("profile.name"))
        assertEquals(
            jsonArrayOf(true, false),
            element.jsonObjectOrNull!!.getJsonObject("profile")["flags"]
        )
    }

    @Test
    fun `convertToFinalJsonElement rejects unsupported values`() {
        val error = assertFailsWith<IllegalStateException> {
            convertToFinalJsonElement(UnsupportedValue())
        }

        assertTrue(error.message.orEmpty().contains("Unsupported type"))
    }
}
