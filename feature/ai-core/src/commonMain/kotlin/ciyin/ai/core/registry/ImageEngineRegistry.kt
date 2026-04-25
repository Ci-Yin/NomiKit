package ciyin.ai.core.registry

import ciyin.ai.core.capability.ImageCapability
import ciyin.ai.core.engine.EngineId
import ciyin.ai.core.engine.ImageEngine

/**
 * 生图引擎注册表。
 *
 * 与 [ChatEngineRegistry] 同构，仅承担查找职责。
 *
 * 默认实现见 [DefaultImageEngineRegistry]。
 */
interface ImageEngineRegistry {

    /** 注册到本 Registry 的全部引擎，顺序与构造时传入顺序一致。 */
    fun all(): List<ImageEngine>

    /** 按 [id] 精确查找；找不到返回 `null`。 */
    fun get(id: EngineId): ImageEngine?

    /**
     * 按能力筛选：返回**全部**满足"同时具备 [required] 中所有能力"的引擎。
     */
    fun findByCapability(vararg required: ImageCapability): List<ImageEngine>
}
