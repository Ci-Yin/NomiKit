package ciyin.ai.core.chat

import ciyin.ai.core.error.AiEngineError

/**
 * 聊天调用过程中产出的事件。
 *
 * 引擎实现的 `Flow<ChatEvent>` 必须遵守的约束：
 * 1. 第一个事件应为 [Started]；
 * 2. 流必须以 [Completed] 或 [Failed] 之一结束；
 * 3. 同一次调用中 `Completed` 与 `Failed` **不能**同时出现；
 * 4. 中间过程允许任意数量的 [Delta] / [ToolCall] 穿插。
 */
sealed interface ChatEvent {

    /** 调用已发出，正在等待第一个响应。 */
    data object Started : ChatEvent

    /**
     * 一段流式文本增量。
     *
     * @property text 本次新增的文本片段（已是相对前一段的"增量"，调用方直接 append 即可）。
     */
    data class Delta(val text: String) : ChatEvent

    /**
     * 工具调用通知。
     *
     * 部分流式协议会先单独通知工具调用、再 done；非流式协议则只在 `Completed` 时一次性给出。
     *
     * @property id 与最终 [ChatResponse.toolCalls] 中对应项 [ChatToolCall.id] 一致。
     * @property name 工具名。
     * @property arguments JSON 字符串形式的参数，可能是流式拼出的不完整片段——
     *           **不**在 ai-core 层做合并，调用方按 [id] 自行聚合。
     */
    data class ToolCall(
        val id: String,
        val name: String,
        val arguments: String,
    ) : ChatEvent

    /**
     * 调用成功完成。
     *
     * @property response 完整聚合后的结果，参见 [ChatResponse]。
     */
    data class Completed(val response: ChatResponse) : ChatEvent

    /**
     * 调用失败。
     *
     * @property error 引擎层错误模型，参见 `ciyin.ai.core.error.AiEngineError`。
     */
    data class Failed(val error: AiEngineError) : ChatEvent
}
