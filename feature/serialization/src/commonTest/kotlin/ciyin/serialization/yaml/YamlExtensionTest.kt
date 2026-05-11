package ciyin.serialization.yaml

import ciyin.io.File
import ciyin.io.readText
import kotlinx.serialization.Serializable
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * YAML 扩展函数测试。
 */
class YamlExtensionTest {

    /**
     * 测试用用户模型。
     *
     * @property id 用户标识。
     * @property name 用户名称。
     * @property tags 用户标签列表。
     */
    @Serializable
    private data class User(
        val id: Int,
        val name: String,
        val tags: List<String>
    )

    /**
     * 当前测试使用的临时 YAML 文件。
     */
    private lateinit var testFile: File

    /**
     * 创建每个测试用例所需的临时文件。
     */
    @BeforeTest
    fun setup() {
        testFile = File.createTempFile("yaml_extensions", ".yaml")
    }

    /**
     * 清理测试过程中创建的临时文件。
     */
    @AfterTest
    fun tearDown() {
        if (testFile.exists()) {
            testFile.delete()
        }
    }

    /**
     * 验证对象可以序列化为 YAML 字符串。
     */
    @Test
    fun `toYamlStr should serialize data class to yaml string`() {
        val user = User(
            id = 1,
            name = "Alice",
            tags = listOf("admin", "active")
        )

        val yaml = user.toYamlStr()

        assertTrue(yaml.contains("id: 1"))
        assertTrue(yaml.contains("name: Alice"))
        assertTrue(yaml.contains("tags:"))
        assertTrue(yaml.contains("admin"))
    }

    /**
     * 验证 YAML 字符串可以反序列化为对象。
     */
    @Test
    fun `fromYaml should deserialize yaml string to data class`() {
        val yaml = """
            id: 2
            name: Bob
            tags:
            - reader
            - tester
        """.trimIndent()

        val user: User = yaml.fromYaml()

        assertEquals(2, user.id)
        assertEquals("Bob", user.name)
        assertEquals(listOf("reader", "tester"), user.tags)
    }

    /**
     * 验证文件 YAML 写入与读取可以保持数据一致。
     */
    @Test
    fun `writeYaml and readYaml should preserve data`() {
        val user = User(
            id = 3,
            name = "Carol",
            tags = listOf("writer")
        )

        testFile.writeYaml(user)
        val restored: User = testFile.readYaml()

        assertEquals(user, restored)
        assertTrue(testFile.readText().contains("Carol"))
    }

    /**
     * 验证显式序列化器重载可以正常写入与读取。
     */
    @Test
    fun `serializer overloads should write and read yaml`() {
        val user = User(
            id = 4,
            name = "Dave",
            tags = listOf("explicit")
        )

        testFile.writeYaml(User.serializer(), user)
        val restored = testFile.readYaml(User.serializer())

        assertEquals(user, restored)
    }
}
