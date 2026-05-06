package ciyin.ai.integrate.image

import ciyin.ai.core.capability.ImageCapability
import ciyin.ai.core.engine.EngineId
import ciyin.ai.core.engine.EngineRuntime
import ciyin.ai.core.engine.ImageEngine
import ciyin.ai.core.error.UnsupportedCapabilityException
import ciyin.ai.core.image.GeneratedImage
import ciyin.ai.core.image.ImageEvent
import ciyin.ai.core.image.ImageModelInfo
import ciyin.ai.core.image.ImageRequest
import ciyin.ai.core.image.ImageResult
import ciyin.ai.facade.selection.ChatEngineSpec
import ciyin.ai.facade.selection.EnginePreferences
import ciyin.ai.facade.selection.FallbackPolicy
import ciyin.ai.facade.selection.ImageEngineSpec
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class AiImageIntegrateTest {

    @Test
    fun generate_without_engines_throws_unsupported_capability() = runTest {
        val integrate = testAiImageIntegrate(
            defaultEngineConfigs = emptyList(),
            preferences = NeutralEnginePreferences(),
            buildImageEngine = { unusedStubEngine() },
        )
        val ex = assertFailsWith<UnsupportedCapabilityException> {
            integrate.generate(ImageRequest(prompt = "test")).toList()
        }
        assertTrue(ex.message.orEmpty().isNotEmpty())
    }

    @Test
    fun models_without_registered_runtime_returns_empty() = runTest {
        val integrate = testAiImageIntegrate(
            defaultEngineConfigs = emptyList(),
            preferences = NeutralEnginePreferences(),
            buildImageEngine = { unusedStubEngine() },
        )
        assertContentEquals(emptyList(), integrate.models())
    }

    @Test
    fun merges_default_model_before_delegate_when_request_model_null() = runTest {
        val stub = recordingStubEngine(IntegrateImageEngineIds.sdWebUi)
        val integrate = testAiImageIntegrate(
            buildImageEngine = { stub },
        )
        integrate.engines(
            listOf(
                ImageEngineConfig.SdWebUi(
                    baseUrl = "http://127.0.0.1:7860",
                    apiKey = "",
                    defaultModel = "from-config",
                ),
            ),
        )
        integrate.generate(ImageRequest(prompt = "p", model = null)).toList()
        assertEquals("from-config", stub.receivedRequests.single().model)
    }

    @Test
    fun same_sealed_config_type_latter_entry_wins() = runTest {
        val builtConfigs = mutableListOf<ImageEngineConfig>()
        val integrate = testAiImageIntegrate(
            defaultEngineConfigs = emptyList(),
            buildImageEngine = { cfg ->
                builtConfigs += cfg
                recordingStubEngine(cfg.engineId)
            },
        )
        val first = ImageEngineConfig.SdWebUi(
            baseUrl = "http://127.0.0.1:1111",
            apiKey = "",
            defaultModel = null,
        )
        val second = ImageEngineConfig.SdWebUi(
            baseUrl = "http://127.0.0.1:2222",
            apiKey = "",
            defaultModel = null,
        )
        integrate.engines(listOf(first, second))
        assertEquals(1, builtConfigs.size)
        assertEquals("http://127.0.0.1:2222", builtConfigs.single().baseUrl)
    }

    @Test
    fun models_delegates_to_registered_engine() = runTest {
        val expected = listOf(
            ImageModelInfo(
                engineId = IntegrateImageEngineIds.sdWebUi,
                model = "stub-model",
            ),
        )
        val stub = recordingStubEngine(
            id = IntegrateImageEngineIds.sdWebUi,
            modelsResult = expected,
        )
        val integrate = testAiImageIntegrate(
            buildImageEngine = { stub },
        )
        integrate.engines(
            listOf(
                ImageEngineConfig.SdWebUi(
                    baseUrl = "http://127.0.0.1:7860",
                    apiKey = "",
                    defaultModel = null,
                ),
            ),
        )
        assertEquals(expected, integrate.models())
        assertEquals(
            expected,
            integrate.models(
                spec = ImageEngineSpec.Explicit(engineId = IntegrateImageEngineIds.sdWebUi),
            ),
        )
    }

    @Test
    fun generate_collects_facade_stream_without_network() = runTest {
        val stub = recordingStubEngine(IntegrateImageEngineIds.sdWebUi)
        val integrate = testAiImageIntegrate(
            buildImageEngine = { cfg ->
                assertEquals(IntegrateImageEngineIds.sdWebUi, cfg.engineId)
                stub
            },
        )
        integrate.engines(
            listOf(
                ImageEngineConfig.SdWebUi(
                    baseUrl = "http://127.0.0.1:7860",
                    apiKey = "",
                    defaultModel = null,
                ),
            ),
        )
        val terminal = integrate.generate(ImageRequest(prompt = "hello")).toList().last()
        assertTrue(terminal is ImageEvent.Completed || terminal is ImageEvent.Failed)
        assertEquals(1, stub.receivedRequests.size)
    }

    @Test
    fun engines_empty_list_uses_builtin_defaults() = runTest {
        val stub = recordingStubEngine(IntegrateImageEngineIds.sdWebUi)
        val integrate = testAiImageIntegrate(
            buildImageEngine = { cfg ->
                assertEquals("http://127.0.0.1:7860", cfg.baseUrl)
                stub
            },
        )
        integrate.engines(emptyList())
        integrate.generate(ImageRequest(prompt = "x")).toList()
        assertEquals(1, stub.receivedRequests.size)
    }

    @Test
    fun engines_overrides_builtin_sd_web_ui_base_url() = runTest {
        val builtUrls = mutableListOf<String>()
        val stub = recordingStubEngine(IntegrateImageEngineIds.sdWebUi)
        val integrate = testAiImageIntegrate(
            defaultEngineConfigs = emptyList(),
            buildImageEngine = { cfg ->
                builtUrls += (cfg as ImageEngineConfig.SdWebUi).baseUrl
                stub
            },
        )
        integrate.engines(
            listOf(
                ImageEngineConfig.SdWebUi(
                    baseUrl = "http://192.168.1.10:7860",
                    apiKey = "",
                    defaultModel = null,
                ),
            ),
        )
        assertEquals(
            listOf("http://192.168.1.10:7860"),
            builtUrls,
        )
    }
}

private fun testAiImageIntegrate(
    defaultEngineConfigs: List<ImageEngineConfig> = IntegrateImageDefaults.sdWebUiLocalhost(),
    preferences: EnginePreferences = IntegrateEnginePreferences(),
    buildImageEngine: (ImageEngineConfig) -> ImageEngine,
): AiImageIntegrate = AiImageIntegrate(
    defaultEngineConfigs = defaultEngineConfigs,
    preferences = preferences,
    buildImageEngine = buildImageEngine,
)

/**
 * 默认生图路由保持 [ImageEngineSpec.Default]，避免在「零引擎」快照下解析为固定 [EngineId] 导致 [models] 或路由抛错。
 */
private class NeutralEnginePreferences : EnginePreferences {

    override suspend fun defaultChatSpec(): ChatEngineSpec = ChatEngineSpec.Default

    override suspend fun defaultImageSpec(): ImageEngineSpec = ImageEngineSpec.Default

    override suspend fun chatFallback(): FallbackPolicy = FallbackPolicy()

    override suspend fun imageFallback(): FallbackPolicy = FallbackPolicy()
}

private fun unusedStubEngine(): ImageEngine =
    recordingStubEngine(IntegrateImageEngineIds.sdWebUi)

private fun recordingStubEngine(
    id: EngineId,
    modelsResult: List<ImageModelInfo> = emptyList(),
): RecordingStubImageEngine = RecordingStubImageEngine(
    id = id,
    modelsResult = modelsResult,
)

/**
 * 不发起真实 HTTP：记录 [ImageRequest]，并产出符合 Facade 调度契约的终结事件。
 */
private class RecordingStubImageEngine(
    override val id: EngineId,
    private val modelsResult: List<ImageModelInfo> = emptyList(),
) : ImageEngine {

    val receivedRequests = mutableListOf<ImageRequest>()

    override val provider: String = "stub"

    override val runtime: EngineRuntime = EngineRuntime.RemoteSelfHosted

    override val capabilities: Set<ImageCapability> = emptySet()

    override fun generate(request: ImageRequest) = flow {
        receivedRequests += request
        emit(ImageEvent.Started)
        emit(
            ImageEvent.Completed(
                result = ImageResult(
                    images = listOf(
                        GeneratedImage(
                            mimeType = "image/png",
                            bytes = byteArrayOf(1),
                        ),
                    ),
                ),
            ),
        )
    }

    override suspend fun models(): List<ImageModelInfo> = modelsResult

    override suspend fun validate(request: ImageRequest): Result<Unit> = Result.success(Unit)
}
