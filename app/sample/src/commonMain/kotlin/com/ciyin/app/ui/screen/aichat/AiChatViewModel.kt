package com.ciyin.app.ui.screen.aichat

import ciyin.ai.core.chat.ChatEvent
import ciyin.ai.core.chat.ChatMessage
import ciyin.ai.core.chat.ChatOptions
import ciyin.ai.core.chat.ChatRequest
import ciyin.ai.core.chat.ChatResponse
import ciyin.ai.core.error.AiEngineError
import ciyin.ui.foundation.viewmodel.StateMachineMviViewModel
import com.ciyin.app.ui.screen.aichat.data.AiChatRepository
import com.freeletics.flowredux2.FlowReduxBuilder
import com.freeletics.flowredux2.FlowReduxStateMachineFactory
import com.freeletics.flowredux2.initializeWith
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * AI 聊天示例 ViewModel。
 *
 * 负责维护内存会话、把 UI 输入转换为 [ChatRequest]，并将 [ChatEvent] 回灌为页面 Action。
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class AiChatViewModel :
    StateMachineMviViewModel<AiChatUiState, AiChatAction, AiChatEffect>() {

    private var streamJob: Job? = null
    private val repository: AiChatRepository = AiChatRepository()

    /** 初始化聊天页面的默认状态。 */
    override fun FlowReduxStateMachineFactory<AiChatUiState, AiChatAction>.initialize() {
        initializeWith { AiChatUiState() }
    }

    /** 声明聊天页面的 MVI 状态转移规则。 */
    override fun FlowReduxBuilder<AiChatUiState, AiChatAction>.spec() {
        inState<AiChatUiState> {

            // 进入页面时从 DataStore 读取偏好并派发 PrefsLoaded
            onEnterEffect {
                val prefs = repository.loadPreferences()
                dispatchAction(AiChatAction.PrefsLoaded(prefs))
            }

            // 合并 DataStore 中的偏好与历史消息，并校正下一条消息 ID
            on<AiChatAction.PrefsLoaded> { action ->
                mutate {
                    val prefs = action.prefs
                    val nextMessageId = (prefs.messages.maxOfOrNull { it.id } ?: 0L) + 1L
                    copy(
                        baseUrl = prefs.baseUrl,
                        apiKey = prefs.apiKey,
                        model = prefs.model,
                        messages = prefs.messages,
                        nextMessageId = nextMessageId,
                    )
                }
            }

            // 取消进行中的请求并触发返回导航
            onActionEffect<AiChatAction.BackClick> {
                streamJob?.cancel()
                poseEffect(AiChatEffect.NavigateBack)
            }

            // 更新 Base URL 并异步写回偏好
            on<AiChatAction.BaseUrlChange> { action ->
                mutate {
                    copy(baseUrl = action.value, errorMessage = null).apply {
                        persistConfig(
                            this
                        )
                    }
                }
            }

            // 更新 API Key 并异步写回偏好
            on<AiChatAction.ApiKeyChange> { action ->
                mutate {
                    copy(
                        apiKey = action.value,
                        errorMessage = null
                    ).apply { persistConfig(this) }
                }
            }

            // 更新模型名并异步写回偏好
            on<AiChatAction.ModelChange> { action ->
                mutate {
                    copy(
                        model = action.value,
                        errorMessage = null
                    ).apply { persistConfig(this) }
                }
            }

            // 同步输入框文案
            on<AiChatAction.InputChange> { action ->
                mutate { copy(input = action.value, errorMessage = null) }
            }

            // 校验后发送用户消息并启动流式对话
            on<AiChatAction.SendClick> {
                val current = snapshot
                when {
                    current.input.isBlank() -> noChange()
                    current.baseUrl.isBlank() || current.model.isBlank() ->
                        mutate { copy(errorMessage = "请先填写 baseUrl 和模型名") }

                    current.isStreaming -> noChange()
                    else -> {
                        val userText = current.input.trim()
                        val requestId = current.nextMessageId
                        val userMessage = AiChatMessageItem(
                            id = requestId,
                            role = AiChatMessageRole.User,
                            text = userText,
                        )
                        startStreaming(
                            requestId = requestId,
                            config = current.connectionConfig(),
                            messages = current.toChatMessages(userText),
                        )
                        mutate {
                            copy(
                                input = "",
                                messages = messages + userMessage,
                                isStreaming = true,
                                errorMessage = null,
                                nextMessageId = nextMessageId + 1,
                                activeRequestId = requestId,
                                activeAssistantMessageId = null,
                            )
                        }
                    }
                }
            }

            // 流开始：追加空的助手占位气泡
            on<AiChatAction.ChatStarted> { action ->
                mutate {
                    if (!isActive(action.requestId) || activeAssistantMessageId != null) {
                        this
                    } else {
                        copy(
                            messages = messages + AiChatMessageItem(
                                id = nextMessageId,
                                role = AiChatMessageRole.Assistant,
                                text = "",
                            ),
                            nextMessageId = nextMessageId + 1,
                            activeAssistantMessageId = nextMessageId,
                            isStreaming = true,
                            errorMessage = null,
                        )
                    }
                }
            }

            // 流式增量：向当前助手气泡拼接文本
            on<AiChatAction.ChatDelta> { action ->
                mutate {
                    if (!isActive(action.requestId)) {
                        this
                    } else {
                        copy(
                            messages = messages.appendAssistantDelta(
                                activeAssistantMessageId,
                                action.text
                            ),
                            isStreaming = true,
                            errorMessage = null,
                        )
                    }
                }
            }

            // 流正常结束：写入最终回复并持久化消息
            on<AiChatAction.ChatCompleted> { action ->
                mutate {
                    if (!isActive(action.requestId)) {
                        this
                    } else {
                        finishWithAssistantResponse(action.response).apply { persistMessages(this) }
                    }
                }
            }

            // 引擎结构化失败：展示错误文案并持久化消息
            on<AiChatAction.ChatFailed> { action ->
                mutate {
                    if (!isActive(action.requestId)) {
                        this
                    } else {
                        finishWithError(action.error.readableMessage()).apply { persistMessages(this) }
                    }
                }
            }

            // 调用链异常：展示异常信息并持久化消息
            on<AiChatAction.ChatException> { action ->
                mutate {
                    if (!isActive(action.requestId)) {
                        this
                    } else {
                        finishWithError(action.message).apply { persistMessages(this) }
                    }
                }
            }
        }
    }

    private fun startStreaming(
        requestId: Long,
        config: AiChatConnectionConfig,
        messages: List<ChatMessage>,
    ) {
        streamJob?.cancel()
        streamJob = backgroundScope.launch(Dispatchers.IO) {
            try {
                repository.chat(config)
                    .stream(
                        ChatRequest(
                            model = config.model,
                            messages = messages,
                            options = ChatOptions(stream = true),
                        )
                    )
                    .collect { event ->
                        dispatchAction(event.toAction(requestId))
                    }
            } catch (ce: CancellationException) {
                throw ce
            } catch (throwable: Throwable) {
                dispatchAction(
                    AiChatAction.ChatException(
                        requestId = requestId,
                        message = throwable.message ?: throwable::class.simpleName ?: "未知异常",
                    )
                )
            }
        }
    }

    private fun ChatEvent.toAction(requestId: Long): AiChatAction = when (this) {
        ChatEvent.Started -> AiChatAction.ChatStarted(requestId)
        is ChatEvent.Delta -> AiChatAction.ChatDelta(requestId, text)
        is ChatEvent.Completed -> AiChatAction.ChatCompleted(requestId, response)
        is ChatEvent.Failed -> AiChatAction.ChatFailed(requestId, error)
        is ChatEvent.ToolCall -> AiChatAction.ChatDelta(requestId, "\n[工具调用] $name: $arguments")
    }

    private fun persistConfig(state: AiChatUiState) {
        backgroundScope.launch {
            repository.persistConnection(
                baseUrl = state.baseUrl,
                apiKey = state.apiKey,
                model = state.model,
            )
        }
    }

    private fun persistMessages(state: AiChatUiState) {
        backgroundScope.launch {
            repository.persistMessages(state.messages)
        }
    }

    private fun AiChatUiState.connectionConfig(): AiChatConnectionConfig = AiChatConnectionConfig(
        baseUrl = baseUrl,
        apiKey = apiKey,
        model = model,
    )

    private fun AiChatUiState.toChatMessages(pendingUserText: String): List<ChatMessage> =
        buildList {
            add(ChatMessage.System("你是 NomiKit 示例中的中文 AI 助手，请用简洁、友好的中文回答。"))
            messages.forEach { message ->
                when (message.role) {
                    AiChatMessageRole.User -> add(ChatMessage.User(message.text))
                    AiChatMessageRole.Assistant -> if (message.text.isNotBlank()) {
                        add(ChatMessage.Assistant(message.text))
                    }
                }
            }
            add(ChatMessage.User(pendingUserText))
        }

    private fun AiChatUiState.isActive(requestId: Long): Boolean =
        activeRequestId == requestId

    private fun AiChatUiState.finishWithAssistantResponse(response: ChatResponse): AiChatUiState {
        val finalText = response.content.ifBlank {
            if (response.toolCalls.isNotEmpty()) {
                "模型返回了工具调用，但没有生成可展示文本。"
            } else {
                ""
            }
        }
        return copy(
            messages = messages.replaceAssistantText(activeAssistantMessageId, finalText),
            isStreaming = false,
            errorMessage = null,
            activeRequestId = null,
            activeAssistantMessageId = null,
        )
    }

    private fun AiChatUiState.finishWithError(message: String): AiChatUiState = copy(
        messages = messages.replaceAssistantText(activeAssistantMessageId, "请求失败：$message"),
        isStreaming = false,
        errorMessage = message,
        activeRequestId = null,
        activeAssistantMessageId = null,
    )

    private fun List<AiChatMessageItem>.appendAssistantDelta(
        assistantMessageId: Long?,
        delta: String,
    ): List<AiChatMessageItem> = map { message ->
        if (message.id == assistantMessageId) {
            message.copy(text = message.text + delta)
        } else {
            message
        }
    }

    private fun List<AiChatMessageItem>.replaceAssistantText(
        assistantMessageId: Long?,
        text: String,
    ): List<AiChatMessageItem> = map { message ->
        if (message.id == assistantMessageId) {
            message.copy(text = text)
        } else {
            message
        }
    }

    private fun AiEngineError.readableMessage(): String = when (this) {
        is AiEngineError.Network -> message ?: cause?.message ?: "网络连接失败"
        is AiEngineError.Unauthorized -> providerMessage ?: "鉴权失败，请检查 API Key"
        is AiEngineError.RateLimited -> providerMessage ?: "请求被限流，请稍后重试"
        is AiEngineError.Protocol ->
            message.ifBlank { cause?.message?.trim().orEmpty() }.ifBlank { "协议错误" }

        is AiEngineError.Refused -> reason
        is AiEngineError.Unsupported -> message
        AiEngineError.Cancelled -> "请求已取消"
        is AiEngineError.Unknown -> message ?: cause?.message ?: "未知错误"
    }
}
