package ciyin.ai.core.registry

import ciyin.ai.core.capability.ChatCapability
import ciyin.ai.core.engine.ChatEngine
import ciyin.ai.core.engine.EngineId

/**
 * 聊天引擎注册表。
 *
 * 仅承担"按 ID / 按能力查找"的职责，**不**负责选择策略与降级——
 * 这些决策属于 [EngineSelector] 与 `feature/ai-facade` 的范畴。
 *
 * 默认实现见 [DefaultChatEngineRegistry]。
 */
interface ChatEngineRegistry {

    /** 注册到本 Registry 的全部引擎，顺序与构造时传入顺序一致。 */
    fun all(): List<ChatEngine>

    /** 按 [id] 精确查找；找不到返回 `null`。 */
    fun get(id: EngineId): ChatEngine?

    /**
     * 按能力筛选：返回**全部**满足"同时具备 [required] 中所有能力"的引擎。
     *
     * 顺序与 [all] 保持一致；调用方据此可做"取首个"等策略。
     */
    fun findByCapability(vararg required: ChatCapability): List<ChatEngine>
}
