package ciyin.serialization.json

import ciyin.io.File
import ciyin.io.readText
import ciyin.io.writeText
import kotlinx.serialization.Serializable
import okio.FileNotFoundException
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * JSON 扩展函数的测试类
 *
 * 测试覆盖：
 * - String.fromJson() - JSON 字符串反序列化
 * - Any.toJsonStr() - 对象序列化为 JSON
 * - File.writeJson() - 写入 JSON 到文件
 * - File.readJson() - 从文件读取 JSON
 */
class JsonExtensionsTest {

    /**
     * 测试用户数据类
     * 包含基本类型、可空类型和默认值
     */
    @Serializable
    data class User(
        val id: Int,
        val name: String,
        val email: String? = null,
        val active: Boolean = true
    )

    /**
     * 测试产品数据类
     * 包含集合类型
     */
    @Serializable
    data class Product(
        val productId: String,
        val price: Double,
        val tags: List<String> = emptyList()
    )

    /**
     * 复杂嵌套数据类
     * 包含对象、集合和映射
     */
    @Serializable
    data class ComplexData(
        val user: User,
        val products: List<Product>,
        val metadata: Map<String, String> = emptyMap()
    )

    private lateinit var testFile: File

    @BeforeTest
    fun setup() {
        // 创建临时测试文件
        testFile = File.createTempFile("test_json", ".json")
    }

    @AfterTest
    fun tearDown() {
        // 清理测试文件
        if (testFile.exists()) {
            testFile.delete()
        }
    }

    // ==================== String.fromJson() 测试 ====================

    /**
     * 测试：反序列化简单对象
     * 验证所有字段都能正确解析
     */
    @Test
    fun `fromJson should deserialize simple object`() {
        val json = """{"id":1,"name":"Alice","email":"alice@example.com","active":true}"""
        val user: User = json.fromJson()

        assertEquals(1, user.id)
        assertEquals("Alice", user.name)
        assertEquals("alice@example.com", user.email)
        assertTrue(user.active)
    }

    /**
     * 测试：处理 null 值
     * 验证可空字段能正确处理 null
     */
    @Test
    fun `fromJson should handle null values`() {
        val json = """{"id":2,"name":"Bob","email":null,"active":false}"""
        val user: User = json.fromJson()

        assertEquals(2, user.id)
        assertEquals("Bob", user.name)
        assertNull(user.email)
        assertFalse(user.active)
    }

    /**
     * 测试：使用默认值
     * 验证缺失字段能使用类定义的默认值
     */
    @Test
    fun `fromJson should use default values when fields missing`() {
        val json = """{"id":3,"name":"Charlie"}"""
        val user: User = json.fromJson()

        assertEquals(3, user.id)
        assertEquals("Charlie", user.name)
        assertNull(user.email)
        assertTrue(user.active) // 使用默认值
    }

    /**
     * 测试：反序列化列表
     * 验证能正确解析 JSON 数组为类型化列表
     */
    @Test
    fun `fromJson should deserialize list`() {
        val json = """[
            {"id":1,"name":"Alice"},
            {"id":2,"name":"Bob"}
        ]"""

        // 关键：使用 List<User> 类型参数，而不是依赖类型推断
        val users: List<User> = json.fromJson()

        assertEquals(2, users.size)
        assertEquals("Alice", users[0].name)
        assertEquals("Bob", users[1].name)
    }

    /**
     * 测试：反序列化嵌套对象
     * 验证能处理复杂的嵌套结构
     */
    @Test
    fun `fromJson should deserialize nested objects`() {
        val json = """
        {
            "user": {"id":1,"name":"Alice"},
            "products": [
                {"productId":"P001","price":99.99,"tags":["electronics","sale"]},
                {"productId":"P002","price":49.99}
            ],
            "metadata": {"source":"api","version":"1.0"}
        }
        """.trimIndent()

        val data: ComplexData = json.fromJson()

        assertEquals("Alice", data.user.name)
        assertEquals(2, data.products.size)
        assertEquals("P001", data.products[0].productId)
        assertEquals(99.99, data.products[0].price, 0.001)
        assertEquals(2, data.products[0].tags.size)
        assertEquals("api", data.metadata["source"])
    }

    /**
     * 测试：反序列化基本类型
     * 验证能处理单个基本类型值
     */
    @Test
    fun `fromJson should deserialize primitive types`() {
        val intJson = "42"
        val int: Int = intJson.fromJson()
        assertEquals(42, int)

        val stringJson = "\"hello\""
        val string: String = stringJson.fromJson()
        assertEquals("hello", string)

        val boolJson = "true"
        val bool: Boolean = boolJson.fromJson()
        assertTrue(bool)
    }

    /**
     * 测试：处理无效 JSON
     * 验证能正确抛出异常
     */
    @Test
    fun `fromJson should throw exception on invalid json`() {
        try {
            val invalidJson = """{"id":1,"name":}"""
            invalidJson.fromJson<User>()
            throw AssertionError("Should have thrown exception")
        } catch (e: Exception) {
            println(e.message)
            // 预期会抛出异常，验证异常消息
            assertFalse(e.message?.contains("Should have thrown exception") ?: false)
        }
    }

    // ==================== Any.toJsonStr() 测试 ====================

    /**
     * 测试：序列化简单对象（无格式化）
     * 验证生成紧凑的 JSON 字符串
     */
    @Test
    fun `toJsonStr should serialize simple object without formatting`() {
        val user = User(1, "Alice", "alice@example.com", true)
        val json = user.toJsonStr(isFormat = false)

        // 紧凑格式不应包含换行符
        assertFalse(json.contains("\n"))
        // 验证包含关键字段（允许有或无空格）
        assertTrue(json.contains("\"id\":1") || json.contains("\"id\": 1"))
        assertTrue(json.contains("\"name\":\"Alice\"") || json.contains("\"name\": \"Alice\""))
    }

    /**
     * 测试：序列化时使用美化输出
     * 验证生成格式化的 JSON
     */
    @Test
    fun `toJsonStr should serialize with pretty print when isFormat is true`() {
        val user = User(1, "Alice", "alice@example.com")
        val json = user.toJsonStr(isFormat = true)

        // 格式化输出应包含换行符
        assertTrue(json.contains("\n"))
    }

    /**
     * 测试：序列化包含 null 的对象
     * 验证 null 值的处理
     */
    @Test
    fun `toJsonStr should handle null values`() {
        val user = User(1, "Alice", null)
        val json = user.toJsonStr()

        assertTrue(json.contains("\"name\":\"Alice\"") || json.contains("\"name\": \"Alice\""))
    }

    /**
     * 测试：序列化集合
     * 验证能将列表转换为 JSON 数组
     *
     * 重要：必须显式声明为 List<User> 类型
     */
    @Test
    fun `toJsonStr should serialize collections`() {
        // 显式声明类型为 List<User>，避免运行时类型为 ArrayList
        val users: List<User> = listOf(
            User(1, "Alice"),
            User(2, "Bob")
        )
        val json = users.toJsonStr()

        assertTrue(json.startsWith("["))
        assertTrue(json.endsWith("]"))
        assertTrue(json.contains("Alice"))
        assertTrue(json.contains("Bob"))
    }

    /**
     * 测试：序列化嵌套对象
     * 验证能处理复杂的对象图
     */
    @Test
    fun `toJsonStr should serialize nested objects`() {
        val product = Product("P001", 99.99, listOf("electronics", "sale"))
        val user = User(1, "Alice")
        val data = ComplexData(user, listOf(product), mapOf("key" to "value"))

        val json = data.toJsonStr()

        assertTrue(json.contains("P001"))
        assertTrue(json.contains("Alice"))
        assertTrue(json.contains("key"))
    }

    /**
     * 测试：序列化基本类型
     * 验证能处理单个值
     */
    @Test
    fun `toJsonStr should serialize primitive types`() {
        assertEquals("42", 42.toJsonStr())
        assertEquals("\"hello\"", "hello".toJsonStr())
        assertEquals("true", true.toJsonStr())
        assertTrue(3.14.toJsonStr().contains("3.14"))
    }

    // ==================== File.writeJson() 测试 ====================

    /**
     * 测试：写入对象到文件（无格式化）
     * 验证能生成紧凑的 JSON 文件
     */
    @Test
    fun `writeJson should write object to file without formatting`() {
        val user = User(1, "Alice", "alice@example.com")
        testFile.writeJson(user, isFormat = false)

        assertTrue(testFile.exists())
        val content = testFile.readText()
        // 移除可能的尾随换行符后检查
        assertFalse(content.trim().contains("\n"))
        assertTrue(content.contains("Alice"))
    }

    /**
     * 测试：写入对象到文件（格式化）
     * 验证能生成可读的 JSON 文件
     */
    @Test
    fun `writeJson should write object to file with pretty print`() {
        val user = User(1, "Alice", "alice@example.com")
        testFile.writeJson(user, isFormat = true)

        assertTrue(testFile.exists())
        val content = testFile.readText()
        // 验证内容有效且包含数据
        assertTrue(content.contains("Alice"))
    }

    /**
     * 测试：覆盖已存在的文件
     * 验证能正确覆盖文件内容
     */
    @Test
    fun `writeJson should overwrite existing file`() {
        // 第一次写入
        val user1 = User(1, "Alice")
        testFile.writeJson(user1)

        // 第二次写入覆盖
        val user2 = User(2, "Bob")
        testFile.writeJson(user2)

        val content = testFile.readText()
        assertTrue(content.contains("Bob"))
        assertFalse(content.contains("Alice"))
    }

    /**
     * 测试：写入复杂嵌套对象
     * 验证能处理多层嵌套结构
     */
    @Test
    fun `writeJson should write complex nested objects`() {
        val product = Product("P001", 99.99, listOf("tag1", "tag2"))
        val user = User(1, "Alice")
        val data = ComplexData(user, listOf(product))

        testFile.writeJson(data, isFormat = true)

        assertTrue(testFile.exists())
        val content = testFile.readText()
        assertTrue(content.contains("Alice"))
        assertTrue(content.contains("P001"))
        assertTrue(content.contains("tag1"))
    }

    /**
     * 测试：写入空集合
     * 验证能正确处理空列表和空 Map
     */
    @Test
    fun `writeJson should write empty collections`() {
        val data = ComplexData(
            user = User(1, "Alice"),
            products = emptyList(),
            metadata = emptyMap()
        )
        testFile.writeJson(data)

        val content = testFile.readText()
        assertTrue(content.contains("Alice"))
    }

    // ==================== File.readJson() 测试 ====================

    /**
     * 测试：从文件读取对象
     * 验证能正确反序列化文件内容
     */
    @Test
    fun `readJson should read object from file`() {
        val user = User(1, "Alice", "alice@example.com")
        testFile.writeText(user.toJsonStr())

        val readUser: User = testFile.readJson()

        assertEquals(user.id, readUser.id)
        assertEquals(user.name, readUser.name)
        assertEquals(user.email, readUser.email)
    }

    /**
     * 测试：从文件读取列表
     * 验证能正确解析 JSON 数组
     */
    @Test
    fun `readJson should read list from file`() {
        val users: List<User> = listOf(
            User(1, "Alice"),
            User(2, "Bob")
        )
        testFile.writeText(users.toJsonStr())

        val readUsers: List<User> = testFile.readJson()

        assertEquals(2, readUsers.size)
        assertEquals("Alice", readUsers[0].name)
        assertEquals("Bob", readUsers[1].name)
    }

    /**
     * 测试：从文件读取嵌套对象
     * 验证能处理复杂结构的读取
     */
    @Test
    fun `readJson should read nested objects from file`() {
        val product = Product("P001", 99.99, listOf("tag1"))
        val user = User(1, "Alice")
        val data = ComplexData(user, listOf(product))

        testFile.writeText(data.toJsonStr())
        val readData: ComplexData = testFile.readJson()

        assertEquals("Alice", readData.user.name)
        assertEquals(1, readData.products.size)
        assertEquals("P001", readData.products[0].productId)
    }

    /**
     * 测试：读取格式化的 JSON
     * 验证能解析包含空白字符的 JSON
     */
    @Test
    fun `readJson should handle pretty printed json`() {
        val user = User(1, "Alice")
        testFile.writeJson(user, isFormat = true)

        val readUser: User = testFile.readJson()

        assertEquals(user.id, readUser.id)
        assertEquals(user.name, readUser.name)
    }

    /**
     * 测试：文件不存在时抛出异常
     * 验证错误处理
     */
    @Test
    fun `readJson should throw exception when file does not exist`() {
        try {
            val nonExistentFile = File("non_existent_file.json")
            nonExistentFile.readJson<User>()
            throw AssertionError("Should have thrown exception")
        } catch (e: Exception) {
            // 预期抛出 FileNotFoundException
            assertTrue(e is FileNotFoundException || e.message?.contains("找不到") ?: false)
        }
    }

    /**
     * 测试：无效 JSON 内容时抛出异常
     * 验证能检测到格式错误
     */
    @Test
    fun `readJson should throw exception on invalid json content`() {
        try {
            testFile.writeText("invalid json content")
            testFile.readJson<User>()
            throw AssertionError("Should have thrown exception")
        } catch (e: Exception) {
            // 预期抛出 JSON 解析异常
            assertTrue(true)
        }
    }

    // ==================== 集成测试 ====================

    /**
     * 集成测试：完整的写入-读取循环
     * 验证数据的完整性保持
     */
    @Test
    fun `integration test - write and read cycle`() {
        val originalUser = User(
            id = 100,
            name = "Integration Test User",
            email = "test@example.com",
            active = false
        )

        // 写入文件
        testFile.writeJson(originalUser, isFormat = true)

        // 从文件读取
        val readUser: User = testFile.readJson()

        // 验证所有字段一致
        assertEquals(originalUser.id, readUser.id)
        assertEquals(originalUser.name, readUser.name)
        assertEquals(originalUser.email, readUser.email)
        assertEquals(originalUser.active, readUser.active)
    }

    /**
     * 集成测试：复杂数据的写入-读取循环
     * 验证嵌套结构的数据完整性
     */
    @Test
    fun `integration test - complex data write and read cycle`() {
        val originalData = ComplexData(
            user = User(1, "Alice", "alice@example.com"),
            products = listOf(
                Product("P001", 99.99, listOf("electronics", "new")),
                Product("P002", 49.99, listOf("books"))
            ),
            metadata = mapOf(
                "version" to "1.0",
                "source" to "test"
            )
        )

        // 写入并读取
        testFile.writeJson(originalData, isFormat = true)
        val readData: ComplexData = testFile.readJson()

        // 验证嵌套数据
        assertEquals(originalData.user.name, readData.user.name)
        assertEquals(originalData.products.size, readData.products.size)
        assertEquals(originalData.products[0].productId, readData.products[0].productId)
        assertEquals(originalData.metadata["version"], readData.metadata["version"])
    }

    /**
     * 集成测试：序列化-反序列化的对称性
     * 验证 toJsonStr 和 fromJson 是互逆操作
     */
    @Test
    fun `integration test - fromJson and toJsonStr are symmetrical`() {
        val original = User(1, "Alice", "alice@example.com", true)

        // 对象 -> JSON -> 对象
        val json = original.toJsonStr()
        val restored: User = json.fromJson()

        // 验证对称性
        assertEquals(original.id, restored.id)
        assertEquals(original.name, restored.name)
        assertEquals(original.email, restored.email)
        assertEquals(original.active, restored.active)
    }

    /**
     * 性能测试：大列表序列化
     * 验证能在合理时间内处理大量数据
     */
    @OptIn(ExperimentalTime::class)
    @Test
    fun `performance test - large list serialization`() {
        // 创建 1000 个用户的列表，显式声明为 List 类型
        val largeList: List<User> = (1..1000).map { i ->
            User(i, "User$i", "user$i@example.com")
        }

        val startTime = Clock.System.now().toEpochMilliseconds()
        val json = largeList.toJsonStr()
        val endTime = Clock.System.now().toEpochMilliseconds()

        println("Serialized 1000 users in ${endTime - startTime}ms")
        assertTrue(json.isNotEmpty())
        assertTrue(endTime - startTime < 5000) // 应在 5 秒内完成
    }

    /**
     * 性能测试：大列表反序列化
     * 验证解析性能
     */
    @OptIn(ExperimentalTime::class)
    @Test
    fun `performance test - large list deserialization`() {
        val largeList: List<User> = (1..1000).map { i ->
            User(i, "User$i", "user$i@example.com")
        }
        val json = largeList.toJsonStr()

        val startTime = Clock.System.now().toEpochMilliseconds()
        val restored: List<User> = json.fromJson()
        val endTime = Clock.System.now().toEpochMilliseconds()

        println("Deserialized 1000 users in ${endTime - startTime}ms")
        assertEquals(1000, restored.size)
        assertTrue(endTime - startTime < 5000) // 应在 5 秒内完成
    }
}