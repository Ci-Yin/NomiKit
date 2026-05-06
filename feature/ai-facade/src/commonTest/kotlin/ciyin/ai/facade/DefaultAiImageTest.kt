package ciyin.ai.facade

import ciyin.ai.core.capability.ImageCapability
import ciyin.ai.core.engine.EngineId
import ciyin.ai.core.image.ImageEvent
import ciyin.ai.core.image.ImageModelInfo
import ciyin.ai.core.image.ImageRequest
import ciyin.ai.core.image.ImageResult
import ciyin.ai.core.registry.DefaultImageEngineRegistry
import ciyin.ai.core.registry.ImageEngineSelector
import ciyin.ai.facade.selection.FallbackPolicy
import ciyin.ai.facade.selection.ImageEngineSpec
import ciyin.ai.facade.support.FakeEnginePreferences
import ciyin.ai.facade.support.RecordingImageEngine
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * [DefaultAiImage] 的基础单元测试。
 */
class DefaultAiImageTest {

    /**
     * 当业务显式指定模型时，应把模型名覆盖到发送给目标引擎的请求上。
     */
    @Test
    fun `Explicit 应把模型名下发给目标生图引擎`() = runBlocking {
        val engine = RecordingImageEngine(
            id = EngineId("image:sd"),
            capabilities = setOf(ImageCapability.TextToImage),
            plannedEvents = ArrayDeque(
                listOf(
                    listOf(
                        ImageEvent.Started,
                        ImageEvent.Completed(ImageResult(images = emptyList())),
                    ),
                ),
            ),
        )
        val aiImage = DefaultAiImage(
            selector = selector(images = listOf(engine)),
            preferences = FakeEnginePreferences(
                imageFallbackPolicy = FallbackPolicy(maxRetries = 0),
            ),
        )

        aiImage.generate(
            request = ImageRequest(prompt = "cat"),
            spec = ImageEngineSpec.Explicit(engine.id, model = "sdxl"),
        ).toList()

        assertEquals(1, engine.receivedRequests.size)
        assertEquals("sdxl", engine.receivedRequests.single().model)
    }

    @Test
    fun `Explicit spec 仅枚举目标引擎的模型`() = runBlocking {
        val idA = EngineId("image:a")
        val idB = EngineId("image:b")
        val infoA = ImageModelInfo(
            engineId = idA,
            model = "m-a",
        )
        val infoB = ImageModelInfo(
            engineId = idB,
            model = "m-b",
        )
        val engineA = RecordingImageEngine(
            id = idA,
            capabilities = setOf(ImageCapability.TextToImage),
            models = listOf(infoA),
        )
        val engineB = RecordingImageEngine(
            id = idB,
            capabilities = setOf(ImageCapability.TextToImage),
            models = listOf(infoB),
        )
        val aiImage = DefaultAiImage(
            selector = selector(
                images = listOf(engineA, engineB),
            ),
            preferences = FakeEnginePreferences(
                imageFallbackPolicy = FallbackPolicy(maxRetries = 0),
            ),
        )
        assertEquals(
            listOf(infoA),
            aiImage.models(
                spec = ImageEngineSpec.Explicit(engineId = idA),
            ),
        )
        assertEquals(
            2,
            aiImage.models(spec = ImageEngineSpec.Default).size,
        )
    }

    /**
     * 构造一套仅包含生图引擎的选择器。
     */
    private fun selector(
        images: List<RecordingImageEngine>,
    ): ImageEngineSelector = ImageEngineSelector(
        registry = DefaultImageEngineRegistry(images),
    )
}
