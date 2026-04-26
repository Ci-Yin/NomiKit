package ciyin.ai.core.chat

import kotlinx.serialization.json.JsonElement

/**
 * 跨引擎通用的聊天请求。
 *
 * 设计原则：
 * - 公共字段只覆盖**稳定**的能力（[messages] / [options] / [tools] / [attachments]）；
 * - 厂商专有字段（OpenAI 的 `response_format`、Claude 的 `cache_control`、DeepSeek 的某些扩展等）
 *   通过 [vendorOptions] 透传，由对应 engine 解释；**不**在 ai-core 抽象。
 * - 同一个 `ChatRequest` 在不同模型下应**行为可比**——差异化能力由 `vendorOptions` 覆盖。
 *
 * @property model 模型名（如 `"gpt-4o-mini"` / `"deepseek-chat"`）；`null` 表示由引擎默认值决定。
 * @property messages 完整的对话历史，按发送顺序排列。引擎不维护会话状态，调用方负责拼接。
 * @property options 通用调用选项，参见 [ChatOptions]。
 * @property tools 可选工具列表，启用 function calling 时使用。
 * @property attachments 请求级附件（与某条具体消息无强绑定，例如全局检索结果）。
 * @property vendorOptions 厂商专有参数，键名建议加厂商前缀以避免冲突，
 *           如 `"openai.response_format"` / `"anthropic.cache_control"`。
 */
data class ChatRequest(
    val model: String? = null,
    val messages: List<ChatMessage>,
    val options: ChatOptions = ChatOptions(),
    val tools: List<ChatToolSpec> = emptyList(),
    val attachments: List<ChatAttachment> = emptyList(),
    val vendorOptions: Map<String, JsonElement> = emptyMap(),
)
