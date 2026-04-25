package ciyin.ai.chat.openai.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * OpenAI 兼容非流式 `/chat/completions` 响应 DTO。
 */
@Serializable
internal data class ChatCompletionResponseDto(
    @SerialName("id") val id: String? = null,
    @SerialName("model") val model: String? = null,
    @SerialName("choices") val choices: List<ResponseChoiceDto> = emptyList(),
    @SerialName("usage") val usage: UsageDto? = null,
)

/**
 * 非流式 choice。
 */
@Serializable
internal data class ResponseChoiceDto(
    @SerialName("index") val index: Int = 0,
    @SerialName("message") val message: ResponseMessageDto,
    @SerialName("finish_reason") val finishReason: String? = null,
)

/**
 * 非流式消息。
 */
@Serializable
internal data class ResponseMessageDto(
    @SerialName("role") val role: String,
    @SerialName("content") val content: String? = null,
    @SerialName("tool_calls") val toolCalls: List<ToolCallDto>? = null,
)

/**
 * `/models` 响应 DTO。
 */
@Serializable
internal data class ModelListResponseDto(
    @SerialName("data") val data: List<ModelDto> = emptyList(),
)

/**
 * 单个模型项 DTO。
 */
@Serializable
internal data class ModelDto(
    @SerialName("id") val id: String,
)
