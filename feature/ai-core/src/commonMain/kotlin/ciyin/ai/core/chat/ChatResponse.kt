package ciyin.ai.core.chat

/**
 * 一次完整聊天调用的最终结果。
 *
 * 由 [ChatEvent.Completed] 携带向上传递。流式调用过程中可以从 [ChatEvent.Delta] / [ChatEvent.ToolCall]
 * 中拼出大致内容，但只有 `Completed` 才包含官方权威的最终文本与 token 计数。
 *
 * @property content 助手最终回复的完整文本（已聚合所有 Delta）。
 * @property toolCalls 模型本轮发起的工具调用集合（若有）。
 * @property usage Token 用量统计；`null` 表示上游未提供。
 * @property finishReason 结束原因的原始字符串（如 `"stop"` / `"length"` / `"tool_calls"`），
 *           不做枚举化映射，原样保留以避免上游新增取值时丢失信息。
 */
data class ChatResponse(
    val content: String,
    val toolCalls: List<ChatToolCall> = emptyList(),
    val usage: ChatUsage? = null,
    val finishReason: String? = null,
)

/**
 * Token 用量统计。
 *
 * @property promptTokens 输入 token 数（含 system / user / 历史 assistant）。
 * @property completionTokens 本轮输出 token 数。
 * @property totalTokens 上游汇报的总 token 数；`null` 表示上游未提供，调用方可自己相加。
 */
data class ChatUsage(
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int? = null,
)
