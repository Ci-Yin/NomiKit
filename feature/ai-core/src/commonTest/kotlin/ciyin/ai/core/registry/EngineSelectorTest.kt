package ciyin.ai.core.registry

import ciyin.ai.core.capability.ChatCapability
import ciyin.ai.core.capability.ImageCapability
import ciyin.ai.core.engine.EngineId
import ciyin.ai.core.error.UnsupportedCapabilityException
import ciyin.ai.core.support.FakeChatEngine
import ciyin.ai.core.support.FakeImageEngine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class EngineSelectorTest {

    private val openai = FakeChatEngine(
        id = EngineId("openai:default"),
        provider = "openai",
        capabilities = setOf(ChatCapability.Streaming, ChatCapability.ToolCalling),
    )
    private val ollama = FakeChatEngine(
        id = EngineId("ollama:home"),
        provider = "ollama",
        capabilities = setOf(ChatCapability.Streaming),
    )
    private val anthropic = FakeChatEngine(
        id = EngineId("anthropic:default"),
        provider = "anthropic",
        capabilities = setOf(
            ChatCapability.Streaming,
            ChatCapability.ToolCalling,
            ChatCapability.PromptCaching,
        ),
    )

    private val sdLocal = FakeImageEngine(
        id = EngineId("sdwebui:local-7860"),
        provider = "sdwebui",
        capabilities = setOf(ImageCapability.TextToImage, ImageCapability.ControlNet),
    )

    private fun selectorWith(
        chats: List<FakeChatEngine> = listOf(openai, ollama, anthropic),
        images: List<FakeImageEngine> = listOf(sdLocal),
    ) = EngineSelector(
        chatRegistry = DefaultChatEngineRegistry(chats),
        imageRegistry = DefaultImageEngineRegistry(images),
    )

    @Test
    fun `selectChat 无偏好 无要求 返回首个`() {
        val selector = selectorWith()

        val engine = selector.selectChat()

        assertSame(openai, engine)
    }

    @Test
    fun `selectChat 偏好命中且满足能力 优先返回偏好`() {
        val selector = selectorWith()

        val engine = selector.selectChat(
            preferredId = EngineId("anthropic:default"),
            required = setOf(ChatCapability.PromptCaching),
        )

        assertSame(anthropic, engine)
    }

    @Test
    fun `selectChat 偏好不存在 退化为按能力挑首个`() {
        val selector = selectorWith()

        val engine = selector.selectChat(
            preferredId = EngineId("missing:x"),
            required = setOf(ChatCapability.ToolCalling),
        )

        assertSame(openai, engine)
    }

    @Test
    fun `selectChat 偏好存在但不满足能力 退化为按能力挑首个`() {
        val selector = selectorWith()

        val engine = selector.selectChat(
            preferredId = EngineId("ollama:home"),
            required = setOf(ChatCapability.ToolCalling),
        )

        assertSame(openai, engine)
    }

    @Test
    fun `selectChat 没有任何引擎满足要求 抛 UnsupportedCapability`() {
        val selector = selectorWith()

        assertFailsWith<UnsupportedCapabilityException> {
            selector.selectChat(required = setOf(ChatCapability.VisionInput))
        }
    }

    @Test
    fun `selectChat 注册顺序决定默认选择`() {
        val selector = selectorWith(chats = listOf(ollama, openai, anthropic))

        val engine = selector.selectChat(required = setOf(ChatCapability.Streaming))

        assertSame(ollama, engine)
    }

    @Test
    fun `selectImage 与 selectChat 行为同构`() {
        val selector = selectorWith()

        val byPreference = selector.selectImage(
            preferredId = EngineId("sdwebui:local-7860"),
            required = setOf(ImageCapability.ControlNet),
        )
        assertSame(sdLocal, byPreference)

        assertFailsWith<UnsupportedCapabilityException> {
            selector.selectImage(required = setOf(ImageCapability.FaceSwap))
        }
    }

    @Test
    fun `UnsupportedCapability 异常携带原始 required 集合`() {
        val required = setOf(ChatCapability.VisionInput, ChatCapability.JsonOutput)
        val selector = selectorWith()

        val ex = assertFailsWith<UnsupportedCapabilityException> {
            selector.selectChat(required = required)
        }
        assertEquals(required, ex.required)
    }
}
