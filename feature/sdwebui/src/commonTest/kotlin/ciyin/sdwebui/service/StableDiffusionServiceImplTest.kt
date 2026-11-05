package ciyin.sdwebui.service

import ciyin.sdwebui.client.Client
import ciyin.sdwebui.payload.Text2ImagePayload
import ciyin.sdwebui.support.RecordingClient
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
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
        assertEquals("info-string", response.info.toString())
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

    /** 验证未计算哈希的模型与普通模型混合时，整份目录及其顺序均被保留。 */
    @Test
    fun get_models_should_preserve_models_with_null_hashes() = runTest {
        val client = RecordingClient().apply {
            enqueueSuccess(
                """
                [
                    {
                        "title": "hashed-before.safetensors [abc]",
                        "model_name": "hashed-before",
                        "hash": "abc",
                        "sha256": "sha-before",
                        "filename": "/models/hashed-before.safetensors",
                        "config": null
                    },
                    {
                        "title": "svd.safetensors",
                        "model_name": "svd",
                        "hash": null,
                        "sha256": null,
                        "filename": "/models/svd.safetensors",
                        "config": null
                    },
                    {
                        "title": "hashed-after.safetensors [def]",
                        "model_name": "hashed-after",
                        "hash": "def",
                        "sha256": "sha-after",
                        "filename": "/models/hashed-after.safetensors",
                        "config": null
                    }
                ]
                """.trimIndent(),
            )
        }

        val models = newService(client).getModels().getOrThrow()

        assertEquals(listOf("hashed-before", "svd", "hashed-after"), models.map { it.name })
        assertEquals("abc", models.first().hash)
        assertEquals("sha-before", models.first().sha256)
        assertEquals("svd.safetensors", models[1].title)
        assertEquals("/models/svd.safetensors", models[1].filename)
        assertNull(models[1].hash)
        assertNull(models[1].sha256)
        assertNull(models[1].config)
        assertEquals("def", models.last().hash)
        assertEquals("sha-after", models.last().sha256)
    }

    /** 验证只有短哈希为空时，已提供的完整哈希不被丢弃。 */
    @Test
    fun get_models_should_accept_null_hash_with_present_sha256() = runTest {
        val client = RecordingClient().apply {
            enqueueSuccess(
                """
                [{
                    "title": "checkpoint.safetensors",
                    "model_name": "checkpoint",
                    "hash": null,
                    "sha256": "known-sha256",
                    "filename": "/models/checkpoint.safetensors",
                    "config": null
                }]
                """.trimIndent(),
            )
        }

        val model = newService(client).getModels().getOrThrow().single()

        assertNull(model.hash)
        assertEquals("known-sha256", model.sha256)
    }

    /** 验证只有完整哈希为空时，已提供的短哈希不被丢弃。 */
    @Test
    fun get_models_should_accept_null_sha256_with_present_hash() = runTest {
        val client = RecordingClient().apply {
            enqueueSuccess(
                """
                [{
                    "title": "checkpoint.safetensors [abc]",
                    "model_name": "checkpoint",
                    "hash": "abc",
                    "sha256": null,
                    "filename": "/models/checkpoint.safetensors",
                    "config": null
                }]
                """.trimIndent(),
            )
        }

        val model = newService(client).getModels().getOrThrow().single()

        assertEquals("abc", model.hash)
        assertNull(model.sha256)
    }

    /** 验证未提供可选哈希字段的响应也能保留模型，并明确表示哈希未知。 */
    @Test
    fun get_models_should_accept_missing_hash_fields() = runTest {
        val client = RecordingClient().apply {
            enqueueSuccess(
                """
                [{
                    "title": "checkpoint.safetensors",
                    "model_name": "checkpoint",
                    "filename": "/models/checkpoint.safetensors",
                    "config": null
                }]
                """.trimIndent(),
            )
        }

        val model = newService(client).getModels().getOrThrow().single()

        assertEquals("checkpoint", model.name)
        assertNull(model.hash)
        assertNull(model.sha256)
    }

    /** 验证模型身份字段仍严格解析，不把非法响应当成成功或空目录。 */
    @Test
    fun get_models_should_fail_when_required_model_name_is_null() = runTest {
        val client = RecordingClient().apply {
            enqueueSuccess(
                """
                [{
                    "title": "checkpoint.safetensors",
                    "model_name": null,
                    "hash": null,
                    "sha256": null,
                    "filename": "/models/checkpoint.safetensors",
                    "config": null
                }]
                """.trimIndent(),
            )
        }

        val result = newService(client).getModels()

        assertTrue(result.isFailure)
        assertIs<SerializationException>(result.exceptionOrNull())
    }

    /** 验证 LoRA 查询会访问 `sdapi/v1/loras` 并把 metadata 保留为 JSON 字符串。 */
    @Test
    fun get_loras_should_issue_get_to_loras_endpoint() = runTest {
        val client = RecordingClient().apply {
            enqueueSuccess(loraResponseJson())
        }
        val service = newService(client)

        val result = service.getLoras()

        val request = client.requests.single()
        assertEquals("sdapi/v1/loras", request.path)
        assertEquals(Client.Method.GET, request.method)
        assertNull(request.body)

        val lora = assertNotNull(result.getOrNull()).single()
        assertEquals("anima_kazutake_epoch70", lora.name)
        assertEquals("anima_kazutake_epoch70", lora.alias)
        assertEquals(
            "F:\\LiblibAI-workspace\\Models\\loras\\anima_kazutake_epoch70.safetensors",
            lora.path
        )

        val metadata = Json.parseToJsonElement(lora.metadata).jsonObject
        assertEquals("64", metadata.getValue("ss_network_alpha").jsonPrimitive.content)
        val args = metadata.getValue("ss_network_args").jsonObject
        assertEquals("4", args.getValue("factor").jsonPrimitive.content)
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

        val body =
            assertIs<Map<*, *>>(request.body, "setModel 应以 Map 形式传递 sd_model_checkpoint")
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

    /** 构造 LoRA 列表接口的代表性响应。 */
    private fun loraResponseJson(): String = """
        [
            {
                "name": "anima_kazutake_epoch70",
                "alias": "anima_kazutake_epoch70",
                "path": "F:\\LiblibAI-workspace\\Models\\loras\\anima_kazutake_epoch70.safetensors",
                "metadata": {
                    "ss_network_alpha": "64",
                    "ss_network_module": "lycoris.kohya",
                    "ss_network_args": {
                        "algo": "lokr",
                        "factor": 4
                    },
                    "ss_network_dim": "64"
                }
            }
        ]
    """.trimIndent()

    /** 构造代表性的文生图请求体，供 Service 请求契约测试复用。 */
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
        hrScale = 1f,
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
