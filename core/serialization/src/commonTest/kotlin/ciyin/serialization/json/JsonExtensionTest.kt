package ciyin.serialization.json

import ciyin.io.File
import ciyin.io.readText
import ciyin.io.writeText
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * [JsonExtension.kt] 中 JSON 序列化与对象修改扩展的单元测试。
 */
class JsonExtensionTest {

    @Serializable
    private data class User(
        val id: Int,
        val name: String,
        val email: String? = null,
        val active: Boolean = true
    )

    @Serializable
    private data class Profile(
        val user: User,
        val tags: List<String> = emptyList()
    )

    private lateinit var testFile: File

    @BeforeTest
    fun setup() {
        testFile = File.createTempFile("json_extension", ".json")
    }

    @AfterTest
    fun tearDown() {
        if (testFile.exists()) {
            testFile.delete()
        }
    }

    @Test
    fun `fromJson decodes primitives collections and objects`() {
        val user: User = """{"id":1,"name":"Alice"}""".fromJson()
        val users: List<User> = """[{"id":2,"name":"Bob"}]""".fromJson()
        val number: Int = "42".fromJson()

        assertEquals(User(1, "Alice"), user)
        assertEquals(User(2, "Bob"), users.single())
        assertEquals(42, number)
    }

    @Test
    fun `fromJson uses builder action`() {
        val user: User = """{"id":1,"name":"Alice","extra":"ignored"}""".fromJson {
            ignoreUnknownKeys = true
        }

        assertEquals(User(1, "Alice"), user)
    }

    @Test
    fun `fromJson throws for invalid json`() {
        assertFailsWith<SerializationException> {
            """{"id":1,"name":}""".fromJson<User>()
        }
    }

    @Test
    fun `toJsonStr encodes compact and pretty json`() {
        val user = User(1, "Alice", "alice@example.com")
        val compact = user.toJsonStr()
        val pretty = user.toJsonStr {
            prettyPrint = true
        }

        assertFalse(compact.contains("\n"))
        assertTrue(compact.contains("\"name\":\"Alice\""))
        assertTrue(pretty.contains("\n"))
    }

    @Test
    fun `toJsonStr encodes nested values`() {
        val profile = Profile(
            user = User(1, "Alice"),
            tags = listOf("new", "vip")
        )
        val json = profile.toJsonStr()
        val decoded: Profile = json.fromJson()

        assertEquals(profile, decoded)
    }

    @Test
    fun `writeJson and readJson round trip reified values`() {
        val profile = Profile(
            user = User(1, "Alice", active = false),
            tags = listOf("new")
        )

        testFile.writeJson(profile) {
            prettyPrint = true
        }
        val restored: Profile = testFile.readJson()

        assertEquals(profile, restored)
        assertTrue(testFile.readText().contains("\n"))
    }

    @Test
    fun `writeJson and readJson round trip serializer values`() {
        val user = User(2, "Bob", "bob@example.com", active = false)

        testFile.writeJson(User.serializer(), user) {
            prettyPrint = true
        }
        val restored = testFile.readJson(User.serializer())

        assertEquals(user, restored)
        assertTrue(testFile.readText().contains("\n"))
    }

    @Test
    fun `readJson serializer overload uses builder action`() {
        testFile.writeText("""{"id":3,"name":"Cindy","unknown":true}""")

        val restored = testFile.readJson(User.serializer()) {
            ignoreUnknownKeys = true
        }

        assertEquals(User(3, "Cindy"), restored)
    }

    @Test
    fun `modifyJson updates fields and keeps original unchanged`() {
        val original = User(1, "Alice", "old@example.com")
        val updated = original.modifyJson(
            mapOf(
                "name" to JsonPrimitive("Alicia"),
                "email" to JsonNull,
                "active" to JsonPrimitive(false)
            )
        )

        assertEquals(User(1, "Alicia", null, active = false), updated)
        assertEquals(User(1, "Alice", "old@example.com"), original)
    }

    @Test
    fun `modifyJson updates nested and list fields`() {
        val profile = Profile(
            user = User(1, "Alice"),
            tags = listOf("old")
        )
        val updated = profile.modifyJson(
            mapOf(
                "user" to jsonObjectOf("id" to 2, "name" to "Bob"),
                "tags" to jsonArrayOf("new", "vip")
            )
        )

        assertEquals(Profile(User(2, "Bob"), listOf("new", "vip")), updated)
    }

    @Test
    fun `modifyJson ignores unknown fields by default`() {
        val updated = User(1, "Alice").modifyJson(
            mapOf(
                "name" to JsonPrimitive("Alicia"),
                "missing" to JsonPrimitive("ignored")
            )
        )

        assertEquals(User(1, "Alicia"), updated)
    }

    @Test
    fun `modifyJson serializer overload updates fields`() {
        val original = User(1, "Alice", "old@example.com")
        val updated = original.modifyJson(
            serializer = User.serializer(),
            updates = mapOf("email" to JsonPrimitive("new@example.com"))
        )

        assertEquals(User(1, "Alice", "new@example.com"), updated)
    }

    @Test
    fun `modifyJson rejects non object serialized values`() {
        assertFailsWith<IllegalArgumentException> {
            42.modifyJson(mapOf("value" to JsonPrimitive(43)))
        }
        assertFailsWith<IllegalArgumentException> {
            listOf("a", "b").modifyJson(
                serializer = ListSerializer(String.serializer()),
                updates = emptyMap(),
                json = Json
            )
        }
    }

    @Test
    fun `readJson fails for invalid content`() {
        testFile.writeText("invalid json")

        assertFailsWith<SerializationException> {
            testFile.readJson<User>()
        }
    }
}
