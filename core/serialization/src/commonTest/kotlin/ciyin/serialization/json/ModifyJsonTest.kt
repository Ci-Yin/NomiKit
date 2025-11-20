package ciyin.serialization.json


import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * modifyJson 扩展函数的单元测试
 */
class ModifyJsonTest {

    @Serializable
    data class User(
        val id: Long,
        val name: String,
        val email: String?,
        val age: Int,
        val isActive: Boolean = true
    )

    @Serializable
    data class Product(
        val sku: String,
        val price: Double,
        val tags: List<String> = emptyList()
    )

    @Serializable
    data class Address(
        val city: String,
        val street: String
    )

    @Serializable
    data class Person(
        val name: String,
        val age: Int,
        val address: Address
    )

    @Test
    fun `测试修改单个字段`() {
        val user = User(
            id = 1,
            name = "张三",
            email = "old@example.com",
            age = 25
        )

        val updated = user.modifyJson(
            mapOf("email" to JsonPrimitive("new@example.com"))
        )

        assertEquals(1L, updated.id)
        assertEquals("张三", updated.name)
        assertEquals("new@example.com", updated.email)
        assertEquals(25, updated.age)
        assertEquals(true, updated.isActive)
    }

    @Test
    fun `测试修改多个字段`() {
        val user = User(
            id = 1,
            name = "张三",
            email = "test@example.com",
            age = 25
        )

        val updated = user.modifyJson(
            mapOf(
                "age" to JsonPrimitive(26),
                "isActive" to JsonPrimitive(false),
                "email" to JsonPrimitive("updated@example.com")
            )
        )

        assertEquals(26, updated.age)
        assertEquals(false, updated.isActive)
        assertEquals("updated@example.com", updated.email)
        // 其他字段保持不变
        assertEquals(1L, updated.id)
        assertEquals("张三", updated.name)
    }

    @Test
    fun `测试设置字段为null`() {
        val user = User(
            id = 1,
            name = "张三",
            email = "test@example.com",
            age = 25
        )

        val updated = user.modifyJson(
            mapOf("email" to JsonNull)
        )

        assertNull(updated.email)
        // 其他字段保持不变
        assertEquals(1L, updated.id)
        assertEquals("张三", updated.name)
    }

    @Test
    fun `测试修改集合类型字段`() {
        val product = Product(
            sku = "PROD-001",
            price = 99.99,
            tags = listOf("电子", "热销")
        )

        val newTags = buildJsonArray {
            add("电子")
            add("热销")
            add("限时优惠")
        }

        val updated = product.modifyJson(
            mapOf("tags" to newTags)
        )

        assertEquals(3, updated.tags.size)
        assertTrue(updated.tags.contains("限时优惠"))
    }

    @Test
    fun `测试修改嵌套对象字段`() {
        val person = Person(
            name = "李四",
            age = 30,
            address = Address("北京", "长安街")
        )

        val newAddress = buildJsonObject {
            put("city", "上海")
            put("street", "南京路")
        }

        val updated = person.modifyJson(
            mapOf("address" to newAddress)
        )

        assertEquals("上海", updated.address.city)
        assertEquals("南京路", updated.address.street)
        assertEquals("李四", updated.name)
        assertEquals(30, updated.age)
    }

    @Test
    fun `测试不修改任何字段`() {
        val user = User(
            id = 1,
            name = "张三",
            email = "test@example.com",
            age = 25
        )

        val updated = user.modifyJson(emptyMap())

        // 应该返回相同内容的新对象
        assertEquals(user.id, updated.id)
        assertEquals(user.name, updated.name)
        assertEquals(user.email, updated.email)
        assertEquals(user.age, updated.age)
        assertEquals(user.isActive, updated.isActive)
    }

    @Test
    fun `测试修改不存在的字段会被忽略`() {
        val user = User(
            id = 1,
            name = "张三",
            email = "test@example.com",
            age = 25
        )

        // 添加不存在的字段 - 现在会被忽略
        val updated = user.modifyJson(
            mapOf(
                "age" to JsonPrimitive(26),
                "nonExistentField" to JsonPrimitive("value")
            )
        )

        // age 应该被更新
        assertEquals(26, updated.age)
        // 其他字段保持不变
        assertEquals(1L, updated.id)
        assertEquals("张三", updated.name)
        assertEquals("test@example.com", updated.email)
    }

    @Test
    fun `测试对基本类型抛出异常`() {
        val number = 42

        val exception = assertFailsWith<IllegalArgumentException> {
            number.modifyJson(mapOf("key" to JsonPrimitive("value")))
        }

        assertTrue(exception.message?.contains("Int") == true)
        assertTrue(exception.message?.contains("JsonObject") == true)
    }

    @Test
    fun `测试对字符串抛出异常`() {
        val text = "Hello"

        val exception = assertFailsWith<IllegalArgumentException> {
            text.modifyJson(mapOf("key" to JsonPrimitive("value")))
        }

        assertTrue(exception.message?.contains("String") == true)
        assertTrue(exception.message?.contains("JsonObject") == true)
    }

    @Test
    fun `测试对List抛出异常`() {
        @Serializable
        data class Wrapper(val items: List<String>)

        val list = listOf("a", "b", "c")

        val exception = assertFailsWith<IllegalArgumentException> {
            list.modifyJson(mapOf("key" to JsonPrimitive("value")))
        }

        // 检查异常消息包含 JsonArray 相关信息
        assertTrue(exception.message?.contains("JsonObject") == true)
    }

    @Test
    fun `测试类型转换`() {
        val user = User(
            id = 1,
            name = "张三",
            email = "test@example.com",
            age = 25
        )

        // 尝试用字符串更新数字字段
        val updated = user.modifyJson(
            mapOf("age" to JsonPrimitive("26"))
        )

        // kotlinx.serialization 会自动处理类型转换
        assertEquals(26, updated.age)
    }

    @Test
    fun `测试修改Boolean字段`() {
        val user = User(
            id = 1,
            name = "张三",
            email = "test@example.com",
            age = 25,
            isActive = true
        )

        val updated = user.modifyJson(
            mapOf("isActive" to JsonPrimitive(false))
        )

        assertEquals(false, updated.isActive)
    }

    @Test
    fun `测试修改Long类型字段`() {
        val user = User(
            id = 1,
            name = "张三",
            email = "test@example.com",
            age = 25
        )

        val updated = user.modifyJson(
            mapOf("id" to JsonPrimitive(999L))
        )

        assertEquals(999L, updated.id)
    }

    @Test
    fun `测试原对象不被修改`() {
        val original = User(
            id = 1,
            name = "张三",
            email = "test@example.com",
            age = 25
        )

        val updated = original.modifyJson(
            mapOf("age" to JsonPrimitive(26))
        )

        // 原对象应该保持不变
        assertEquals(25, original.age)
        // 新对象应该被修改
        assertEquals(26, updated.age)
    }

    @Test
    fun `测试连续修改`() {
        val user = User(
            id = 1,
            name = "张三",
            email = "test@example.com",
            age = 25
        )

        val updated = user
            .modifyJson(mapOf("age" to JsonPrimitive(26)))
            .modifyJson(mapOf("email" to JsonPrimitive("new@example.com")))
            .modifyJson(mapOf("isActive" to JsonPrimitive(false)))

        assertEquals(26, updated.age)
        assertEquals("new@example.com", updated.email)
        assertEquals(false, updated.isActive)
    }

    @Test
    fun `测试修改多个不存在字段只更新有效字段`() {
        val user = User(
            id = 1,
            name = "张三",
            email = "test@example.com",
            age = 25
        )

        val updated = user.modifyJson(
            mapOf(
                "age" to JsonPrimitive(30),
                "fakeField1" to JsonPrimitive("value1"),
                "name" to JsonPrimitive("李四"),
                "fakeField2" to JsonPrimitive("value2")
            )
        )

        // 有效字段应该被更新
        assertEquals(30, updated.age)
        assertEquals("李四", updated.name)
        // 其他字段保持不变
        assertEquals(1L, updated.id)
        assertEquals("test@example.com", updated.email)
    }
}