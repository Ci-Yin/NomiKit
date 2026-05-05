package ciyin.ai.core.capability

/**
 * 聊天引擎能力。
 *
 * 用于 `Registry.findByCapability(...)` 做能力筛选，以及上层 [ciyin.ai.core.registry.ChatEngineSelector]
 * 决定"按能力挑引擎"时使用。新增能力请保持向后兼容（只追加、不删除）。
 */
sealed interface ChatCapability : AiCapability {

    /** 支持流式输出（SSE / token streaming）。 */
    data object Streaming : ChatCapability

    /** 支持函数 / 工具调用（OpenAI tools、Claude tool_use 等）。 */
    data object ToolCalling : ChatCapability

    /** 支持图像输入（多模态视觉理解）。 */
    data object VisionInput : ChatCapability

    /** 支持强制 JSON 输出（如 OpenAI `response_format = json_object`）。 */
    data object JsonOutput : ChatCapability

    /** 支持 system prompt（少数本地小模型可能不支持）。 */
    data object SystemPrompt : ChatCapability

    /** 支持 prompt 缓存（如 Anthropic `cache_control`、DeepSeek 上下文缓存）。 */
    data object PromptCaching : ChatCapability
}
