package ciyin.ai.integrate.chat

import ciyin.ai.core.capability.ChatCapability
import ciyin.ai.core.chat.ChatEvent
import ciyin.ai.core.chat.ChatMessage
import ciyin.ai.core.chat.ChatModelInfo
import ciyin.ai.core.chat.ChatRequest
import ciyin.ai.core.chat.ChatResponse
import ciyin.ai.core.engine.ChatEngine
import ciyin.ai.core.engine.EngineId
import ciyin.ai.core.engine.EngineRuntime
import ciyin.ai.core.error.UnsupportedCapabilityException
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * [AiChatIntegrate] 的基础路由与装配测试。
 */
class AiChatIntegrateTest {

    /** 验证未注册任何聊天引擎时会暴露装配错误。 */
    @Test
    fun stream_without_engines_throws_unsupported_capability() = runTest {
        val integrate = testAiChatIntegrate(
            defaultEngineConfigs = emptyList(),
            buildChatEngine = { unusedStubEngine() },
        )
        assertFailsWith<UnsupportedCapabilityException> {
            integrate.stream(ChatRequest(messages = listOf(ChatMessage.User("hi")))).toList()
        }
    }

    /** 验证未注册任何聊天引擎时模型列表为空。 */
    @Test
    fun models_without_registered_runtime_returns_empty() = runTest {
        val integrate = testAiChatIntegrate(
            defaultEngineConfigs = emptyList(),
            buildChatEngine = { unusedStubEngine() },
        )
        assertContentEquals(emptyList(), integrate.models())
    }

    /** 验证请求未带模型时会使用配置中的默认模型。 */
    @Test
    fun stream_merges_config_default_model_when_request_model_null() = runTest {
        val stub = recordingStubEngine(IntegrateChatEngineIds.openAiCompatible)
        val integrate = testAiChatIntegrate(
            buildChatEngine = { stub },
        )
        integrate.stream(ChatRequest(messages = listOf(ChatMessage.User("hi")))).toList()
        assertEquals("from-config", stub.receivedRequests.single().model)
    }

    /** 验证显式规格模型会覆盖配置中的默认模型。 */
    @Test
    fun stream_uses_explicit_spec_model_over_config_default_model() = runTest {
        val stub = recordingStubEngine(IntegrateChatEngineIds.openAiCompatible)
        val integrate = testAiChatIntegrate(
            buildChatEngine = { stub },
        )
        integrate.stream(
            request = ChatRequest(messages = listOf(ChatMessage.User("hi"))),
            spec = ChatEngineSpec.Explicit(
                engineId = IntegrateChatEngineIds.openAiCompatible,
                model = "from-spec",
            ),
        ).toList()
        assertEquals("from-spec", stub.receivedRequests.single().model)
    }

    /** 验证请求和配置都未带模型时会使用显式规格中的模型。 */
    @Test
    fun stream_uses_explicit_spec_model_when_request_and_config_model_null() = runTest {
        val id = EngineId("openai-compatible:test")
        val stub = recordingStubEngine(id)
        val integrate = testAiChatIntegrate(
            defaultEngineConfigs = listOf(
                ChatEngineConfig.OpenAiCompatible(
                    engineId = id,
                    baseUrl = "http://127.0.0.1:11434/v1",
                    apiKey = null,
                    defaultModel = null,
                ),
            ),
            buildChatEngine = { stub },
        )
        integrate.stream(
            request = ChatRequest(messages = listOf(ChatMessage.User("hi"))),
            spec = ChatEngineSpec.Explicit(
                engineId = id,
                model = "from-spec",
            ),
        ).toList()
        assertEquals("from-spec", stub.receivedRequests.single().model)
    }

    /** 验证跨引擎模型列表会按小写模型名去重并保留首个。 */
    @Test
    fun models_deduplicates_by_lowercase_model_name() = runTest {
        val idA = EngineId("openai-compatible:a")
        val idB = EngineId("openai-compatible:b")
        val first = recordingStubEngine(
            id = idA,
            modelsResult = listOf(ChatModelInfo(engineId = idA, model = "demo-model")),
        )
        val second = recordingStubEngine(
            id = idB,
            modelsResult = listOf(ChatModelInfo(engineId = idB, model = "DEMO-MODEL")),
        )
        val integrate = testAiChatIntegrate(
            defaultEngineConfigs = listOf(
                ChatEngineConfig.OpenAiCompatible(
                    engineId = idA,
                    baseUrl = "http://127.0.0.1:1111/v1",
                    apiKey = null,
                    defaultModel = "a",
                ),
                ChatEngineConfig.OpenAiCompatible(
                    engineId = idB,
                    baseUrl = "http://127.0.0.1:2222/v1",
                    apiKey = null,
                    defaultModel = "b",
                ),
            ),
            buildChatEngine = { cfg ->
                when (cfg.engineId) {
                    idA -> first
                    idB -> second
                    else -> unusedStubEngine()
                }
            },
        )
        assertEquals(
            listOf(ChatModelInfo(engineId = idA, model = "demo-model")),
            integrate.models(),
        )
    }
}

/**
 * 创建测试用聊天聚合入口。
 */
private fun testAiChatIntegrate(
    defaultEngineConfigs: List<ChatEngineConfig> = listOf(
        ChatEngineConfig.OpenAiCompatible(
            engineId = IntegrateChatEngineIds.openAiCompatible,
            baseUrl = "http://127.0.0.1:11434/v1",
            apiKey = null,
            defaultModel = "from-config",
        ),
    ),
    buildChatEngine: (ChatEngineConfig) -> ChatEngine,
): AiChatIntegrate = AiChatIntegrate(
    defaultEngineConfigs = defaultEngineConfigs,
    buildChatEngine = buildChatEngine,
)

/**
 * 创建不会发起真实网络请求的兜底聊天引擎。
 */
private fun unusedStubEngine(): ChatEngine =
    recordingStubEngine(IntegrateChatEngineIds.openAiCompatible)

/**
 * 创建会记录请求的聊天引擎。
 */
private fun recordingStubEngine(
    id: EngineId,
    modelsResult: List<ChatModelInfo> = emptyList(),
): RecordingStubChatEngine = RecordingStubChatEngine(
    id = id,
    modelsResult = modelsResult,
)

/**
 * 不发起真实 HTTP：记录 [ChatRequest]，并产出符合聊天事件契约的终结事件。
 *
 * @property id 测试引擎 ID。
 * @property modelsResult 模型列表返回值。
 */
private class RecordingStubChatEngine(
    override val id: EngineId,
    private val modelsResult: List<ChatModelInfo> = emptyList(),
) : ChatEngine {

    /** 本测试引擎收到的聊天请求。 */
    val receivedRequests = mutableListOf<ChatRequest>()

    /** 测试引擎提供方标识。 */
    override val provider: String = "stub"

    /** 测试引擎运行时类型。 */
    override val runtime: EngineRuntime = EngineRuntime.RemoteSelfHosted

    /** 测试引擎声明的聊天能力集合。 */
    override val capabilities: Set<ChatCapability> = emptySet()

    /** 记录聊天请求并返回成功事件流。 */
    override fun stream(request: ChatRequest) = flow {
        receivedRequests += request
        emit(ChatEvent.Started)
        emit(ChatEvent.Completed(ChatResponse(content = "ok")))
    }

    /** 返回预设模型列表。 */
    override suspend fun models(): List<ChatModelInfo> = modelsResult

    /** 测试引擎始终通过本地校验。 */
    override suspend fun validate(request: ChatRequest): Result<Unit> = Result.success(Unit)
}
