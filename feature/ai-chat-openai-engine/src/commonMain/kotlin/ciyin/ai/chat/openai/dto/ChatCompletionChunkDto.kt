package ciyin.ai.chat.openai.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * OpenAI 兼容流式 chunk DTO。
 */
@Serializable
internal data class ChatCompletionChunkDto(
    @SerialName("id") val id: String? = null,
    @SerialName("model") val model: String? = null,
    @SerialName("choices") val choices: List<ChunkChoiceDto> = emptyList(),
    @SerialName("usage") val usage: UsageDto? = null,
)

/**
 * 流式 choice。
 */
@Serializable
internal data class ChunkChoiceDto(
    @SerialName("index") val index: Int = 0,
    @SerialName("delta") val delta: DeltaDto? = null,
    @SerialName("finish_reason") val finishReason: String? = null,
)

/**
 * 流式增量片段。
 */
@Serializable
internal data class DeltaDto(
    @SerialName("role") val role: String? = null,
    @SerialName("content") val content: String? = null,
    @SerialName("tool_calls") val toolCalls: List<ToolCallDto>? = null,
)

/**
 * token 用量 DTO。
 */
@Serializable
internal data class UsageDto(
    @SerialName("prompt_tokens") val promptTokens: Int = 0,
    @SerialName("completion_tokens") val completionTokens: Int = 0,
    @SerialName("total_tokens") val totalTokens: Int? = null,
)
