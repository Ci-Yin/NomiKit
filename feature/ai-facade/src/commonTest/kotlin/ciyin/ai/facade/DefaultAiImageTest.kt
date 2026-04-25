package ciyin.ai.facade

import ciyin.ai.core.capability.ImageCapability
import ciyin.ai.core.engine.EngineId
import ciyin.ai.core.image.ImageEvent
import ciyin.ai.core.image.ImageRequest
import ciyin.ai.core.image.ImageResult
import ciyin.ai.core.registry.DefaultChatEngineRegistry
import ciyin.ai.core.registry.DefaultImageEngineRegistry
import ciyin.ai.core.registry.EngineSelector
import ciyin.ai.facade.selection.FallbackPolicy
import ciyin.ai.facade.selection.ImageModelSpec
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
            spec = ImageModelSpec.Explicit(engine.id, model = "sdxl"),
            request = ImageRequest(prompt = "cat"),
        ).toList()

        assertEquals(1, engine.receivedRequests.size)
        assertEquals("sdxl", engine.receivedRequests.single().model)
    }

    /**
     * 构造一套仅包含生图引擎的选择器。
     */
    private fun selector(
        images: List<RecordingImageEngine>,
    ): EngineSelector = EngineSelector(
        chatRegistry = DefaultChatEngineRegistry(emptyList()),
        imageRegistry = DefaultImageEngineRegistry(images),
    )
}
