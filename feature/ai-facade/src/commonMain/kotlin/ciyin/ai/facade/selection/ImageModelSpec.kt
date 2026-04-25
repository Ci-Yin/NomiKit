package ciyin.ai.facade.selection

import ciyin.ai.core.capability.ImageCapability
import ciyin.ai.core.engine.EngineId
import ciyin.ai.core.engine.ImageEngine

/**
 * 业务侧用来"挑哪个生图模型"的轻量描述，与 [ChatModelSpec] 形态完全对称。
 *
 * 关键约束：业务层**永远不**直接持有 [ImageEngine] 实例，只通过本类型表达意图。
 */
sealed interface ImageModelSpec {

    /** 使用 `EnginePreferences` 提供的默认模型。 */
    data object Default : ImageModelSpec

    /**
     * 显式指定 [EngineId] + 该引擎下的 [model]。
     *
     * @property engineId 目标引擎 ID。
     * @property model 模型名；`null` 表示沿用引擎当前激活的默认模型。
     */
    data class Explicit(
        val engineId: EngineId,
        val model: String? = null,
    ) : ImageModelSpec

    /**
     * 按能力筛选。
     *
     * @property required 必须同时具备的能力集合，空集合等价于 [Default]。
     */
    data class ByCapability(val required: Set<ImageCapability>) : ImageModelSpec
}
