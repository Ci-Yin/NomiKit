package ciyin.serialization.json

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * [JsonBuilders.kt] 中 JSON 构建辅助扩展的单元测试。
 */
class JsonBuildersTest {

    @Test
    fun `string list converts to json array in order`() {
        val array = listOf("a", "b", "c").toJsonArray()

        assertEquals(3, array.size)
        assertEquals(JsonPrimitive("a"), array[0])
        assertEquals(JsonPrimitive("b"), array[1])
        assertEquals(JsonPrimitive("c"), array[2])
    }

    @Test
    fun `number and boolean lists convert to json arrays in order`() {
        assertEquals(
            listOf(JsonPrimitive(1), JsonPrimitive(2)),
            listOf(1, 2).toJsonArray().toList()
        )
        assertEquals(
            listOf(JsonPrimitive(1L), JsonPrimitive(2L)),
            listOf(1L, 2L).toJsonArray().toList()
        )
        assertEquals(
            listOf(JsonPrimitive(1.5f), JsonPrimitive(2.5f)),
            listOf(1.5f, 2.5f).toJsonArray().toList()
        )
        assertEquals(
            listOf(JsonPrimitive(1.5), JsonPrimitive(2.5)),
            listOf(1.5, 2.5).toJsonArray().toList()
        )
        assertEquals(
            listOf(JsonPrimitive(true), JsonPrimitive(false)),
            listOf(true, false).toJsonArray().toList()
        )
    }

    @Test
    fun `putAll copies all fields into builder`() {
        val source = JsonObject(
            mapOf(
                "name" to JsonPrimitive("Alice"),
                "age" to JsonPrimitive(18)
            )
        )
        val target = buildJsonObject {
            put("id", JsonPrimitive(1))
            putAll(source)
        }

        assertEquals(JsonPrimitive(1), target["id"])
        assertEquals(JsonPrimitive("Alice"), target["name"])
        assertEquals(JsonPrimitive(18), target["age"])
    }
}
