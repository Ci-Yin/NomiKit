package ciyin.ai.core.registry

import ciyin.ai.core.capability.ImageCapability
import ciyin.ai.core.engine.EngineId
import ciyin.ai.core.support.FakeImageEngine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DefaultImageEngineRegistryTest {

    private val sdLocal = FakeImageEngine(
        id = EngineId("sdwebui:local-7860"),
        provider = "sdwebui",
        capabilities = setOf(
            ImageCapability.TextToImage,
            ImageCapability.ImageToImage,
            ImageCapability.Inpainting,
            ImageCapability.ControlNet,
            ImageCapability.BackgroundRemoval,
        ),
    )
    private val sdRemote = FakeImageEngine(
        id = EngineId("sdwebui:remote-prod"),
        provider = "sdwebui",
        capabilities = setOf(
            ImageCapability.TextToImage,
            ImageCapability.ImageToImage,
            ImageCapability.ControlNet,
        ),
    )

    @Test
    fun `findByCapability 命中多个时按构造顺序返回`() {
        val registry = DefaultImageEngineRegistry(listOf(sdLocal, sdRemote))

        val controlNetCapable = registry.findByCapability(
            ImageCapability.TextToImage,
            ImageCapability.ControlNet,
        )

        assertEquals(listOf(sdLocal, sdRemote), controlNetCapable)
    }

    @Test
    fun `findByCapability 单家命中`() {
        val registry = DefaultImageEngineRegistry(listOf(sdLocal, sdRemote))

        val rembg = registry.findByCapability(ImageCapability.BackgroundRemoval)

        assertEquals(listOf(sdLocal), rembg)
    }

    @Test
    fun `重复 id 直接拒绝构造`() {
        val duplicate = FakeImageEngine(id = EngineId("sdwebui:local-7860"))

        assertFailsWith<IllegalArgumentException> {
            DefaultImageEngineRegistry(listOf(sdLocal, duplicate))
        }
    }
}
