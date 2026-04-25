package ciyin.sdwebui.client

import ciyin.sdwebui.client.Client.Companion.body
import ciyin.sdwebui.client.Client.Companion.load
import io.ktor.util.reflect.typeInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * [Client] 抽象层中纯逻辑部分的单元测试，
 * 不依赖任何真实 HTTP 引擎。
 */
class ClientTest {

    /**
     * 用于校验 [Client.Companion.body] 与 JSON 编解码的最小可序列化模型。
     */
    @Serializable
    private data class SamplePayload(
        @SerialName("name") val name: String,
        @SerialName("count") val count: Int,
    )

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }

    @Test
    fun request_builder_build_should_carry_all_fields() {
        val request = Client.RequestBuilder()
            .baseUrl("http://127.0.0.1:7860")
            .path("sdapi/v1/txt2img")
            .method(Client.Method.POST)
            .build()

        assertEquals("http://127.0.0.1:7860", request.baseUrl)
        assertEquals("sdapi/v1/txt2img", request.path)
        assertEquals(Client.Method.POST, request.method)
        assertNull(request.body)
        assertNull(request.bodyType)
    }

    @Test
    fun request_builder_default_method_should_be_get() {
        val request = Client.RequestBuilder().build()

        assertEquals(Client.Method.GET, request.method)
        assertEquals("", request.baseUrl)
        assertEquals("", request.path)
    }

    @Test
    fun request_builder_body_should_attach_value_and_type_info() {
        val payload = SamplePayload(name = "demo", count = 3)

        val request = Client.RequestBuilder()
            .body(payload)
            .build()

        assertEquals(payload, request.body)
        val bodyType = assertNotNull(request.bodyType, "bodyType 必须由 body() 扩展自动填充")
        assertEquals(typeInfo<SamplePayload>().type, bodyType.type)
    }

    @Test
    fun response_load_unit_should_succeed_even_when_body_is_blank() {
        val response = Client.Response(isSuccess = true, body = "")

        val result = response.load<Unit>(json)

        assertTrue(result.isSuccess)
        assertEquals(Unit, result.getOrNull())
    }

    @Test
    fun response_load_unit_should_succeed_even_when_response_is_failure() {
        val response = Client.Response(isSuccess = false, body = "ignored")

        val result = response.load<Unit>(json)

        assertTrue(result.isSuccess, "Unit 类型应优先于 isSuccess 直接返回成功")
    }

    @Test
    fun response_load_should_return_failure_with_client_error_when_not_success() {
        val response = Client.Response(isSuccess = false, body = "internal-error")

        val result = response.load<SamplePayload>(json)

        assertTrue(result.isFailure)
        val error = assertIs<Client.Error>(result.exceptionOrNull(), "失败结果应包装为 Client.Error")
        assertEquals("internal-error", error.body)
        assertEquals("internal-error", error.message)
    }

    @Test
    fun response_load_should_decode_payload_with_serial_names() {
        val response = Client.Response(
            isSuccess = true,
            body = """{"name":"demo","count":42}""",
        )

        val result = response.load<SamplePayload>(json)

        val payload = assertNotNull(result.getOrNull())
        assertEquals(SamplePayload(name = "demo", count = 42), payload)
    }

    @Test
    fun response_load_should_wrap_decode_exception_as_failure() {
        val response = Client.Response(
            isSuccess = true,
            body = "not-a-json",
        )

        val result = response.load<SamplePayload>(json)

        assertTrue(result.isFailure)
        assertFalse(result.exceptionOrNull() is Client.Error, "解析异常不应被错误地标记为 Client.Error")
    }
}
