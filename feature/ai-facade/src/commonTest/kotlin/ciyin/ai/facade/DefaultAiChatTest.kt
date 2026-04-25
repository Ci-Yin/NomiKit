package ciyin.ai.facade

import ciyin.ai.core.capability.ChatCapability
import ciyin.ai.core.chat.ChatEvent
import ciyin.ai.core.chat.ChatRequest
import ciyin.ai.core.chat.ChatResponse
import ciyin.ai.core.engine.EngineId
import ciyin.ai.core.error.AiEngineError
import ciyin.ai.core.registry.DefaultChatEngineRegistry
import ciyin.ai.core.registry.DefaultImageEngineRegistry
import ciyin.ai.core.registry.EngineSelector
import ciyin.ai.facade.selection.ChatModelSpec
import ciyin.ai.facade.selection.FallbackPolicy
import ciyin.ai.facade.support.FakeEnginePreferences
import ciyin.ai.facade.support.RecordingChatEngine
import ciyin.ai.facade.support.RecordingListener
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * [DefaultAiChat] 的单元测试。
 */
class DefaultAiChatTest {

    /**
     * 当业务按能力筛选模型时，应选中首个满足要求的引擎，而不是仅按注册顺序取第一个。
     */
    @Test
    fun `ByCapability 应选择满足能力的聊天引擎`() = runBlocking {
        val streamingOnly = RecordingChatEngine(
            id = EngineId("chat:streaming"),
            capabilities = setOf(ChatCapability.Streaming),
            plannedEvents = ArrayDeque(
                listOf(
                    listOf(
                        ChatEvent.Started,
                        ChatEvent.Completed(ChatResponse(content = "streaming")),
                    ),
                ),
            ),
        )
        val toolCalling = RecordingChatEngine(
            id = EngineId("chat:tools"),
            capabilities = setOf(ChatCapability.Streaming, ChatCapability.ToolCalling),
            plannedEvents = ArrayDeque(
                listOf(
                    listOf(
                        ChatEvent.Started,
                        ChatEvent.Completed(ChatResponse(content = "tools")),
                    ),
                ),
            ),
        )
        val aiChat = DefaultAiChat(
            selector = selector(chats = listOf(streamingOnly, toolCalling)),
            preferences = FakeEnginePreferences(),
        )

        aiChat.stream(
            spec = ChatModelSpec.ByCapability(setOf(ChatCapability.ToolCalling)),
            request = request(),
        ).toList()

        assertEquals(0, streamingOnly.receivedRequests.size)
        assertEquals(1, toolCalling.receivedRequests.size)
    }

    /**
     * 当主引擎失败且错误类型命中 fallback 条件时，应切换到备用引擎继续完成调用，
     * 同时 listener 顺序必须保持 `start -> failed -> start -> completed`。
     */
    @Test
    fun `fallback 命中 triggerOn 时应切换备用引擎并按顺序触发 listener`() = runBlocking {
        val primary = RecordingChatEngine(
            id = EngineId("chat:primary"),
            capabilities = setOf(ChatCapability.Streaming),
            plannedEvents = ArrayDeque(
                listOf(
                    listOf(
                        ChatEvent.Started,
                        ChatEvent.Failed(AiEngineError.Network(cause = null, message = "timeout")),
                    ),
                ),
            ),
        )
        val backup = RecordingChatEngine(
            id = EngineId("chat:backup"),
            capabilities = setOf(ChatCapability.Streaming),
            plannedEvents = ArrayDeque(
                listOf(
                    listOf(
                        ChatEvent.Started,
                        ChatEvent.Completed(ChatResponse(content = "fallback-ok")),
                    ),
                ),
            ),
        )
        val listener = RecordingListener()
        val aiChat = DefaultAiChat(
            selector = selector(chats = listOf(primary, backup)),
            preferences = FakeEnginePreferences(
                chatFallbackPolicy = FallbackPolicy(
                    maxRetries = 0,
                    backupEngines = listOf(backup.id),
                    triggerOn = setOf(AiEngineError.Network::class),
                ),
            ),
            listeners = listOf(listener),
        )

        val events = aiChat.stream(request()).toList()

        assertEquals(
            listOf(
                "start:chat:primary:1",
                "failed:chat:primary:1:Network",
                "start:chat:backup:2",
                "completed:chat:backup:2",
            ),
            listener.records,
        )
        assertEquals(1, primary.receivedRequests.size)
        assertEquals(1, backup.receivedRequests.size)
        assertEquals(4, events.size)
        assertIs<ChatEvent.Started>(events[0])
        assertIs<ChatEvent.Failed>(events[1])
        assertIs<ChatEvent.Started>(events[2])
        assertEquals("fallback-ok", (events[3] as ChatEvent.Completed).response.content)
    }

    /**
     * 当错误类型不命中 fallback 条件时，应立即把失败透传给上层，而不是盲目尝试备用引擎。
     */
    @Test
    fun `fallback 未命中 triggerOn 时不应切换备用引擎`() = runBlocking {
        val primary = RecordingChatEngine(
            id = EngineId("chat:primary"),
            capabilities = setOf(ChatCapability.Streaming),
            plannedEvents = ArrayDeque(
                listOf(
                    listOf(
                        ChatEvent.Started,
                        ChatEvent.Failed(AiEngineError.Unsupported("not-supported")),
                    ),
                ),
            ),
        )
        val backup = RecordingChatEngine(
            id = EngineId("chat:backup"),
            capabilities = setOf(ChatCapability.Streaming),
            plannedEvents = ArrayDeque(
                listOf(
                    listOf(
                        ChatEvent.Started,
                        ChatEvent.Completed(ChatResponse(content = "should-not-run")),
                    ),
                ),
            ),
        )
        val listener = RecordingListener()
        val aiChat = DefaultAiChat(
            selector = selector(chats = listOf(primary, backup)),
            preferences = FakeEnginePreferences(
                chatFallbackPolicy = FallbackPolicy(
                    maxRetries = 0,
                    backupEngines = listOf(backup.id),
                    triggerOn = setOf(AiEngineError.Network::class),
                ),
            ),
            listeners = listOf(listener),
        )

        val events = aiChat.stream(request()).toList()

        assertEquals(1, primary.receivedRequests.size)
        assertEquals(0, backup.receivedRequests.size)
        assertEquals(
            listOf(
                "start:chat:primary:1",
                "failed:chat:primary:1:Unsupported",
            ),
            listener.records,
        )
        assertEquals(2, events.size)
        assertIs<ChatEvent.Started>(events[0])
        assertIs<ChatEvent.Failed>(events[1])
    }

    /**
     * 构造一套最小可用的 [EngineSelector]，便于聚焦测试 Facade 的编排逻辑。
     */
    private fun selector(
        chats: List<RecordingChatEngine>,
    ): EngineSelector = EngineSelector(
        chatRegistry = DefaultChatEngineRegistry(chats),
        imageRegistry = DefaultImageEngineRegistry(emptyList()),
    )

    /**
     * 生成一份最小聊天请求。
     */
    private fun request(): ChatRequest = ChatRequest(
        messages = emptyList(),
    )
}
