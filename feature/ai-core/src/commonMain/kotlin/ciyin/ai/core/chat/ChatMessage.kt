package ciyin.ai.core.chat

/**
 * 跨引擎通用的聊天消息。
 *
 * 设计上覆盖四种典型角色：[System] / [User] / [Assistant] / [Tool]，
 * 与 OpenAI / Anthropic / DeepSeek / Ollama 等主流协议都能直接对应。
 *
 * 厂商特有的角色（如 Anthropic 的 `assistant_with_thinking`）由各自适配层在
 * mapper 中以 `vendorOptions` + 普通 `Assistant` 表达，**不**进入 `ai-core`。
 */
sealed interface ChatMessage {

    /** 系统提示。多数厂商只支持一个；多个 system 时由适配层决定如何合并。 */
    data class System(val content: String) : ChatMessage

    /**
     * 用户消息。
     *
     * @property content 文本内容。
     * @property attachments 可选附件，多模态输入由此承载。
     */
    data class User(
        val content: String,
        val attachments: List<ChatAttachment> = emptyList(),
    ) : ChatMessage

    /**
     * 助手消息。
     *
     * @property content 文本内容；当本轮只产生工具调用时可能为空字符串。
     * @property toolCalls 助手发起的工具调用列表（用于 `tools` 协议）。
     */
    data class Assistant(
        val content: String,
        val toolCalls: List<ChatToolCall> = emptyList(),
    ) : ChatMessage

    /**
     * 工具调用结果消息。
     *
     * 由调用方在执行完工具后回填给引擎，用于继续下一轮对话。
     *
     * @property toolCallId 对应的 [ChatToolCall.id]，引擎依靠该 ID 关联请求与结果。
     * @property content 工具产出的文本结果（一般是 JSON 字符串）。
     */
    data class Tool(
        val toolCallId: String,
        val content: String,
    ) : ChatMessage
}

/**
 * 助手发起的一次工具调用记录。
 *
 * 与 OpenAI tools / Anthropic tool_use / 其他类似协议保持同构。
 *
 * @property id 工具调用唯一 ID，由引擎生成；调用方在回填 [ChatMessage.Tool] 时必须原样带回。
 * @property name 工具名（对应 [ChatToolSpec.name]）。
 * @property arguments 工具参数的 JSON 字符串（保持原始字符串避免双重序列化损耗）。
 */
data class ChatToolCall(
    val id: String,
    val name: String,
    val arguments: String,
)
