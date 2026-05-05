package ciyin.ai.core.registry

import ciyin.ai.core.capability.ImageCapability
import ciyin.ai.core.engine.EngineId
import ciyin.ai.core.error.UnsupportedCapabilityException
import ciyin.ai.core.support.FakeImageEngine
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class ImageEngineSelectorTest {

    private val sdLocal = FakeImageEngine(
        id = EngineId("sdwebui:local-7860"),
        provider = "sdwebui",
        capabilities = setOf(ImageCapability.TextToImage, ImageCapability.ControlNet),
    )

    private fun selectorWith(
        images: List<FakeImageEngine> = listOf(sdLocal),
    ) = ImageEngineSelector(registry = DefaultImageEngineRegistry(images))

    @Test
    fun `select 偏好命中且满足能力 优先返回偏好`() {
        val selector = selectorWith()

        val engine = selector.select(
            preferredId = EngineId("sdwebui:local-7860"),
            required = setOf(ImageCapability.ControlNet),
        )
        assertSame(sdLocal, engine)
    }

    @Test
    fun `select 没有任何引擎满足要求 抛 UnsupportedCapability`() {
        val selector = selectorWith()

        assertFailsWith<UnsupportedCapabilityException> {
            selector.select(required = setOf(ImageCapability.FaceSwap))
        }
    }
}
