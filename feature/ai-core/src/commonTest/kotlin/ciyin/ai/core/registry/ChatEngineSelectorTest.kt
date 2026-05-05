package ciyin.ai.core.registry

import ciyin.ai.core.capability.ChatCapability
import ciyin.ai.core.engine.EngineId
import ciyin.ai.core.error.UnsupportedCapabilityException
import ciyin.ai.core.support.FakeChatEngine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class ChatEngineSelectorTest {

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

    private fun selectorWith(
        chats: List<FakeChatEngine> = listOf(openai, ollama, anthropic),
    ) = ChatEngineSelector(registry = DefaultChatEngineRegistry(chats))

    @Test
    fun `select 无偏好 无要求 返回首个`() {
        val selector = selectorWith()

        val engine = selector.select()

        assertSame(openai, engine)
    }

    @Test
    fun `select 偏好命中且满足能力 优先返回偏好`() {
        val selector = selectorWith()

        val engine = selector.select(
            preferredId = EngineId("anthropic:default"),
            required = setOf(ChatCapability.PromptCaching),
        )

        assertSame(anthropic, engine)
    }

    @Test
    fun `select 偏好不存在 退化为按能力挑首个`() {
        val selector = selectorWith()

        val engine = selector.select(
            preferredId = EngineId("missing:x"),
            required = setOf(ChatCapability.ToolCalling),
        )

        assertSame(openai, engine)
    }

    @Test
    fun `select 偏好存在但不满足能力 退化为按能力挑首个`() {
        val selector = selectorWith()

        val engine = selector.select(
            preferredId = EngineId("ollama:home"),
            required = setOf(ChatCapability.ToolCalling),
        )

        assertSame(openai, engine)
    }

    @Test
    fun `select 没有任何引擎满足要求 抛 UnsupportedCapability`() {
        val selector = selectorWith()

        assertFailsWith<UnsupportedCapabilityException> {
            selector.select(required = setOf(ChatCapability.VisionInput))
        }
    }

    @Test
    fun `select 注册顺序决定默认选择`() {
        val selector = selectorWith(chats = listOf(ollama, openai, anthropic))

        val engine = selector.select(required = setOf(ChatCapability.Streaming))

        assertSame(ollama, engine)
    }

    @Test
    fun `UnsupportedCapability 异常携带原始 required 集合`() {
        val required = setOf(ChatCapability.VisionInput, ChatCapability.JsonOutput)
        val selector = selectorWith()

        val ex = assertFailsWith<UnsupportedCapabilityException> {
            selector.select(required = required)
        }
        assertEquals(required, ex.required)
    }
}
