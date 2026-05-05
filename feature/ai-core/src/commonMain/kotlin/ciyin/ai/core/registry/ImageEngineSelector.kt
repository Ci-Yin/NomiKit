package ciyin.ai.core.registry

import ciyin.ai.core.capability.ImageCapability
import ciyin.ai.core.engine.EngineId
import ciyin.ai.core.engine.ImageEngine
import ciyin.ai.core.error.UnsupportedCapabilityException

/**
 * 生图引擎选择器：按"偏好 ID + 必要能力"挑出唯一的 [ImageEngine]。
 *
 * 选择策略与 [ChatEngineSelector] 同构：
 * 1. 偏好 ID 命中且满足 [required] 时优先返回；
 * 2. 否则按能力过滤后返回注册顺序的第一个；
 * 3. 找不到则抛 [UnsupportedCapabilityException]。
 *
 * @property registry 生图引擎注册表。
 */
class ImageEngineSelector(
    private val registry: ImageEngineRegistry,
) {

    /** 返回全部已注册的生图引擎，顺序与装配时传入 Registry 的顺序一致。 */
    fun all(): List<ImageEngine> = registry.all()

    /**
     * 选择一个生图引擎。
     *
     * @param preferredId 业务侧偏好的引擎 ID；命中且满足 [required] 时优先返回。
     * @param required 必须同时具备的能力集合；空集合表示"任意生图引擎均可"。
     * @throws UnsupportedCapabilityException 没有任何已注册引擎满足 [required]。
     */
    fun select(
        preferredId: EngineId? = null,
        required: Set<ImageCapability> = emptySet(),
    ): ImageEngine {
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

    private fun ImageEngine.satisfies(required: Set<ImageCapability>): Boolean =
        required.isEmpty() || capabilities.containsAll(required)
}
