package ciyin.ai.chat.openai

import ciyin.ai.chat.openai.client.OpenAiChatClient
import ciyin.ai.chat.openai.dto.ModelListResponseDto
import ciyin.ai.chat.openai.mapper.ChatResponseAccumulator
import ciyin.ai.chat.openai.mapper.toAiEngineError
import ciyin.ai.chat.openai.mapper.toOpenAiRequestBody
import ciyin.ai.core.capability.AiCapability
import ciyin.ai.core.capability.ChatCapability
import ciyin.ai.core.chat.ChatAttachment
import ciyin.ai.core.chat.ChatEvent
import ciyin.ai.core.chat.ChatModelInfo
import ciyin.ai.core.chat.ChatRequest
import ciyin.ai.core.engine.ChatEngine
import ciyin.ai.core.engine.EngineRuntime
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * 基于 OpenAI 兼容协议的聊天引擎实现。
 *
 * 支持：
 * - 流式聊天（SSE）；
 * - 非流式聊天；
 * - 工具调用；
 * - 图像输入。
 */
class OpenAiChatEngine internal constructor(
    private val config: OpenAiChatEngineConfig,
    private val client: OpenAiChatClient,
) : ChatEngine {

    /**
     * 默认构造：使用模块内置的 Ktor 客户端实现。
     */
    constructor(config: OpenAiChatEngineConfig) : this(
        config = config,
        client = OpenAiChatClient(config),
    )

    override val id = config.id

    override val provider: String = "openai-compatible"

    override val runtime: EngineRuntime = if (config.baseUrl.isLocalBaseUrl()) {
        EngineRuntime.RemoteSelfHosted
    } else {
        EngineRuntime.RemoteCloud
    }

    override val capabilities: Set<AiCapability> = setOf(
        ChatCapability.Streaming,
        ChatCapability.ToolCalling,
        ChatCapability.VisionInput,
        ChatCapability.JsonOutput,
        ChatCapability.SystemPrompt,
    )

    override fun stream(request: ChatRequest): Flow<ChatEvent> = flow {
        emit(ChatEvent.Started)
        val body = try {
            request.toOpenAiRequestBody(config)
        } catch (t: Throwable) {
            emit(ChatEvent.Failed(t.toAiEngineError()))
            return@flow
        }

        val accumulator = ChatResponseAccumulator()
        runCatching {
            if (request.options.stream) {
                client.streamChat(body).collect { chunk ->
                    accumulator.append(chunk).forEach { emit(it) }
                }
            } else {
                accumulator.absorb(client.completeChat(body))
            }
        }.fold(
            onSuccess = {
                emit(ChatEvent.Completed(accumulator.build()))
            },
            onFailure = { throwable ->
                emit(ChatEvent.Failed(throwable.toAiEngineError()))
            },
        )
    }

    override suspend fun models(): List<ChatModelInfo> = runCatching {
        client.listModels().toChatModelInfos()
    }.getOrElse { emptyList() }

    override suspend fun validate(request: ChatRequest): Result<Unit> = runCatching {
        require(request.messages.isNotEmpty()) { "messages 不能为空" }
        require(request.attachments.isEmpty()) { "OpenAI 兼容聊天暂不支持请求级 attachments" }
        require(request.model != null || config.defaultModel != null) { "缺少 model，且未配置 defaultModel" }
        request.tools.forEach { tool ->
            require(tool.name.isNotBlank()) { "tool.name 不能为空" }
        }
        request.messages.forEach { message ->
            when (message) {
                is ciyin.ai.core.chat.ChatMessage.User -> {
                    message.attachments.forEach { attachment ->
                        require(attachment is ChatAttachment.Image) { "当前仅支持图像附件" }
                    }
                }

                else -> Unit
            }
        }
    }

    /**
     * 把 `/models` 响应映射为通用模型信息。
     */
    private fun ModelListResponseDto.toChatModelInfos(): List<ChatModelInfo> = data.map { model ->
        ChatModelInfo(
            engineId = id,
            model = model.id,
            capabilities = capabilities.filterIsInstance<ChatCapability>().toSet(),
        )
    }
}

/**
 * 判断 baseUrl 是否为本地自托管端点。
 */
private fun String.isLocalBaseUrl(): Boolean {
    val host = Url(this).host.lowercase()
    return host == "localhost" || host == "127.0.0.1" || host == "::1"
}
