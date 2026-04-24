package ciyin.sdwebui.service

import ciyin.sdwebui.client.Client
import ciyin.sdwebui.payload.Text2ImagePayload
import ciyin.sdwebui.support.RecordingClient
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * [StableDiffusionServiceImpl] 的单元测试：
 * 选取 GET / POST / 返回 Unit / 携带 Map body 等代表性方法，
 * 验证 path / method / body 与 AUTOMATIC1111 官方 API 路径一致。
 */
class StableDiffusionServiceImplTest {

    private val baseUrl = "http://127.0.0.1:7860"
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }

    private fun newService(client: RecordingClient): StableDiffusionServiceImpl =
        StableDiffusionServiceImpl(baseUrl, client, json)

    @Test
    fun text2_image_should_post_payload_to_txt2img_endpoint() = runTest {
        val payload = samplePayload(prompt = "a cat")
        val client = RecordingClient().apply {
            enqueueSuccess("""{"images":["base64-image"],"info":"info-string"}""")
        }
        val service = newService(client)

        val result = service.text2Image(payload)

        val request = client.requests.single()
        assertEquals(baseUrl, request.baseUrl)
        assertEquals("sdapi/v1/txt2img", request.path)
        assertEquals(Client.Method.POST, request.method)
        assertSame(payload, request.body, "POST 请求体必须是传入的 payload 实例本身")
        assertNotNull(request.bodyType, "POST 请求必须携带 bodyType 用于序列化")

        val response = assertNotNull(result.getOrNull())
        assertEquals(listOf("base64-image"), response.images)
        assertEquals("info-string", response.info)
    }

    @Test
    fun get_models_should_issue_get_to_sd_models_endpoint() = runTest {
        val client = RecordingClient().apply {
            enqueueSuccess(
                """
                [
                    {
                        "title": "v1-5-pruned",
                        "model_name": "v1-5-pruned",
                        "hash": "abc",
                        "sha256": "deadbeef",
                        "filename": "v1-5-pruned.safetensors",
                        "config": null
                    }
                ]
                """.trimIndent()
            )
        }
        val service = newService(client)

        val result = service.getModels()

        val request = client.requests.single()
        assertEquals("sdapi/v1/sd-models", request.path)
        assertEquals(Client.Method.GET, request.method)
        assertNull(request.body)

        val models = assertNotNull(result.getOrNull())
        assertEquals(1, models.size)
        assertEquals("v1-5-pruned", models.single().name)
    }

    @Test
    fun set_model_should_post_options_endpoint_with_checkpoint_map() = runTest {
        val client = RecordingClient().apply {
            enqueueSuccess()
        }
        val service = newService(client)

        val result = service.setModel("v1-5-pruned")

        val request = client.requests.single()
        assertEquals("sdapi/v1/options", request.path)
        assertEquals(Client.Method.POST, request.method)

        val body = assertIs<Map<*, *>>(request.body, "setModel 应以 Map 形式传递 sd_model_checkpoint")
        assertEquals("v1-5-pruned", body["sd_model_checkpoint"])

        assertTrue(result.isSuccess)
    }

    @Test
    fun refresh_checkpoints_should_post_without_body_and_succeed_for_unit_response() = runTest {
        val client = RecordingClient().apply {
            enqueueSuccess()
        }
        val service = newService(client)

        val result = service.refreshCheckpoints()

        val request = client.requests.single()
        assertEquals("sdapi/v1/refresh-checkpoints", request.path)
        assertEquals(Client.Method.POST, request.method)
        assertNull(request.body, "refreshCheckpoints 不应携带请求体")
        assertNull(request.bodyType)

        assertTrue(result.isSuccess)
    }

    @Test
    fun refresh_checkpoints_should_succeed_for_unit_even_when_response_is_failure() = runTest {
        val client = RecordingClient().apply {
            enqueueFailure("ignored")
        }
        val service = newService(client)

        val result = service.refreshCheckpoints()

        assertTrue(
            result.isSuccess,
            "Result<Unit> 在 Response.load 中走快速通道，不会受 isSuccess 影响",
        )
    }

    private fun samplePayload(prompt: String): Text2ImagePayload = Text2ImagePayload(
        prompt = prompt,
        negativePrompt = "",
        styles = emptyList(),
        seed = -1,
        subseed = -1,
        subseedStrength = 0,
        seedResizeFromH = 0,
        seedResizeFromW = 0,
        samplerName = "Euler a",
        batchSize = 1,
        nIter = 1,
        steps = 20,
        cfgScale = 7f,
        width = 512,
        height = 512,
        restoreFaces = false,
        tiling = false,
        doNotSaveSamples = false,
        doNotSaveGrid = false,
        eta = 0f,
        denoisingStrength = 0f,
        sChurn = 0,
        sTmax = 0,
        sTmin = 0,
        sNoise = 0,
        overrideSettings = emptyMap(),
        overrideSettingsRestoreAfterwards = false,
        comments = emptyMap(),
        enableHr = false,
        firstphaseWidth = 0,
        firstphaseHeight = 0,
        hrScale = 1,
        hrUpscaler = "",
        hrSecondPassSteps = 0,
        hrResizeX = 0,
        hrResizeY = 0,
        samplerIndex = "Euler a",
        scriptName = null,
        scriptArgs = emptyList(),
        sendImages = true,
        saveImages = false,
        alwaysonScripts = emptyMap(),
    )
}
