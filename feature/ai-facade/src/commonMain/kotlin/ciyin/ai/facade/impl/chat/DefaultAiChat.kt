package ciyin.ai.facade.impl.chat

import ciyin.ai.core.capability.ChatCapability
import ciyin.ai.core.chat.ChatEvent
import ciyin.ai.core.chat.ChatMessage
import ciyin.ai.core.chat.ChatModelInfo
import ciyin.ai.core.chat.ChatRequest
import ciyin.ai.core.engine.ChatEngine
import ciyin.ai.core.engine.EngineId
import ciyin.ai.core.registry.ChatEngineSelector
import ciyin.ai.facade.AiChat
import ciyin.ai.facade.internal.EngineAttempt
import ciyin.ai.facade.internal.InvocationIds
import ciyin.ai.facade.internal.buildAttempts
import ciyin.ai.facade.internal.collectWithFallback
import ciyin.ai.facade.observability.AiInvocationListener
import ciyin.ai.facade.selection.ChatEngineSpec
import ciyin.ai.facade.selection.EnginePreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

/**
 * [ciyin.ai.facade.AiChat] 的默认实现。
 *
 * 该实现只承担技术策略：
 * - 解析 [ciyin.ai.facade.selection.ChatEngineSpec]；
 * - 通过 [ciyin.ai.core.registry.ChatEngineSelector] 挑选主引擎；
 * - 按 [ciyin.ai.facade.selection.EnginePreferences.chatFallback] 执行单引擎重试与跨引擎降级；
 * - 通过 [ciyin.ai.facade.observability.AiInvocationListener] 暴露每次尝试的观测事件。
 *
 * 它**不**读取任何业务存储，也**不**把错误翻译成业务文案；这些职责属于 `app:shared/data` 的 Repository。
 *
 * @property selector 引擎选择器。
 * @property preferences 默认模型与降级策略提供者。
 * @property listeners 调用观测监听器列表。
 */
class DefaultAiChat(
    private val selector: ChatEngineSelector,
    private val preferences: EnginePreferences,
    private val listeners: List<AiInvocationListener> = emptyList(),
) : AiChat {

    override fun stream(request: ChatRequest, spec: ChatEngineSpec): Flow<ChatEvent> = flow {
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
            errorOf = { event -> (event as? ChatEvent.Failed)?.error },
            isCompleted = { event -> event is ChatEvent.Completed },
            uncaughtFailureEvent = { err -> ChatEvent.Failed(err) },
        )
    }

    override suspend fun models(): List<ChatModelInfo> {
        val deduped = LinkedHashMap<String, ChatModelInfo>()
        selector.all().forEach { engine ->
            engine.models().forEach { model ->
                deduped.getOrPut(model.model.lowercase()) { model }
            }
        }
        return deduped.values.toList()
    }

    private suspend fun resolveRequestedSpec(spec: ChatEngineSpec): ChatEngineSpec = when (spec) {
        ChatEngineSpec.Default -> {
            when (val preferred = preferences.defaultChatSpec()) {
                ChatEngineSpec.Default -> ChatEngineSpec.ByCapability(emptySet())
                else -> preferred
            }
        }

        else -> spec
    }

    private fun resolveAttempt(
        spec: ChatEngineSpec,
        request: ChatRequest,
    ): EngineAttempt<ChatEngine, ChatEvent> = when (spec) {
        is ChatEngineSpec.Default -> {
            val engine = selector.select()
            engine.toAttempt(model = request.model, request = request)
        }

        is ChatEngineSpec.Explicit -> {
            val engine = selector.select(preferredId = spec.engineId)
            engine.toAttempt(model = spec.model ?: request.model, request = request)
        }

        is ChatEngineSpec.ByCapability -> {
            val engine = selector.select(required = spec.required)
            engine.toAttempt(model = request.model, request = request)
        }
    }

    /**
     * 解析一个备用引擎尝试。
     *
     * 备用引擎列表只有 [ciyin.ai.core.engine.EngineId]，没有绑定模型名；因此这里保留调用方原始 [ChatRequest.model]，
     * 不强行复用主引擎的显式模型，避免把某家私有模型名错误地下发给另一家引擎。
     */
    private fun resolveBackupAttempt(
        engineId: EngineId,
        request: ChatRequest,
    ): EngineAttempt<ChatEngine, ChatEvent>? {
        val engine = selector.all().firstOrNull { it.id == engineId } ?: return null
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
                    is ChatMessage.User -> message.attachments.isNotEmpty()
                    else -> false
                }
            } || attachments.isNotEmpty() -> ChatCapability.VisionInput

            else -> ChatCapability.Streaming
        }
}