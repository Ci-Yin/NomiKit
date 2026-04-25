package ciyin.ai.facade.support

import ciyin.ai.core.capability.ChatCapability
import ciyin.ai.core.capability.ImageCapability
import ciyin.ai.core.chat.ChatEvent
import ciyin.ai.core.chat.ChatModelInfo
import ciyin.ai.core.chat.ChatRequest
import ciyin.ai.core.engine.ChatEngine
import ciyin.ai.core.engine.EngineId
import ciyin.ai.core.engine.EngineRuntime
import ciyin.ai.core.engine.ImageEngine
import ciyin.ai.core.error.AiEngineError
import ciyin.ai.core.image.ImageEvent
import ciyin.ai.core.image.ImageModelInfo
import ciyin.ai.core.image.ImageRequest
import ciyin.ai.facade.observability.AiInvocationListener
import ciyin.ai.facade.observability.InvocationMetadata
import ciyin.ai.facade.selection.ChatModelSpec
import ciyin.ai.facade.selection.EnginePreferences
import ciyin.ai.facade.selection.FallbackPolicy
import ciyin.ai.facade.selection.ImageModelSpec
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * `ai-facade` 单测使用的可编排聊天引擎。
 *
 * 通过 [plannedEvents] 预置每次调用要返回的事件序列，并把收到的 [ChatRequest] 记录到 [receivedRequests]，
 * 方便断言模型覆盖、fallback 切换以及调用次数。
 */
internal class RecordingChatEngine(
    override val id: EngineId,
    override val provider: String = "fake-chat",
    override val runtime: EngineRuntime = EngineRuntime.RemoteCloud,
    override val capabilities: Set<ChatCapability> = emptySet(),
    private val plannedEvents: ArrayDeque<List<ChatEvent>> = ArrayDeque(),
    private val modelsResult: Result<List<ChatModelInfo>> = Result.success(emptyList()),
) : ChatEngine {

    /** 按调用顺序记录收到的请求。 */
    val receivedRequests: MutableList<ChatRequest> = mutableListOf()

    override fun stream(request: ChatRequest): Flow<ChatEvent> = flow {
        receivedRequests += request
        val events = if (plannedEvents.isNotEmpty()) plannedEvents.removeFirst() else emptyList()
        events.forEach { emit(it) }
    }

    override suspend fun listModels(): Result<List<ChatModelInfo>> = modelsResult

    override suspend fun validate(request: ChatRequest): Result<Unit> = Result.success(Unit)
}

/**
 * `ai-facade` 单测使用的可编排生图引擎。
 */
internal class RecordingImageEngine(
    override val id: EngineId,
    override val provider: String = "fake-image",
    override val runtime: EngineRuntime = EngineRuntime.RemoteSelfHosted,
    override val capabilities: Set<ImageCapability> = emptySet(),
    private val plannedEvents: ArrayDeque<List<ImageEvent>> = ArrayDeque(),
    private val modelsResult: Result<List<ImageModelInfo>> = Result.success(emptyList()),
) : ImageEngine {

    /** 按调用顺序记录收到的请求。 */
    val receivedRequests: MutableList<ImageRequest> = mutableListOf()

    override fun generate(request: ImageRequest): Flow<ImageEvent> = flow {
        receivedRequests += request
        val events = if (plannedEvents.isNotEmpty()) plannedEvents.removeFirst() else emptyList()
        events.forEach { emit(it) }
    }

    override suspend fun listModels(): Result<List<ImageModelInfo>> = modelsResult

    override suspend fun validate(request: ImageRequest): Result<Unit> = Result.success(Unit)
}

/**
 * 测试用的偏好实现，允许按需注入默认模型与 fallback 策略。
 */
internal class FakeEnginePreferences(
    private val chatSpec: ChatModelSpec = ChatModelSpec.Default,
    private val imageSpec: ImageModelSpec = ImageModelSpec.Default,
    private val chatFallbackPolicy: FallbackPolicy = FallbackPolicy(),
    private val imageFallbackPolicy: FallbackPolicy = FallbackPolicy(),
) : EnginePreferences {

    override suspend fun defaultChatSpec(): ChatModelSpec = chatSpec

    override suspend fun defaultImageSpec(): ImageModelSpec = imageSpec

    override suspend fun chatFallback(): FallbackPolicy = chatFallbackPolicy

    override suspend fun imageFallback(): FallbackPolicy = imageFallbackPolicy
}

/**
 * 记录 listener 回调顺序的测试监听器。
 */
internal class RecordingListener : AiInvocationListener {

    /** 顺序记录开始 / 成功 / 失败事件，便于断言时序。 */
    val records: MutableList<String> = mutableListOf()

    override fun onStart(metadata: InvocationMetadata) {
        records += "start:${metadata.engineId.value}:${metadata.attempt}"
    }

    override fun onCompleted(metadata: InvocationMetadata, durationMs: Long) {
        records += "completed:${metadata.engineId.value}:${metadata.attempt}"
    }

    override fun onFailed(metadata: InvocationMetadata, error: AiEngineError) {
        records += "failed:${metadata.engineId.value}:${metadata.attempt}:${error::class.simpleName}"
    }
}
