package ciyin.ai.chat.openai.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 * OpenAI 兼容 `/chat/completions` 请求体。
 */
@Serializable
internal data class ChatCompletionRequestDto(
    @SerialName("model") val model: String,
    @SerialName("messages") val messages: List<MessageDto>,
    @SerialName("temperature") val temperature: Float? = null,
    @SerialName("top_p") val topP: Float? = null,
    @SerialName("max_tokens") val maxTokens: Int? = null,
    @SerialName("stop") val stop: List<String>? = null,
    @SerialName("seed") val seed: Long? = null,
    @SerialName("stream") val stream: Boolean = true,
    @SerialName("tools") val tools: List<ToolDto>? = null,
    @SerialName("tool_choice") val toolChoice: String? = null,
)

/**
 * OpenAI 协议消息 DTO。
 */
@Serializable
internal data class MessageDto(
    @SerialName("role") val role: String,
    @SerialName("content") val content: JsonElement? = null,
    @SerialName("tool_calls") val toolCalls: List<ToolCallDto>? = null,
    @SerialName("tool_call_id") val toolCallId: String? = null,
)

/**
 * OpenAI 协议工具定义 DTO。
 */
@Serializable
internal data class ToolDto(
    @SerialName("type") val type: String = "function",
    @SerialName("function") val function: FunctionDefinitionDto,
)

/**
 * 工具函数定义。
 */
@Serializable
internal data class FunctionDefinitionDto(
    @SerialName("name") val name: String,
    @SerialName("description") val description: String,
    @SerialName("parameters") val parameters: JsonObject,
)

/**
 * 协议层工具调用 DTO。
 */
@Serializable
internal data class ToolCallDto(
    @SerialName("id") val id: String? = null,
    @SerialName("index") val index: Int? = null,
    @SerialName("type") val type: String = "function",
    @SerialName("function") val function: FunctionCallDto,
)

/**
 * 工具函数调用 DTO。
 */
@Serializable
internal data class FunctionCallDto(
    @SerialName("name") val name: String? = null,
    @SerialName("arguments") val arguments: String? = null,
)
