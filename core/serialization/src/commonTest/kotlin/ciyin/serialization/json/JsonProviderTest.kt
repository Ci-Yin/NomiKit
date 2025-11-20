package ciyin.serialization.json

import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * [JsonProvider] 的单元测试类
 *
 * 注意: 所有测试数据类必须在类级别定义,不能在测试方法内定义
 * 因为 Moshi 不支持序列化局部类
 */
class JsonProviderTest {

    // 测试用的数据类 - 全部定义在类级别
    @Serializable
    data class User(
        val id: Int,
        val name: String,
        val email: String
    )

    @Serializable
    data class Product(
        val title: String,
        val price: Double,
        val inStock: Boolean
    )

    @Serializable
    data class NestedData(
        val user: User,
        val products: List<Product>
    )

    @Serializable
    data class Container(val items: List<String>)

    @Serializable
    data class OptionalData(
        val required: String,
        val optional: String? = null
    )

    @Serializable
    data class Message(val text: String)

    @Serializable
    data class Numbers(
        val int: Int,
        val long: Long,
        val double: Double,
        val float: Float
    )

    @Serializable
    data class Flags(val enabled: Boolean, val active: Boolean)

    @Test
    fun `test default JsonProvider singleton`() {
        // 验证默认单例可以正常访问
        assertNotNull(JsonProvider)
        assertNotNull(JsonProvider.configuration)
    }

//    @Test
//    fun `test fromJson with KClass parameter`() {
//        // 准备测试数据
//        val jsonString = """{"id":1,"name":"张三","email":"zhangsan@example.com"}"""
//
//        // 执行解析
//        val user = JsonProvider.fromJson(jsonString, User::class)
//
//        // 验证结果
//        assertEquals(1, user.id)
//        assertEquals("张三", user.name)
//        assertEquals("zhangsan@example.com", user.email)
//    }

    @Test
    fun `test fromJson with reified type parameter`() {
        // 准备测试数据
        val jsonString = """{"id":2,"name":"李四","email":"lisi@example.com"}"""

        // 使用 reified 版本
        val user = JsonProvider.fromJson<User>(jsonString)

        // 验证结果
        assertEquals(2, user.id)
        assertEquals("李四", user.name)
        assertEquals("lisi@example.com", user.email)
    }

    @Test
    fun `test fromJson with KClass parameter map`() {
        // 准备测试数据
        val jsonString = """{"id":"1","name":"张三","email":"zhangsan@example.com"}"""

        // 执行解析
        val user = JsonProvider.fromJson<Map<String, String>>(jsonString)

        // 验证结果
        assertEquals("1", user["id"])
        assertEquals("张三", user["name"])
        assertEquals("zhangsan@example.com", user["email"])
    }

    @Test
    fun `test fromJson with reified type parameter list`() {
        // 准备测试数据
        val jsonString = """[{"id":2,"name":"李四","email":"lisi@example.com"}]"""

        // 使用 reified 版本
        val users = JsonProvider.fromJson<List<User>>(jsonString)
        val user = users[0]

        // 验证结果
        assertEquals(2, user.id)
        assertEquals("李四", user.name)
        assertEquals("lisi@example.com", user.email)
    }

    @Test
    fun `test toJson serialization`() {
        // 准备测试对象
        val user = User(
            id = 3,
            name = "王五",
            email = "wangwu@example.com"
        )

        // 执行序列化
        val json = JsonProvider.toJson(user)

        // 验证结果包含所有字段
        assertTrue(json.contains("\"id\"") || json.contains("id"))
        assertTrue(json.contains("3"))
        assertTrue(json.contains("\"name\"") || json.contains("name"))
        assertTrue(json.contains("王五"))
        assertTrue(json.contains("\"email\"") || json.contains("email"))
        assertTrue(json.contains("wangwu@example.com"))
    }

    @Test
    fun `test round-trip serialization and deserialization`() {
        // 准备原始对象
        val original = Product(
            title = "Kotlin 编程指南",
            price = 99.99,
            inStock = true
        )

        // 序列化
        val json = JsonProvider.toJson(original)

        // 反序列化
        val deserialized = JsonProvider.fromJson<Product>(json)

        // 验证往返转换一致性
        assertEquals(original, deserialized)
    }

    @Test
    fun `test custom JsonProvider with builder`() {
        // 创建自定义配置的 JsonProvider
        val customProvider = JsonProvider {
            // 这里可以配置自定义选项
            // 具体配置项取决于 JsonBuilder 的实现
        }

        // 验证可以正常使用
        val user = User(4, "赵六", "zhaoliu@example.com")
        val json = customProvider.toJson(user)
        val parsed = customProvider.fromJson<User>(json)

        assertEquals(user, parsed)
    }

    @Test
    fun `test JsonProvider with custom codec`() {
        // 创建使用自定义编解码器的 JsonProvider
        val customProvider = JsonProvider(
            jsonCodec = { builder -> JsonCodec(builder) }
        ) {
            // 自定义配置
        }

        // 验证功能正常
        assertNotNull(customProvider.configuration)

        val product = Product("测试商品", 49.99, false)
        val json = customProvider.toJson(product)
        val parsed = customProvider.fromJson<Product>(json)

        assertEquals(product, parsed)
    }

    @Test
    fun `test nested object serialization`() {
        // 准备嵌套对象
        val nested = NestedData(
            user = User(5, "孙七", "sunqi@example.com"),
            products = listOf(
                Product("商品A", 29.99, true),
                Product("商品B", 39.99, false)
            )
        )

        // 序列化和反序列化
        val json = JsonProvider.toJson(nested)
        val parsed = JsonProvider.fromJson<NestedData>(json)

        // 验证嵌套结构
        assertEquals(nested.user, parsed.user)
        assertEquals(nested.products.size, parsed.products.size)
        assertEquals(nested.products[0], parsed.products[0])
        assertEquals(nested.products[1], parsed.products[1])
    }

    @Test
    fun `test empty list serialization`() {
        val container = Container(emptyList())
        val json = JsonProvider.toJson(container)
        val parsed = JsonProvider.fromJson<Container>(json)

        assertTrue(parsed.items.isEmpty())
    }

    @Test
    fun `test null handling in nullable fields`() {
        val data = OptionalData("必填项", null)
        val json = JsonProvider.toJson(data)
        val parsed = JsonProvider.fromJson<OptionalData>(json)

        assertEquals("必填项", parsed.required)
        assertNull(parsed.optional)
    }

    @Test
    fun `test nullable field with value`() {
        val data = OptionalData("必填项", "可选值")
        val json = JsonProvider.toJson(data)
        val parsed = JsonProvider.fromJson<OptionalData>(json)

        assertEquals("必填项", parsed.required)
        assertEquals("可选值", parsed.optional)
    }

    @Test
    fun `test multiple JsonProvider instances are independent`() {
        // 创建两个不同配置的实例
        val provider1 = JsonProvider { }
        val provider2 = JsonProvider { }

        // 验证它们是不同的实例
        assertNotSame(provider1, provider2)

        // 验证它们都能正常工作
        val user = User(6, "周八", "zhouba@example.com")
        val json1 = provider1.toJson(user)
        val json2 = provider2.toJson(user)

        // 两个提供器应该产生相同的结果(因为配置相同)
        assertEquals(json1, json2)
    }

    @Test
    fun `test configuration is accessible`() {
        val provider = JsonProvider()

        // 验证配置对象可访问
        assertNotNull(provider.configuration)
    }

    @Test
    fun `test special characters in strings`() {
        val message = Message("包含特殊字符: \"引号\", \\反斜杠\\, \n换行")
        val json = JsonProvider.toJson(message)
        val parsed = JsonProvider.fromJson<Message>(json)

        assertEquals(message.text, parsed.text)
    }

    @Test
    fun `test numeric types`() {
        val numbers = Numbers(
            int = 42,
            long = 9876543210L,
            double = 3.14159,
            float = 2.718f
        )

        val json = JsonProvider.toJson(numbers)
        val parsed = JsonProvider.fromJson<Numbers>(json)

        assertEquals(numbers.int, parsed.int)
        assertEquals(numbers.long, parsed.long)
        assertEquals(numbers.double, parsed.double, 0.00001)
        assertEquals(numbers.float, parsed.float, 0.00001f)
    }

    @Test
    fun `test boolean values`() {
        val flags = Flags(enabled = true, active = false)
        val json = JsonProvider.toJson(flags)
        val parsed = JsonProvider.fromJson<Flags>(json)

        assertTrue(parsed.enabled)
        assertFalse(parsed.active)
    }

    @Test
    fun `test list of primitives`() {
        val data = listOf("第一项", "第二项", "第三项")
        val json = JsonProvider.toJson(data)
        val parsed = JsonProvider.fromJson<List<String>>(json)

        assertEquals(3, parsed.size)
        assertEquals("第一项", parsed[0])
        assertEquals("第二项", parsed[1])
        assertEquals("第三项", parsed[2])
    }

    @Test
    fun `test map serialization`() {
        val data = mapOf(
            "key1" to "value1",
            "key2" to "value2"
        )

        val json = JsonProvider.toJson(data)
        val parsed = JsonProvider.fromJson<Map<String, String>>(json)

        assertEquals(2, parsed.size)
        assertEquals("value1", parsed["key1"])
        assertEquals("value2", parsed["key2"])
    }

    @Test
    fun `test empty map serialization`() {
        val data = emptyMap<String, String>()
        val json = JsonProvider.toJson(data)
        val parsed = JsonProvider.fromJson<Map<String, String>>(json)
        assertTrue(parsed.isEmpty())
    }

    @Test
    fun `test unicode characters`() {
        val message = Message("支持中文、日本語、한글、Emoji 😀🎉")
        val json = JsonProvider.toJson(message)
        val parsed = JsonProvider.fromJson<Message>(json)

        assertEquals(message.text, parsed.text)
    }

    @Test
    fun `test zero and negative numbers`() {
        val numbers = Numbers(
            int = -42,
            long = 0L,
            double = -3.14,
            float = 0.0f
        )

        val json = JsonProvider.toJson(numbers)
        val parsed = JsonProvider.fromJson<Numbers>(json)

        assertEquals(numbers.int, parsed.int)
        assertEquals(numbers.long, parsed.long)
        assertEquals(numbers.double, parsed.double, 0.00001)
        assertEquals(numbers.float, parsed.float, 0.00001f)
    }

    @Test
    fun `test large collection`() {
        val largeList = (1..100).map { "项目$it" }

        val json = JsonProvider.toJson(largeList)
        val parsed = JsonProvider.fromJson<List<String>>(json)

        assertEquals(100, parsed.size)
        assertEquals("项目1", parsed[0])
        assertEquals("项目100", parsed[99])
    }
}