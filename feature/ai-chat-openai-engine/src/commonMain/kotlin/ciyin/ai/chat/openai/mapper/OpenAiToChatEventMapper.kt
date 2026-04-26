package ciyin.ai.chat.openai.mapper

import ciyin.ai.chat.openai.dto.ChatCompletionChunkDto
import ciyin.ai.chat.openai.dto.ChatCompletionResponseDto
import ciyin.ai.chat.openai.dto.ToolCallDto
import ciyin.ai.chat.openai.dto.UsageDto
import ciyin.ai.core.chat.ChatEvent
import ciyin.ai.core.chat.ChatResponse
import ciyin.ai.core.chat.ChatToolCall
import ciyin.ai.core.chat.ChatUsage

/**
 * OpenAI 流式 / 非流式响应聚合器。
 *
 * 它负责两件事：
 * - 在流式 chunk 到来时产出增量事件（`Delta` / `ToolCall`）；
 * - 在流结束后构建最终 [ChatResponse]。
 */
internal class ChatResponseAccumulator {

    private val content = StringBuilder()
    private val toolCalls = linkedMapOf<Int, ToolCallBuffer>()
    private var finishReason: String? = null
    private var usage: ChatUsage? = null

    /**
     * 追加一个流式 chunk，并返回应向上游发出的增量事件。
     */
    fun append(chunk: ChatCompletionChunkDto): List<ChatEvent> {
        chunk.usage?.let { usage = it.toChatUsage() }
        val events = mutableListOf<ChatEvent>()
        chunk.choices.forEach { choice ->
            choice.finishReason?.let { finishReason = it }
            val delta = choice.delta ?: return@forEach
            delta.content?.takeIf { it.isNotEmpty() }?.let { text ->
                content.append(text)
                events += ChatEvent.Delta(text)
            }
            delta.toolCalls.orEmpty().forEach { toolCall ->
                val index = toolCall.index ?: 0
                val buffer = toolCalls.getOrPut(index) { ToolCallBuffer() }
                buffer.merge(toolCall)
                events += ChatEvent.ToolCall(
                    id = buffer.id,
                    name = buffer.name,
                    arguments = toolCall.function.arguments.orEmpty(),
                )
            }
        }
        return events
    }

    /**
     * 直接吸收一个非流式完整响应。
     */
    fun absorb(response: ChatCompletionResponseDto): ChatResponse {
        val choice = response.choices.firstOrNull()
            ?: return ChatResponse(content = "")
        content.clear()
        content.append(choice.message.content.orEmpty())
        toolCalls.clear()
        choice.message.toolCalls.orEmpty().forEachIndexed { index, toolCall ->
            toolCalls[index] = ToolCallBuffer().apply { merge(toolCall) }
        }
        finishReason = choice.finishReason
        usage = response.usage?.toChatUsage()
        return build()
    }

    /**
     * 构建最终聚合结果。
     */
    fun build(): ChatResponse = ChatResponse(
        content = content.toString(),
        toolCalls = toolCalls.toMap().values.map { it.toChatToolCall() },
        usage = usage,
        finishReason = finishReason,
    )
}

/**
 * 聚合中的工具调用缓冲区。
 */
private class ToolCallBuffer {
    var id: String = ""
    var name: String = ""
    private val arguments = StringBuilder()

    /**
     * 合并一段工具调用增量。
     */
    fun merge(toolCall: ToolCallDto) {
        toolCall.id?.takeIf { it.isNotEmpty() }?.let { id = it }
        toolCall.function.name?.takeIf { it.isNotEmpty() }?.let { name = it }
        toolCall.function.arguments?.takeIf { it.isNotEmpty() }?.let(arguments::append)
    }

    /**
     * 产出最终 [ChatToolCall]。
     */
    fun toChatToolCall(): ChatToolCall = ChatToolCall(
        id = id,
        name = name,
        arguments = arguments.toString(),
    )
}

/**
 * 把协议层 usage 映射为通用用量对象。
 */
private fun UsageDto.toChatUsage(): ChatUsage = ChatUsage(
    promptTokens = promptTokens,
    completionTokens = completionTokens,
    totalTokens = totalTokens,
)
