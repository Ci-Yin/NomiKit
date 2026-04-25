package ciyin.ai.core.registry

import ciyin.ai.core.capability.ChatCapability
import ciyin.ai.core.engine.EngineId
import ciyin.ai.core.support.FakeChatEngine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame

class DefaultChatEngineRegistryTest {

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

    @Test
    fun `all 保持构造顺序`() {
        val registry = DefaultChatEngineRegistry(listOf(openai, ollama, anthropic))

        val ids = registry.all().map { it.id.value }

        assertEquals(listOf("openai:default", "ollama:home", "anthropic:default"), ids)
    }

    @Test
    fun `get 按 id 精确查找`() {
        val registry = DefaultChatEngineRegistry(listOf(openai, ollama))

        assertSame(openai, registry.get(EngineId("openai:default")))
        assertSame(ollama, registry.get(EngineId("ollama:home")))
        assertNull(registry.get(EngineId("unknown:x")))
    }

    @Test
    fun `findByCapability 空 vararg 返回全部并保持顺序`() {
        val registry = DefaultChatEngineRegistry(listOf(openai, ollama))

        assertEquals(listOf(openai, ollama), registry.findByCapability())
    }

    @Test
    fun `findByCapability 必须同时具备所有要求的能力`() {
        val registry = DefaultChatEngineRegistry(listOf(openai, ollama, anthropic))

        val streamingAndTools = registry.findByCapability(
            ChatCapability.Streaming,
            ChatCapability.ToolCalling,
        )
        assertEquals(listOf(openai, anthropic), streamingAndTools)

        val streamingAndCache = registry.findByCapability(
            ChatCapability.Streaming,
            ChatCapability.PromptCaching,
        )
        assertEquals(listOf(anthropic), streamingAndCache)

        val visionOnly = registry.findByCapability(ChatCapability.VisionInput)
        assertEquals(emptyList(), visionOnly)
    }

    @Test
    fun `重复 id 直接拒绝构造`() {
        val duplicate = FakeChatEngine(id = EngineId("openai:default"))

        assertFailsWith<IllegalArgumentException> {
            DefaultChatEngineRegistry(listOf(openai, duplicate))
        }
    }
}
