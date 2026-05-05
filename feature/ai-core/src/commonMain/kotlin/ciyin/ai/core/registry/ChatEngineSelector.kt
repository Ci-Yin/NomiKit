package ciyin.ai.core.registry

import ciyin.ai.core.capability.ChatCapability
import ciyin.ai.core.engine.ChatEngine
import ciyin.ai.core.engine.EngineId
import ciyin.ai.core.error.UnsupportedCapabilityException

/**
 * 聊天引擎选择器：按"偏好 ID + 必要能力"挑出唯一的 [ChatEngine]。
 *
 * 选择策略：
 * 1. 若 [EngineId] 不为空且对应引擎存在并满足 [required] 全部能力，优先返回；
 * 2. 否则按能力过滤后返回**注册顺序的第一个**（顺序由装配时传入 Registry 的 `List` 决定）；
 * 3. 找不到则抛 [UnsupportedCapabilityException]。
 *
 * 设计约束：
 * - 本类**不**负责降级链路、重试、观测；这些由 `feature/ai-facade` 的 `AiChat` 消化。
 * - 业务层默认通过 Facade 间接使用；保留 `public` 便于需要遍历引擎等场景。
 *
 * @property registry 聊天引擎注册表。
 */
class ChatEngineSelector(
    private val registry: ChatEngineRegistry,
) {

    /** 返回全部已注册的聊天引擎，顺序与装配时传入 Registry 的顺序一致。 */
    fun all(): List<ChatEngine> = registry.all()

    /**
     * 选择一个聊天引擎。
     *
     * @param preferredId 业务侧偏好的引擎 ID；命中且满足 [required] 时优先返回。
     * @param required 必须同时具备的能力集合；空集合表示"任意聊天引擎均可"。
     * @throws UnsupportedCapabilityException 没有任何已注册引擎满足 [required]。
     */
    fun select(
        preferredId: EngineId? = null,
        required: Set<ChatCapability> = emptySet(),
    ): ChatEngine {
        if (preferredId != null) {
            val preferred = registry.get(preferredId)
            if (preferred != null && preferred.satisfies(required)) {
                return preferred
            }
        }
        return registry
            .findByCapability(*required.toTypedArray())
            .firstOrNull()
            ?: throw UnsupportedCapabilityException(required)
    }

    private fun ChatEngine.satisfies(required: Set<ChatCapability>): Boolean =
        required.isEmpty() || capabilities.containsAll(required)
}
