package ciyin.ai.chat.openai.mapper

import ciyin.ai.chat.openai.OpenAiChatEngineConfig
import ciyin.ai.chat.openai.client.OpenAiJson
import ciyin.ai.chat.openai.dto.ChatCompletionRequestDto
import ciyin.ai.chat.openai.dto.FunctionCallDto
import ciyin.ai.chat.openai.dto.FunctionDefinitionDto
import ciyin.ai.chat.openai.dto.MessageDto
import ciyin.ai.chat.openai.dto.ToolCallDto
import ciyin.ai.chat.openai.dto.ToolDto
import ciyin.ai.core.chat.ChatAttachment
import ciyin.ai.core.chat.ChatMessage
import ciyin.ai.core.chat.ChatRequest
import ciyin.ai.core.error.AiEngineError
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * 把通用 [ChatRequest] 映射为 OpenAI 兼容请求体。
 */
internal fun ChatRequest.toOpenAiRequestBody(config: OpenAiChatEngineConfig): JsonObject {
    val requestDto = ChatCompletionRequestDto(
        model = model ?: config.defaultModel
        ?: throw OpenAiMappingException(AiEngineError.Unsupported("OpenAI 引擎缺少 model，且未配置 defaultModel")),
        messages = messages.map { it.toMessageDto() },
        temperature = options.temperature,
        topP = options.topP,
        maxTokens = options.maxOutputTokens,
        stop = options.stop.takeIf { it.isNotEmpty() },
        seed = options.seed,
        stream = options.stream,
        tools = tools.takeIf { it.isNotEmpty() }?.map { tool ->
            ToolDto(
                function = FunctionDefinitionDto(
                    name = tool.name,
                    description = tool.description,
                    parameters = tool.parametersJsonSchema,
                ),
            )
        },
        toolChoice = tools.takeIf { it.isNotEmpty() }?.let { "auto" },
    )
    val base = OpenAiJson.encodeToJsonElement(
        ChatCompletionRequestDto.serializer(),
        requestDto
    ) as JsonObject
    if (vendorOptions.isEmpty()) return base
    return JsonObject(base + vendorOptions)
}

/**
 * 把通用消息映射为协议消息 DTO。
 */
private fun ChatMessage.toMessageDto(): MessageDto = when (this) {
    is ChatMessage.System -> MessageDto(
        role = "system",
        content = JsonPrimitive(content),
    )

    is ChatMessage.User -> MessageDto(
        role = "user",
        content = toUserContent(content, attachments),
    )

    is ChatMessage.Assistant -> MessageDto(
        role = "assistant",
        content = content.takeIf { it.isNotEmpty() }?.let(::JsonPrimitive),
        toolCalls = toolCalls.takeIf { it.isNotEmpty() }?.mapIndexed { index, call ->
            ToolCallDto(
                id = call.id,
                index = index,
                function = FunctionCallDto(
                    name = call.name,
                    arguments = call.arguments,
                ),
            )
        },
    )

    is ChatMessage.Tool -> MessageDto(
        role = "tool",
        content = JsonPrimitive(content),
        toolCallId = toolCallId,
    )
}

/**
 * 把用户文本与多模态附件映射为 OpenAI 兼容 content。
 */
private fun toUserContent(
    text: String,
    attachments: List<ChatAttachment>,
): JsonElement {
    if (attachments.isEmpty()) return JsonPrimitive(text)
    return buildJsonArray {
        if (text.isNotEmpty()) {
            add(
                buildJsonObject {
                    put("type", "text")
                    put("text", text)
                },
            )
        }
        attachments.forEach { attachment ->
            add(attachment.toContentPart())
        }
    }
}

/**
 * 把附件映射为 OpenAI 兼容多模态 content part。
 */
private fun ChatAttachment.toContentPart(): JsonObject = when (this) {
    is ChatAttachment.Image -> buildJsonObject {
        put("type", "image_url")
        putJsonObject("image_url") {
            put("url", bytes.toDataUrl(mimeType))
        }
    }

    is ChatAttachment.Document -> throw OpenAiMappingException(
        AiEngineError.Unsupported("OpenAI 兼容聊天当前仅支持图像附件，不支持文档附件"),
    )

    is ChatAttachment.Audio -> throw OpenAiMappingException(
        AiEngineError.Unsupported("OpenAI 兼容聊天当前仅支持图像附件，不支持音频附件"),
    )
}

/**
 * 把二进制图像转成 data URL。
 */
@OptIn(ExperimentalEncodingApi::class)
private fun ByteArray.toDataUrl(mimeType: String): String =
    "data:$mimeType;base64,${Base64.Default.encode(this)}"

/**
 * mapper 内部用于短路失败的异常包装。
 */
internal class OpenAiMappingException(
    val error: AiEngineError,
) : RuntimeException(error.toString())
