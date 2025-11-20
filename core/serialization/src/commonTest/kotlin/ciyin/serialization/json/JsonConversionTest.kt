package ciyin.serialization.json

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

class JsonConversionTest {

    @Test
    fun `测试基本类型转换为 JsonPrimitive`() {
        assertEquals(JsonPrimitive("hello"), "hello".toJsonPrimitive())
        assertEquals(JsonPrimitive(42), 42.toJsonPrimitive())
        assertEquals(JsonPrimitive(3.14), 3.14.toJsonPrimitive())
        assertEquals(JsonPrimitive(true), true.toJsonPrimitive())
        assertEquals(JsonPrimitive("A"), 'A'.toJsonPrimitive())
        assertEquals(JsonPrimitive(100L), 100L.toJsonPrimitive())
        assertEquals(JsonPrimitive(3.14f), 3.14f.toJsonPrimitive())
    }

    @Test
    fun `测试基本类型转换为 JsonElement`() {
        assertEquals(JsonPrimitive("test"), "test".toJsonElement())
        assertEquals(JsonPrimitive(123), 123.toJsonElement())
        assertEquals(JsonPrimitive(false), false.toJsonElement())
        assertEquals(JsonNull, null.toJsonElement())
    }

    @Test
    fun `测试 List 转换为 JsonArray`() {
        val list = listOf(1, 2, 3, 4, 5)
        val jsonArray = list.toJsonElement()

        assertTrue(jsonArray is JsonArray)
        assertEquals(5, jsonArray.size)
        assertEquals(JsonPrimitive(1), jsonArray[0])
        assertEquals(JsonPrimitive(5), jsonArray[4])
    }

    @Test
    fun `测试混合类型 List`() {
        val list = listOf(1, "hello", true, 3.14, null)
        val jsonArray = list.toJsonElement() as JsonArray

        assertEquals(5, jsonArray.size)
        assertEquals(JsonPrimitive(1), jsonArray[0])
        assertEquals(JsonPrimitive("hello"), jsonArray[1])
        assertEquals(JsonPrimitive(true), jsonArray[2])
        assertEquals(JsonPrimitive(3.14), jsonArray[3])
        assertEquals(JsonNull, jsonArray[4])
    }

    @Test
    fun `测试 Set 转换为 JsonArray`() {
        val set = setOf("a", "b", "c")
        val jsonArray = set.toJsonElement()

        assertTrue(jsonArray is JsonArray)
        assertEquals(3, jsonArray.size)
    }

    @Test
    fun `测试 Array 转换为 JsonArray`() {
        val array = arrayOf(1, 2, 3)
        val jsonArray = array.toJsonElement()

        assertTrue(jsonArray is JsonArray)
        assertEquals(3, jsonArray.size)
    }

    @Test
    fun `测试原始数组转换`() {
        val intArray = intArrayOf(1, 2, 3)
        val doubleArray = doubleArrayOf(1.1, 2.2, 3.3)
        val boolArray = booleanArrayOf(true, false, true)

        assertTrue(intArray.toJsonElement() is JsonArray)
        assertTrue(doubleArray.toJsonElement() is JsonArray)
        assertTrue(boolArray.toJsonElement() is JsonArray)

        assertEquals(3, (intArray.toJsonElement() as JsonArray).size)
        assertEquals(3, (doubleArray.toJsonElement() as JsonArray).size)
        assertEquals(3, (boolArray.toJsonElement() as JsonArray).size)
    }

    @Test
    fun `测试 Map 转换为 JsonObject`() {
        val map = mapOf(
            "name" to "张三",
            "age" to 25,
            "active" to true
        )

        val jsonObject = map.toJsonElement()

        assertTrue(jsonObject is JsonObject)
        assertEquals(JsonPrimitive("张三"), jsonObject["name"])
        assertEquals(JsonPrimitive(25), jsonObject["age"])
        assertEquals(JsonPrimitive(true), jsonObject["active"])
    }

    @Test
    fun `测试嵌套 Map`() {
        val map = mapOf(
            "user" to mapOf(
                "name" to "李四",
                "age" to 30
            ),
            "settings" to mapOf(
                "theme" to "dark",
                "notifications" to true
            )
        )

        val jsonObject = map.toJsonElement() as JsonObject
        val userObject = jsonObject["user"] as JsonObject
        val settingsObject = jsonObject["settings"] as JsonObject

        assertEquals(JsonPrimitive("李四"), userObject["name"])
        assertEquals(JsonPrimitive(30), userObject["age"])
        assertEquals(JsonPrimitive("dark"), settingsObject["theme"])
        assertEquals(JsonPrimitive(true), settingsObject["notifications"])
    }

    @Test
    fun `测试嵌套 List 和 Map`() {
        val data = mapOf(
            "name" to "产品A",
            "tags" to listOf("热销", "推荐", "新品"),
            "specs" to mapOf(
                "color" to "红色",
                "size" to "L"
            ),
            "prices" to listOf(99.99, 89.99, 79.99)
        )

        val jsonObject = data.toJsonElement() as JsonObject

        assertTrue(jsonObject["tags"] is JsonArray)
        assertTrue(jsonObject["specs"] is JsonObject)
        assertTrue(jsonObject["prices"] is JsonArray)

        val tags = jsonObject["tags"] as JsonArray
        assertEquals(3, tags.size)
        assertEquals(JsonPrimitive("热销"), tags[0])
    }

    @Test
    fun `测试 JsonElement 直接返回`() {
        val primitive = JsonPrimitive("test")
        val array = buildJsonArray { add("a"); add("b") }
        val obj = buildJsonObject { put("key", "value") }

        assertSame(primitive, primitive.toJsonElement())
        assertSame(array, array.toJsonElement())
        assertSame(obj, obj.toJsonElement())
    }

    @Test
    fun `测试 null 转换`() {
        val nullValue: String? = null
        assertEquals(JsonNull, nullValue.toJsonElement())
    }

    @Test
    fun `测试 jsonObjectOf 构建器`() {
        val json = jsonObjectOf(
            "name" to "王五",
            "age" to 35,
            "active" to true,
            "email" to null
        )

        assertEquals(JsonPrimitive("王五"), json["name"])
        assertEquals(JsonPrimitive(35), json["age"])
        assertEquals(JsonPrimitive(true), json["active"])
        assertEquals(JsonNull, json["email"])
    }

    @Test
    fun `测试 jsonArrayOf 构建器`() {
        val json = jsonArrayOf(1, "hello", true, null, 3.14)

        assertEquals(5, json.size)
        assertEquals(JsonPrimitive(1), json[0])
        assertEquals(JsonPrimitive("hello"), json[1])
        assertEquals(JsonPrimitive(true), json[2])
        assertEquals(JsonNull, json[3])
        assertEquals(JsonPrimitive(3.14), json[4])
    }

    @Test
    fun `测试复杂嵌套结构`() {
        val json = jsonObjectOf(
            "user" to jsonObjectOf(
                "name" to "赵六",
                "tags" to jsonArrayOf("VIP", "活跃用户")
            ),
            "orders" to jsonArrayOf(
                jsonObjectOf("id" to 1, "total" to 99.99),
                jsonObjectOf("id" to 2, "total" to 149.99)
            )
        )

        assertTrue(json["user"] is JsonObject)
        assertTrue(json["orders"] is JsonArray)

        val user = json["user"] as JsonObject
        assertEquals(JsonPrimitive("赵六"), user["name"])

        val orders = json["orders"] as JsonArray
        assertEquals(2, orders.size)
    }

    @Test
    fun `测试不支持的类型抛出异常 - toJsonPrimitive`() {
        val list = listOf(1, 2, 3)

        val exception = assertFailsWith<IllegalArgumentException> {
            list.toJsonPrimitive()
        }

        assertTrue(exception.message?.contains("ArrayList") == true)
        assertTrue(exception.message?.contains("JsonPrimitive") == true)
    }

    @Test
    fun `测试不支持的自定义类型 - toJsonElement`() {
        class CustomClass

        val custom = CustomClass()

        val exception = assertFailsWith<IllegalArgumentException> {
            custom.toJsonElement()
        }

        assertTrue(exception.message?.contains("CustomClass") == true)
    }

    @Test
    fun `测试空集合`() {
        val emptyList = emptyList<String>()
        val emptyMap = emptyMap<String, Any>()

        val jsonArray = emptyList.toJsonElement() as JsonArray
        val jsonObject = emptyMap.toJsonElement() as JsonObject

        assertEquals(0, jsonArray.size)
        assertEquals(0, jsonObject.size)
    }

    @Test
    fun `测试 Map 的 null key 被忽略`() {
        val map = mapOf<String?, Any?>(
            "valid" to "value",
            null to "ignored"
        )

        val jsonObject = map.toJsonElement() as JsonObject

        assertEquals(1, jsonObject.size)
        assertTrue(jsonObject.containsKey("valid"))
    }

    @Test
    fun `测试与 modifyJson 配合使用`() {
        @Serializable
        data class User(val name: String, val age: Int, val active: Boolean)

        val user = User("测试", 20, true)

        val updated = user.modifyJson(
            mapOf(
                "age" to 21.toJsonPrimitive(),
                "active" to false.toJsonPrimitive()
            )
        )

        assertEquals(21, updated.age)
        assertEquals(false, updated.active)
        assertEquals("测试", updated.name)
    }
}