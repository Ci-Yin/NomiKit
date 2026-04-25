package ciyin.ai.core.registry

import ciyin.ai.core.capability.ChatCapability
import ciyin.ai.core.capability.ImageCapability
import ciyin.ai.core.engine.ChatEngine
import ciyin.ai.core.engine.EngineId
import ciyin.ai.core.engine.ImageEngine
import ciyin.ai.core.error.UnsupportedCapabilityException

/**
 * 引擎选择器：按"偏好 ID + 必要能力"挑出唯一的 [ChatEngine] / [ImageEngine]。
 *
 * 选择策略（[selectChat] 与 [selectImage] 完全同构）：
 * 1. 若 [EngineId] 不为空且对应引擎存在并满足 [Set] 全部能力，优先返回；
 * 2. 否则按能力过滤后返回**注册顺序的第一个**（注册顺序由调用方装配时传入的 `List` 顺序决定）；
 * 3. 找不到则抛 [UnsupportedCapabilityException]。
 *
 * 设计约束：
 * - 本类**不**负责降级链路、重试、观测、计费；这些策略由 `feature/ai-facade` 的
 *   `AiChat` / `AiImage` 实现内部消化（见设计文档第六节）。
 * - 业务层默认**不**直接使用本类，而是通过 Facade 间接享用其能力；保留 `public`
 *   是为了"扫描所有可用引擎"等批量场景仍可绕开 Facade。
 *
 * @property chatRegistry 聊天引擎注册表。
 * @property imageRegistry 生图引擎注册表。
 */
class EngineSelector(
    private val chatRegistry: ChatEngineRegistry,
    private val imageRegistry: ImageEngineRegistry,
) {

    /**
     * 返回全部已注册的聊天引擎，顺序与装配时传入 Registry 的顺序一致。
     *
     * 主要供 `feature/ai-facade` 这类需要做"聚合列模型 / 探测全部引擎"的上层技术模块使用；
     * 业务侧默认仍应通过 `AiChat` 访问聊天能力，而不是直接遍历这里的结果。
     */
    fun allChat(): List<ChatEngine> = chatRegistry.all()

    /**
     * 返回全部已注册的生图引擎，顺序与装配时传入 Registry 的顺序一致。
     *
     * 语义与 [allChat] 同构。
     */
    fun allImage(): List<ImageEngine> = imageRegistry.all()

    /**
     * 选择一个聊天引擎。
     *
     * @param preferredId 业务侧偏好的引擎 ID；命中且满足 [required] 时优先返回。
     * @param required 必须同时具备的能力集合；空集合表示"任意聊天引擎均可"。
     * @throws UnsupportedCapabilityException 没有任何已注册引擎满足 [required]。
     */
    fun selectChat(
        preferredId: EngineId? = null,
        required: Set<ChatCapability> = emptySet(),
    ): ChatEngine {
        if (preferredId != null) {
            val preferred = chatRegistry.get(preferredId)
            if (preferred != null && preferred.satisfies(required)) {
                return preferred
            }
        }
        return chatRegistry
            .findByCapability(*required.toTypedArray())
            .firstOrNull()
            ?: throw UnsupportedCapabilityException(required)
    }

    /**
     * 选择一个生图引擎。
     *
     * @param preferredId 业务侧偏好的引擎 ID；命中且满足 [required] 时优先返回。
     * @param required 必须同时具备的能力集合；空集合表示"任意生图引擎均可"。
     * @throws UnsupportedCapabilityException 没有任何已注册引擎满足 [required]。
     */
    fun selectImage(
        preferredId: EngineId? = null,
        required: Set<ImageCapability> = emptySet(),
    ): ImageEngine {
        if (preferredId != null) {
            val preferred = imageRegistry.get(preferredId)
            if (preferred != null && preferred.satisfies(required)) {
                return preferred
            }
        }
        return imageRegistry
            .findByCapability(*required.toTypedArray())
            .firstOrNull()
            ?: throw UnsupportedCapabilityException(required)
    }

    private fun ChatEngine.satisfies(required: Set<ChatCapability>): Boolean =
        required.isEmpty() || capabilities.containsAll(required)

    private fun ImageEngine.satisfies(required: Set<ImageCapability>): Boolean =
        required.isEmpty() || capabilities.containsAll(required)
}
