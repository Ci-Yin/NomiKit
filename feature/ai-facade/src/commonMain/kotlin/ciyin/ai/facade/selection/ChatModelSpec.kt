package ciyin.ai.facade.selection

import ciyin.ai.core.capability.ChatCapability
import ciyin.ai.core.engine.ChatEngine
import ciyin.ai.core.engine.EngineId

/**
 * 业务侧用来"挑哪个聊天模型"的轻量描述。
 *
 * 关键约束：业务层**永远不**直接持有 [ChatEngine] 实例，只通过本类型表达"我想用哪种"，
 * 由 `AiChat` 实现内部解析为具体引擎。这样保证业务代码与具体厂商完全解耦。
 *
 * 三种取值的语义：
 * - [Default]：用 `EnginePreferences.defaultChatSpec()` 返回的偏好；
 * - [Explicit]：用户在 UI 上明确选择了某个引擎/模型；
 * - [ByCapability]：业务侧只关心能力而非厂商（"找一个能 Streaming + ToolCalling 的"）。
 */
sealed interface ChatModelSpec {

    /** 使用 `EnginePreferences` 提供的默认模型；适合"刚启动 / 用户未做任何选择"场景。 */
    data object Default : ChatModelSpec

    /**
     * 显式指定 [EngineId] + 该引擎下的 [model]。
     *
     * @property engineId 目标引擎 ID。
     * @property model 模型名；`null` 表示沿用引擎当前激活的默认模型。
     */
    data class Explicit(
        val engineId: EngineId,
        val model: String? = null,
    ) : ChatModelSpec

    /**
     * 按能力筛选，由 `AiChat` 实现按注册顺序挑首个满足条件的引擎。
     *
     * @property required 必须同时具备的能力集合，空集合等价于 [Default]。
     */
    data class ByCapability(val required: Set<ChatCapability>) : ChatModelSpec
}
