package ciyin.ai.facade

import ciyin.ai.core.capability.ChatCapability
import ciyin.ai.core.chat.ChatEvent
import ciyin.ai.core.chat.ChatEvent.Completed
import ciyin.ai.core.chat.ChatEvent.Failed
import ciyin.ai.core.chat.ChatModelInfo
import ciyin.ai.core.chat.ChatRequest
import ciyin.ai.core.engine.ChatEngine
import ciyin.ai.core.engine.EngineId
import ciyin.ai.core.registry.EngineSelector
import ciyin.ai.facade.internal.EngineAttempt
import ciyin.ai.facade.internal.InvocationIds
import ciyin.ai.facade.internal.buildAttempts
import ciyin.ai.facade.internal.collectWithFallback
import ciyin.ai.facade.observability.AiInvocationListener
import ciyin.ai.facade.selection.ChatModelSpec
import ciyin.ai.facade.selection.EnginePreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

/**
 * [AiChat] 的默认实现。
 *
 * 该实现只承担技术策略：
 * - 解析 [ChatModelSpec]；
 * - 通过 [EngineSelector] 挑选主引擎；
 * - 按 [EnginePreferences.chatFallback] 执行单引擎重试与跨引擎降级；
 * - 通过 [AiInvocationListener] 暴露每次尝试的观测事件。
 *
 * 它**不**读取任何业务存储，也**不**把错误翻译成业务文案；这些职责属于 `app:shared/data` 的 Repository。
 *
 * @property selector 引擎选择器。
 * @property preferences 默认模型与降级策略提供者。
 * @property listeners 调用观测监听器列表。
 */
class DefaultAiChat(
    private val selector: EngineSelector,
    private val preferences: EnginePreferences,
    private val listeners: List<AiInvocationListener> = emptyList(),
) : AiChat {

    override fun stream(request: ChatRequest): Flow<ChatEvent> = flow {
        emitAll(stream(ChatModelSpec.Default, request))
    }

    override fun stream(spec: ChatModelSpec, request: ChatRequest): Flow<ChatEvent> = flow {
        val resolvedSpec = resolveRequestedSpec(spec)
        val fallbackPolicy = preferences.chatFallback()
        val primaryAttempt = resolveAttempt(resolvedSpec, request)
        val attempts = buildAttempts(
            primary = primaryAttempt,
            primaryId = primaryAttempt.engine.id,
            backupIds = fallbackPolicy.backupEngines,
            resolve = { backupId -> resolveBackupAttempt(backupId, request) },
        )
        collectWithFallback(
            attempts = attempts,
            policy = fallbackPolicy,
            invocationId = InvocationIds.next(),
            capability = request.primaryCapability(),
            listeners = listeners,
            engineIdOf = { it.id },
            errorOf = { event -> (event as? Failed)?.error },
            isCompleted = { event -> event is Completed },
            uncaughtFailureEvent = { err -> Failed(err) },
        )
    }

    override suspend fun listAvailableModels(): Result<List<ChatModelInfo>> {
        val failures = mutableListOf<Throwable>()
        val deduped = LinkedHashMap<String, ChatModelInfo>()

        selector.allChat().forEach { engine ->
            engine.listModels()
                .onSuccess { models ->
                    models.forEach { model ->
                        deduped.getOrPut(model.model.lowercase()) { model }
                    }
                }
                .onFailure { failures += it }
        }

        if (deduped.isNotEmpty()) {
            return Result.success(deduped.values.toList())
        }
        return Result.failure(
            failures.lastOrNull() ?: IllegalStateException("没有任何聊天引擎返回可用模型"),
        )
    }

    private suspend fun resolveRequestedSpec(spec: ChatModelSpec): ChatModelSpec = when (spec) {
        ChatModelSpec.Default -> {
            when (val preferred = preferences.defaultChatSpec()) {
                ChatModelSpec.Default -> ChatModelSpec.ByCapability(emptySet())
                else -> preferred
            }
        }

        else -> spec
    }

    private fun resolveAttempt(
        spec: ChatModelSpec,
        request: ChatRequest,
    ): EngineAttempt<ChatEngine, ChatEvent> = when (spec) {
        is ChatModelSpec.Default -> {
            val engine = selector.selectChat()
            engine.toAttempt(model = request.model, request = request)
        }

        is ChatModelSpec.Explicit -> {
            val engine = selector.selectChat(preferredId = spec.engineId)
            engine.toAttempt(model = spec.model ?: request.model, request = request)
        }

        is ChatModelSpec.ByCapability -> {
            val engine = selector.selectChat(required = spec.required)
            engine.toAttempt(model = request.model, request = request)
        }
    }

    /**
     * 解析一个备用引擎尝试。
     *
     * 备用引擎列表只有 [EngineId]，没有绑定模型名；因此这里保留调用方原始 [ChatRequest.model]，
     * 不强行复用主引擎的显式模型，避免把某家私有模型名错误地下发给另一家引擎。
     */
    private fun resolveBackupAttempt(
        engineId: EngineId,
        request: ChatRequest,
    ): EngineAttempt<ChatEngine, ChatEvent>? {
        val engine = selector.allChat().firstOrNull { it.id == engineId } ?: return null
        return engine.toAttempt(model = request.model, request = request)
    }

    private fun ChatEngine.toAttempt(
        model: String?,
        request: ChatRequest,
    ): EngineAttempt<ChatEngine, ChatEvent> = EngineAttempt(
        engine = this,
        model = model,
        stream = { stream(request.withModel(model)) },
    )

    private fun ChatRequest.withModel(model: String?): ChatRequest =
        if (this.model == model) this else copy(model = model)

    private fun ChatRequest.primaryCapability(): ChatCapability =
        when {
            tools.isNotEmpty() -> ChatCapability.ToolCalling
            messages.any { message ->
                when (message) {
                    is ciyin.ai.core.chat.ChatMessage.User -> message.attachments.isNotEmpty()
                    else -> false
                }
            } || attachments.isNotEmpty() -> ChatCapability.VisionInput

            else -> ChatCapability.Streaming
        }
}
