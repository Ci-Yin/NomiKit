package ciyin.ai.image.sdwebui

import ciyin.ai.core.engine.EngineId
import ciyin.ai.core.error.AiEngineError
import ciyin.ai.core.error.AiEngineException
import ciyin.ai.core.image.ImageControl
import ciyin.ai.core.image.ImageEvent
import ciyin.ai.core.image.ImagePostProcessor
import ciyin.ai.core.image.ImageRequest
import ciyin.ai.core.image.ImageSource
import ciyin.ai.image.sdwebui.mapper.AiEngineErrorException
import ciyin.ai.image.sdwebui.mapper.toAiEngineError
import ciyin.ai.image.sdwebui.model.SdWebUiImageVendorOptionKeys
import ciyin.ai.image.sdwebui.model.SdWebUiText2ImageExtras
import ciyin.ai.image.sdwebui.model.SdWebUiText2ImageHiresFix
import ciyin.ai.image.sdwebui.support.RecordingClient
import ciyin.sdwebui.SdWebUi
import ciyin.sdwebui.client.Client
import ciyin.sdwebui.payload.Image2ImagePayload
import ciyin.sdwebui.payload.Text2ImagePayload
import ciyin.sdwebui.payload.script.ScriptPayload
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertSame
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
                hiresFix = SdWebUiText2ImageHiresFix(enable = true, scale = 2f),
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
        assertEquals(2f, payload.hrScale)
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
        assertEquals(3f, payload.hrScale)
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
    fun `models 应映射为 ImageModelInfo`() = runBlocking {
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

        val models = engine.models()

        assertEquals(1, client.requests.size)
        assertEquals("sdapi/v1/sd-models", client.lastRequest.path)
        assertEquals(1, models.size)
        assertEquals(engine.id, models.single().engineId)
        assertEquals("sd_xl_base_1.0", models.single().model)
        assertEquals("SDXL Base", models.single().displayName)
    }

    /**
     * 上游真实返回空数组时应成功返回空列表，而不是把空响应当作错误。
     */
    @Test
    fun `models 成功空列表应正常返回`() = runBlocking {
        val client = RecordingClient().apply {
            enqueueSuccess("[]")
        }

        val models = engine(client).models()

        assertTrue(models.isEmpty())
        assertEquals(1, client.requests.size)
        assertEquals("sdapi/v1/sd-models", client.lastRequest.path)
    }

    /**
     * 非 2xx 响应产生的 SDK Result 失败应携带协议错误上抛，不能退化成成功空列表。
     */
    @Test
    fun `models 非成功响应应抛出公共协议异常`() = runBlocking {
        val client = RecordingClient().apply {
            enqueueFailure("model service unavailable")
        }

        val exception = assertFailsWith<AiEngineException> {
            engine(client).models()
        }

        val error = assertIs<AiEngineError.Protocol>(exception.error)
        assertEquals("model service unavailable", error.message)
        assertEquals("model service unavailable", assertIs<Client.Error>(error.cause).body)
        assertEquals(1, client.requests.size)
        assertEquals("sdapi/v1/sd-models", client.lastRequest.path)
    }

    /**
     * 成功状态下的无效 JSON、错误根类型和缺字段都应保留解析异常并归为协议错误。
     */
    @Test
    fun `models 无效 JSON 应抛出公共协议异常`() = runBlocking {
        listOf("not-json", "{}", "[{}]").forEach { body ->
            val client = RecordingClient().apply {
                enqueueSuccess(body)
            }

            val exception = assertFailsWith<AiEngineException> {
                engine(client).models()
            }

            val error = assertIs<AiEngineError.Protocol>(exception.error)
            val cause = assertIs<SerializationException>(error.cause)
            assertEquals(cause.message, error.message)
            assertEquals(1, client.requests.size)
            assertEquals("sdapi/v1/sd-models", client.lastRequest.path)
        }
    }

    /**
     * 请求直接抛出的 HTTP 错误应与 SDK Result 失败走相同协议错误映射。
     */
    @Test
    fun `models 请求直接抛出 HTTP 错误应类型化`() = runBlocking {
        val cause = Client.Error("direct HTTP failure")
        val client = RecordingClient().apply {
            enqueueException(cause)
        }

        val exception = assertFailsWith<AiEngineException> {
            engine(client).models()
        }

        val error = assertIs<AiEngineError.Protocol>(exception.error)
        assertEquals(cause.body, error.message)
        assertSame(cause, error.cause)
        assertEquals(cause.toAiEngineError(), error)
    }

    /**
     * 请求阶段直接抛出的 IO 异常应转为网络错误，保留原始 cause。
     */
    @Test
    fun `models 传输 IO 异常应抛出公共网络异常`() = runBlocking {
        val cause = IOException("connection refused")
        val client = RecordingClient().apply {
            enqueueException(cause)
        }

        val exception = assertFailsWith<AiEngineException> {
            engine(client).models()
        }

        val error = assertIs<AiEngineError.Network>(exception.error)
        assertSame(cause, error.cause)
        assertEquals(cause.message, error.message)
    }

    /**
     * 未分类的请求异常仍须携带统一 Unknown 错误上抛，不能泄漏裸异常或返回空列表。
     */
    @Test
    fun `models 未知请求异常应抛出公共未知异常`() = runBlocking {
        val cause = IllegalStateException("transport failed")
        val client = RecordingClient().apply {
            enqueueException(cause)
        }

        val exception = assertFailsWith<AiEngineException> {
            engine(client).models()
        }

        val error = assertIs<AiEngineError.Unknown>(exception.error)
        assertSame(cause, error.cause)
        assertEquals(cause.message, error.message)
    }

    /**
     * 公共异常与内部 mapper 包装均须解包既有错误，不能再次映射为 Unknown。
     */
    @Test
    fun `models 已类型化异常应保留原错误`() = runBlocking {
        val error = AiEngineError.Protocol(message = "already mapped")
        listOf(AiEngineException(error), AiEngineErrorException(error)).forEach { cause ->
            val client = RecordingClient().apply {
                enqueueException(cause)
            }

            val exception = assertFailsWith<AiEngineException> {
                engine(client).models()
            }

            assertSame(error, exception.error)
        }
    }

    /**
     * 参数异常仍沿用 Unsupported 映射，不应被精确的 JSON 协议错误分支误分类。
     */
    @Test
    fun `models 请求参数异常仍应映射为不支持错误`() = runBlocking {
        val cause = IllegalArgumentException("invalid request")
        val client = RecordingClient().apply {
            enqueueException(cause)
        }

        val exception = assertFailsWith<AiEngineException> {
            engine(client).models()
        }

        assertEquals(AiEngineError.Unsupported(message = "invalid request"), exception.error)
    }

    /**
     * 请求取消必须传播同一个 CancellationException，不能包装或转换为普通失败。
     */
    @Test
    fun `models 取消异常应原样传播`() = runBlocking {
        val cancellation = CancellationException("cancel models")
        val client = RecordingClient().apply {
            enqueueException(cancellation)
        }

        val actual = assertFailsWith<CancellationException> {
            engine(client).models()
        }

        assertSame(cancellation, actual)
        assertSame(
            cancellation,
            assertFailsWith<CancellationException> { cancellation.toAiEngineError() })
        assertEquals(1, client.requests.size)
        assertEquals("sdapi/v1/sd-models", client.lastRequest.path)
    }

    /**
     * LoRA 列表应直接映射为 SD WebUI 引擎专属模型。
     */
    @Test
    fun `loras 应返回 LoRA 列表`() = runBlocking {
        val client = RecordingClient().apply {
            enqueueSuccess(loraResponseJson())
        }
        val engine = engine(client)

        val loras = engine.loras()

        assertEquals("sdapi/v1/loras", client.lastRequest.path)
        val lora = loras.single()
        assertEquals("anima_kazutake_epoch70", lora.name)
        assertEquals("anima_kazutake_epoch70", lora.alias)
        assertEquals(
            "F:\\LiblibAI-workspace\\Models\\loras\\anima_kazutake_epoch70.safetensors",
            lora.path
        )
        val metadata = Json.parseToJsonElement(lora.metadata).jsonObject
        assertEquals("64", metadata.getValue("ss_network_dim").jsonPrimitive.content)
        val args = metadata.getValue("ss_network_args").jsonObject
        assertEquals("4", args.getValue("factor").jsonPrimitive.content)
    }

    /**
     * LoRA 请求失败时应把底层异常抛给调用方。
     */
    @Test
    fun `loras 失败时应抛出异常`() = runBlocking {
        val client = RecordingClient().apply {
            enqueueFailure("boom")
        }
        val engine = engine(client)

        assertFailsWith<Client.Error> {
            engine.loras()
        }
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
        id = EngineId("sdwebui:test"),
        sdWebUi = SdWebUi.Builder()
            .client(client)
            .build(),
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
