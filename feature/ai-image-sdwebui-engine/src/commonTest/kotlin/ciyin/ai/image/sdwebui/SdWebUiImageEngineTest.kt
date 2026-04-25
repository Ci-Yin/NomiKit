package ciyin.ai.image.sdwebui

import ciyin.ai.core.image.ImageControl
import ciyin.ai.core.image.ImageEvent
import ciyin.ai.core.image.ImagePostProcessor
import ciyin.ai.core.image.ImageRequest
import ciyin.ai.core.image.ImageSource
import ciyin.ai.image.sdwebui.model.SdWebUiImageVendorOptionKeys
import ciyin.ai.image.sdwebui.model.SdWebUiText2ImageExtras
import ciyin.ai.image.sdwebui.model.SdWebUiText2ImageHiresFix
import ciyin.ai.image.sdwebui.support.RecordingClient
import ciyin.sdwebui.SdWebUi
import ciyin.sdwebui.client.Client
import ciyin.sdwebui.payload.Image2ImagePayload
import ciyin.sdwebui.payload.Text2ImagePayload
import ciyin.sdwebui.payload.script.ScriptPayload
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * [SdWebUiImageEngine] 的单元测试。
 */
class SdWebUiImageEngineTest {

    /**
     * 文生图请求应映射到 `txt2img`，并把 ControlNet / ADetailer / ReActor 写入 alwayson scripts。
     */
    @Test
    fun `generate 文生图应映射 txt2img 与前置脚本`() = runBlocking {
        val client = RecordingClient().apply {
            progressStub = Client.Response(isSuccess = true, body = minimalProgressJson())
            enqueueSuccess(
                """
                {"images":["${png("generated")}"],"info":"ok"}
                """.trimIndent(),
            )
        }
        val engine = engine(client)

        val events = engine.generate(
            ImageRequest(
                prompt = "a cat",
                controls = listOf(
                    ImageControl.ControlNet(
                        module = "openpose",
                        model = "control_v11p_sd15_openpose",
                        image = "pose".encodeToByteArray(),
                    ),
                ),
                postProcessors = listOf(
                    ImagePostProcessor.FaceDetailer(model = "face_yolov8n.pt"),
                    ImagePostProcessor.FaceSwap(sourceFace = "face".encodeToByteArray()),
                ),
            ),
        ).toList()

        assertEquals(1, client.requests.count { it.path == "sdapi/v1/txt2img" })
        val txt2imgReq = client.requests.first { it.path == "sdapi/v1/txt2img" }
        val payload = assertIs<Text2ImagePayload>(txt2imgReq.body)
        assertEquals("a cat", payload.prompt)
        assertTrue(payload.alwaysonScripts.containsKey("ControlNet"))
        assertTrue(payload.alwaysonScripts.containsKey("ADetailer"))
        assertTrue(payload.alwaysonScripts.containsKey("reactor"))
        assertIs<ScriptPayload.Multiple>(payload.alwaysonScripts.getValue("ControlNet"))
        assertIs<ScriptPayload.Multiple>(payload.alwaysonScripts.getValue("ADetailer"))
        assertIs<ScriptPayload.Array>(payload.alwaysonScripts.getValue("reactor"))
        assertIs<ImageEvent.Started>(events.first())
        assertIs<ImageEvent.Completed>(events.last())
        assertTrue(events.any { it is ImageEvent.Progress })
    }

    /**
     * [SdWebUiImageVendorOptionKeys.txt2imgExtras] 应在基础映射之后覆盖到 `txt2img` 请求体。
     */
    @Test
    fun `generate txt2img vendor extras 应覆盖采样器与 HR`() = runBlocking {
        val client = RecordingClient().apply {
            progressStub = Client.Response(isSuccess = true, body = minimalProgressJson())
            enqueueSuccess("""{"images":["${png("x")}"],"info":"ok"}""")
        }
        val engine = engine(client)
        val vendorJson = Json { ignoreUnknownKeys = true }
        val extrasEl = vendorJson.encodeToJsonElement(
            SdWebUiText2ImageExtras.serializer(),
            SdWebUiText2ImageExtras(
                samplerName = "Euler a",
                hiresFix = SdWebUiText2ImageHiresFix(enable = true, scale = 2),
            ),
        )

        engine.generate(
            ImageRequest(
                prompt = "cat",
                vendorOptions = mapOf(SdWebUiImageVendorOptionKeys.txt2imgExtras to extrasEl),
            ),
        ).toList()

        val payload = assertIs<Text2ImagePayload>(
            client.requests.first { it.path == "sdapi/v1/txt2img" }.body,
        )
        assertEquals("Euler a", payload.samplerName)
        assertEquals("Euler a", payload.samplerIndex)
        assertEquals(true, payload.enableHr)
        assertEquals(2, payload.hrScale)
    }

    /**
     * 顶层扁平的 `enable_hr` / `hr_scale` 等应在解码时并入 [SdWebUiText2ImageExtras] 的 `hi_res`。
     */
    @Test
    fun `generate txt2img vendor extras 接受顶层扁平 hi_res 字段`() = runBlocking {
        val client = RecordingClient().apply {
            progressStub = Client.Response(isSuccess = true, body = minimalProgressJson())
            enqueueSuccess("""{"images":["${png("y")}"],"info":"ok"}""")
        }
        val engine = engine(client)
        val extrasEl = Json.parseToJsonElement(
            """{"sampler_name":"DPM++ 2M","enable_hr":true,"hr_scale":3}""",
        )

        engine.generate(
            ImageRequest(
                prompt = "dog",
                vendorOptions = mapOf(SdWebUiImageVendorOptionKeys.txt2imgExtras to extrasEl),
            ),
        ).toList()

        val payload = assertIs<Text2ImagePayload>(
            client.requests.first { it.path == "sdapi/v1/txt2img" }.body,
        )
        assertEquals("DPM++ 2M", payload.samplerName)
        assertEquals(true, payload.enableHr)
        assertEquals(3, payload.hrScale)
    }

    /**
     * 图生图 / 重绘请求应先切模型（若指定了 model），再走 `img2img`，并把 mask 正确下发。
     */
    @Test
    fun `generate 重绘应先设置模型再调用 img2img`() = runBlocking {
        val client = RecordingClient().apply {
            progressStub = Client.Response(isSuccess = true, body = minimalProgressJson())
            enqueueSuccess()
            enqueueSuccess(
                """
                {"images":["${png("inpaint")}"],"info":"done"}
                """.trimIndent(),
            )
        }
        val engine = engine(client)

        engine.generate(
            ImageRequest(
                model = "sdxl",
                prompt = "repair face",
                source = ImageSource.Inpainting(
                    sourceImage = "source".encodeToByteArray(),
                    mask = "mask".encodeToByteArray(),
                ),
            ),
        ).toList()

        val nonProgress = client.requests.filter { it.path != "sdapi/v1/progress" }
        assertEquals(2, nonProgress.size)
        assertEquals("sdapi/v1/options", nonProgress[0].path)
        assertEquals("sdapi/v1/img2img", nonProgress[1].path)
        val payload = assertIs<Image2ImagePayload>(nonProgress[1].body)
        assertEquals("repair face", payload.prompt)
        assertEquals(1, payload.initImages.size)
        assertNotNull(payload.mask)
    }

    /**
     * 生成后处理中的 RemBG 与 Upscale 应触发额外的二次请求，并按声明顺序串行执行。
     */
    @Test
    fun `generate 生成后处理应串行调用 rembg 与 extra single image`() = runBlocking {
        val client = RecordingClient().apply {
            progressStub = Client.Response(isSuccess = true, body = minimalProgressJson())
            enqueueSuccess("""{"images":["${png("raw")}"],"info":"raw"}""")
            enqueueSuccess("""{"image":"${png("rembg")}"}""")
            enqueueSuccess("""{"html_info":"ok","image":"${png("upscaled")}"}""")
        }
        val engine = engine(client)

        val events = engine.generate(
            ImageRequest(
                prompt = "portrait",
                postProcessors = listOf(
                    ImagePostProcessor.BackgroundRemoval,
                    ImagePostProcessor.Upscale(factor = 2f, model = "R-ESRGAN 4x+"),
                ),
            ),
        ).toList()

        assertEquals(
            listOf(
                "sdapi/v1/txt2img",
                "rembg",
                "sdapi/v1/extra-single-image",
            ),
            client.requests.map { it.path }.filter { it != "sdapi/v1/progress" },
        )
        val completed = assertIs<ImageEvent.Completed>(events.last())
        assertEquals("upscaled", completed.result.images.single().bytes.decodeToString())
    }

    /**
     * 模型列表应映射成带引擎 ID 的通用模型信息。
     */
    @Test
    fun `listModels 应映射为 ImageModelInfo`() = runBlocking {
        val client = RecordingClient().apply {
            enqueueSuccess(
                """
                [
                  {
                    "title":"sd_xl_base_1.0",
                    "model_name":"SDXL Base",
                    "hash":"hash",
                    "sha256":"sha",
                    "filename":"model.safetensors",
                    "config":null
                  }
                ]
                """.trimIndent(),
            )
        }
        val engine = engine(client)

        val models = engine.listModels().getOrThrow()

        assertEquals(1, client.requests.size)
        assertEquals("sdapi/v1/sd-models", client.lastRequest.path)
        assertEquals(1, models.size)
        assertEquals(engine.id, models.single().engineId)
        assertEquals("sd_xl_base_1.0", models.single().model)
        assertEquals("SDXL Base", models.single().displayName)
    }

    /**
     * 不支持的 control 应在 validate 阶段被提前拒绝。
     */
    @Test
    fun `validate 遇到 IPAdapter 应返回失败`() = runBlocking {
        val engine = engine(RecordingClient())

        val result = engine.validate(
            ImageRequest(
                prompt = "cat",
                controls = listOf(
                    ImageControl.IPAdapter(
                        image = "ref".encodeToByteArray(),
                    ),
                ),
            ),
        )

        assertTrue(result.isFailure)
    }

    /**
     * 构造一个绑定录制客户端的测试引擎。
     */
    private fun engine(client: RecordingClient): SdWebUiImageEngine = SdWebUiImageEngine(
        id = ciyin.ai.core.engine.EngineId("sdwebui:test"),
        sdWebUi = SdWebUi.Builder()
            .client(client)
            .build(),
    )
}

@OptIn(ExperimentalEncodingApi::class)
private fun png(text: String): String = Base64.Default.encode(text.encodeToByteArray())

/** 与 [ciyin.sdwebui.response.ProgressResponse] 字段对齐的最小合法 JSON，供进度轮询桩使用。 */
private fun minimalProgressJson(): String = """
    {
      "progress": 0.25,
      "eta_relative": 0.0,
      "state": {
        "skipped": false,
        "interrupted": false,
        "job": "",
        "job_count": 0,
        "job_timestamp": "0",
        "job_no": 0,
        "sampling_step": 5,
        "sampling_steps": 20
      },
      "current_image": null,
      "textinfo": null
    }
""".trimIndent()
