package ciyin.ai.core.chat

import ciyin.ai.core.capability.ChatCapability
import ciyin.ai.core.engine.EngineId

/**
 * 聊天模型描述信息。
 *
 * 由 `ChatEngine.listModels()` / `AiChat.listAvailableModels()` 返回，供 UI 展示与选择。
 *
 * @property engineId 模型所在引擎的 ID；上层做"在哪家挑哪个模型"时需要这个信息。
 * @property model 模型名（如 `"gpt-4o-mini"`）。
 * @property displayName 推荐展示名；为 `null` 时由 UI 直接展示 [model]。
 * @property capabilities 该模型实际具备的能力子集（可能比所属引擎宣称的更窄）。
 * @property contextWindow 模型上下文窗口（输入+输出 token 上限）；`null` 表示未知。
 * @property maxOutputTokens 单次回复 token 上限；`null` 表示未知或无独立限制。
 */
data class ChatModelInfo(
    val engineId: EngineId,
    val model: String,
    val displayName: String? = null,
    val capabilities: Set<ChatCapability> = emptySet(),
    val contextWindow: Int? = null,
    val maxOutputTokens: Int? = null,
)
