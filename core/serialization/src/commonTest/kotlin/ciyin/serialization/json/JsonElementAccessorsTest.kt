package ciyin.serialization.json

import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * [JsonElementAccessors.kt] 中 JSON 元素访问扩展的单元测试。
 */
class JsonElementAccessorsTest {

    private val sample = jsonObjectOf(
        "id" to 1,
        "longId" to 2L,
        "name" to "Alice",
        "active" to true,
        "score" to 9.5,
        "ratio" to 1.5f,
        "profile" to jsonObjectOf(
            "city" to "Shanghai",
            "level" to 3
        ),
        "items" to jsonArrayOf(
            jsonObjectOf("id" to 10),
            "second",
            false,
            7,
            2.5
        ),
        "nullable" to null
    )

    @Test
    fun `json element type casts and flags work`() {
        val obj = sample["profile"]!!
        val array = sample["items"]!!
        val primitive = sample["name"]!!
        val nullElement = sample["nullable"]!!

        assertTrue(obj.isJsonObject)
        assertTrue(array.isJsonArray)
        assertTrue(primitive.isJsonPrimitive)
        assertTrue(nullElement.isJsonNull)
        assertEquals(sample.getJsonObject("profile"), obj.jsonObjectOrNull)
        assertEquals(sample.getJsonArray("items"), array.jsonArrayOrNull)
        assertEquals(JsonPrimitive("Alice"), primitive.jsonPrimitiveOrNull)
    }

    @Test
    fun `json object accessors return typed values`() {
        assertEquals(1, sample.getInt("id"))
        assertEquals(2L, sample.getLong("longId"))
        assertEquals("Alice", sample.getString("name"))
        assertTrue(sample.getBoolean("active"))
        assertEquals(9.5, sample.getDouble("score"))
        assertEquals(1.5f, sample.getFloat("ratio"))
        assertEquals("Shanghai", sample.getJsonObject("profile").getString("city"))
        assertEquals("second", sample.getJsonArray("items").getString(1))
    }

    @Test
    fun `json object nullable and default accessors handle missing and invalid values`() {
        assertNull(sample.getIntOrNull("missing"))
        assertEquals(99, sample.getIntOrDefault("missing", 99))
        assertEquals(99L, sample.getLongOrDefault("missing", 99L))
        assertEquals("fallback", sample.getStringOrDefault("missing", "fallback"))
        assertEquals(false, sample.getBooleanOrDefault("missing", false))
        assertEquals(1.25, sample.getDoubleOrDefault("missing", 1.25))
        assertEquals(1.25f, sample.getFloatOrDefault("missing", 1.25f))
    }

    @Test
    fun `json object strict accessors throw when missing or wrong type`() {
        assertFailsWith<NoSuchElementException> {
            sample.getString("missing")
        }
        assertFailsWith<IllegalStateException> {
            sample.getInt("name")
        }
        assertFailsWith<IllegalStateException> {
            sample.getBoolean("name")
        }
    }

    @Test
    fun `json array accessors return typed values and throw for invalid access`() {
        val array = sample.getJsonArray("items")

        assertEquals(10, array.getJsonObject(0).getInt("id"))
        assertEquals("second", array.getString(1))
        assertFalse(array.getBoolean(2))
        assertEquals(7, array.getInt(3))
        assertEquals(2.5, array.getDouble(4))
        assertNull(array.getStringOrNull(99))
        assertFailsWith<NoSuchElementException> {
            array.getJsonObject(1)
        }
    }

    @Test
    fun `path helpers find nested primitive values`() {
        assertEquals(sample.getJsonObject("profile"), sample.getByPath("profile"))
        assertEquals("Shanghai", sample.getStringByPath("profile.city"))
        assertEquals(3, sample.getIntByPath("profile.level"))
        assertEquals(true, sample.getBooleanByPath("active"))
        assertNull(sample.getByPath("profile.missing"))
    }

    @Test
    fun `merge recursively combines object values`() {
        val left = jsonObjectOf(
            "name" to "Alice",
            "profile" to jsonObjectOf("city" to "Shanghai", "age" to 18)
        )
        val right = jsonObjectOf(
            "profile" to jsonObjectOf("age" to 20, "level" to 3),
            "active" to true
        )

        val merged = left.merge(right)

        assertEquals("Alice", merged.getString("name"))
        assertEquals("Shanghai", merged.getJsonObject("profile").getString("city"))
        assertEquals(20, merged.getJsonObject("profile").getInt("age"))
        assertEquals(3, merged.getJsonObject("profile").getInt("level"))
        assertTrue(merged.getBoolean("active"))
    }

    @Test
    fun `hasNonNullKey distinguishes missing null and present values`() {
        assertTrue(sample.hasNonNullKey("name"))
        assertFalse(sample.hasNonNullKey("missing"))
        assertFalse(sample.hasNonNullKey("nullable"))
        assertEquals(JsonNull, sample["nullable"])
    }
}
